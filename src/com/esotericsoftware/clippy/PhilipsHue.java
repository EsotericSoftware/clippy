
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.esotericsoftware.clippy.Config.ColorTime;
import com.esotericsoftware.clippy.Config.ColorTime.Power;
import com.esotericsoftware.clippy.Config.PhilipsHueLights;
import com.esotericsoftware.clippy.util.ColorTimeline;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHRuleListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.rule.PHRule;
import com.philips.lighting.model.rule.PHRuleAction;
import com.philips.lighting.model.rule.PHRuleCondition;
import com.philips.lighting.model.rule.PHSimpleRuleCondition;
import com.philips.lighting.model.rule.PHSimpleRuleCondition.PHSimpleRuleAttributeName;
import com.philips.lighting.model.sensor.PHSensor;

public class PhilipsHue {
	final Clippy clippy = Clippy.instance;
	ProgressBar progress;
	CloseableHttpClient http;

	public PhilipsHue () {
		if (!clippy.config.philipsHueEnabled) return;

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(clippy.config.philipsHue.size());
		connectionManager.setDefaultMaxPerRoute(clippy.config.philipsHue.size());
		http = HttpClients.custom().setConnectionManager(connectionManager).build();

		Thread thread = new Thread("PhilipsHueStart") {
			public void run () {
				PhilipsHue.this.start();
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	void start () {
		final PHHueSDK hue = PHHueSDK.getInstance();
		hue.setAppName("Clippy");

		String name = System.getenv("COMPUTERNAME");
		if (name == null) name = System.getenv("HOSTNAME");
		if (name == null) name = "Unknown";
		hue.setDeviceName(name);

		hue.getNotificationManager().registerSDKListener(new PHSDKListener() {
			private boolean started;

			public void onAccessPointsFound (List<PHAccessPoint> accessPoints) {
				if (progress == null) progress = new ProgressBar("");
				if (accessPoints.size() == 1) {
					PHAccessPoint accessPoint = accessPoints.get(0);
					if (INFO) info("Philips Hue bridge found: " + accessPoint.getIpAddress());
					progress.green("Philips Hue bridge found: " + accessPoint.getIpAddress());
					hue.connect(accessPoint);
				} else {
					if (INFO) {
						info("Philips Hue bridges found:");
						for (PHAccessPoint accessPoint : accessPoints)
							info("  " + accessPoint.getIpAddress());
					}
					progress.done("Bridges found! See clippy.log for IPs.", -1);
					progress = null;
				}
			}

			public void onAuthenticationRequired (PHAccessPoint accessPoint) {
				if (INFO) {
					info("Philips Hue authentication required: " + accessPoint.getIpAddress());
					info("Press the Philips Hue link button...");
				}
				if (progress == null) progress = new ProgressBar("");
				progress.green("Press the Philips Hue link button...");
				hue.startPushlinkAuthentication(accessPoint);
			}

			public void onBridgeConnected (PHBridge bridge, String username) {
				if (INFO) info("Philips Hue bridge connected: " + username);
				if (progress != null) {
					progress.done("Philips Hue bridge connected!", 2000);
					progress = null;
				}

				String ip = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
				if (!ip.equals(clippy.config.philipsHueIP) || !username.equals(clippy.config.philipsHueUser)) {
					clippy.config.philipsHueIP = ip;
					clippy.config.philipsHueUser = username;
					clippy.config.save();
				}

				hue.setSelectedBridge(bridge);

				if (clippy.config.philipsHueIP != null && !started) {
					started = true;
					for (PhilipsHueLights lights : clippy.config.philipsHue)
						start(lights);
					for (PhilipsHueLights lights : clippy.config.philipsHue)
						if (lights.timeline != null) lights.timeline.setTimeline(Timeline.on);
				}
			}

			public void onConnectionLost (PHAccessPoint accessPoint) {
				if (WARN) warn("Philips Hue connection lost.");
			}

			public void onConnectionResumed (PHBridge bridge) {
				if (INFO) info("Philips Hue connection resumed.");
			}

			public void onCacheUpdated (List<Integer> messageTypes, PHBridge bridge) {
				if (TRACE) trace("Philips Hue cache updated: " + messageTypes);
			}

			public void onError (int code, String message) {
				if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
					if (TRACE) trace("Philips Hue error: " + message + " (" + code + ")");
					return;
				}
				if (ERROR) error("Philips Hue error: " + message + " (" + code + ")");
				if (!started) {
					if (progress == null) progress = new ProgressBar("");
					if (message == null) message = "Error " + code;
					progress.failed("Philips Hue: " + message, 20000);
					progress = null;
				}
			}

			public void onParsingErrors (List<PHHueParsingError> errors) {
				if (ERROR) {
					error("Philips Hue parsing errors:");
					for (PHHueParsingError error : errors) {
						error("  " + error.getMessage() + " (" + error.getCode() + "), " + error.getAddress() + ", "
							+ error.getResourceId());
					}
				}
			}
		});

		if (clippy.config.philipsHueIP != null) {
			if (DEBUG)
				debug("Connecting to Philips Hue bridge: " + clippy.config.philipsHueUser + " @ " + clippy.config.philipsHueIP);
			if (clippy.config.philipsHueUser == null)
				progress = new ProgressBar("Connecting to Philips Hue: " + clippy.config.philipsHueIP);
			PHAccessPoint accessPoint = new PHAccessPoint();
			accessPoint.setIpAddress(clippy.config.philipsHueIP);
			accessPoint.setUsername(clippy.config.philipsHueUser);
			hue.connect(accessPoint);
		} else {
			if (INFO) info("Searching for Philips Hue bridges...");
			progress = new ProgressBar("Searching for Philips Hue bridges...");
			PHBridgeSearchManager search = (PHBridgeSearchManager)hue.getSDKService(PHHueSDK.SEARCH_BRIDGE);
			search.search(true, true);
		}
	}

	void start (final PhilipsHueLights lights) {
		if (lights.times == null || lights.times.isEmpty()) return;
		boolean hasTimeline = false;
		for (ArrayList<ColorTime> timeline : lights.times.values()) {
			if (timeline != null && !timeline.isEmpty()) {
				hasTimeline = true;
				break;
			}
		}
		if (!hasTimeline) return;
		new PhilipsHueTimeline(lights).start();
	}

	static final PHLightListener lightListener = new PHLightListener() {
		public void onSuccess () {
			if (TRACE) trace("Philips Hue light successfully changed.");
		}

		public void onStateUpdate (Map<String, String> success, List<PHHueError> errors) {
			if (TRACE) trace("Philips Hue light state updated: " + success + ", " + errors);
		}

		public void onError (int code, String message) {
			if (ERROR) error("Philips Hue light change error: " + message + " (" + code + ")");
		}

		public void onSearchComplete () {
			if (TRACE) trace("Philips Hue light search complete.");
		}

		public void onReceivingLights (List<PHBridgeResource> resources) {
			if (TRACE) trace("Philips Hue received lights: " + resources);
		}

		public void onReceivingLightDetails (PHLight light) {
			if (TRACE) trace("Philips Hue received light details: " + light.getName());
		}
	};

	static final PHGroupListener groupListener = new PHGroupListener() {
		public void onCreated (PHGroup group) {
			if (TRACE) trace("Philips Hue group created: " + group.getName());
		}

		public void onSuccess () {
			if (TRACE) trace("Philips Hue group successfully changed.");
		}

		public void onStateUpdate (Map<String, String> success, List<PHHueError> errors) {
			if (TRACE) trace("Philips Hue group updated: " + success + ", " + errors);
		}

		public void onError (int code, String message) {
			if (ERROR) error("Philips Hue group change error: " + message + " (" + code + ")");
		}

		public void onReceivingAllGroups (List<PHBridgeResource> resources) {
			if (TRACE) trace("Philips Hue received groups: " + resources);
		}

		public void onReceivingGroupDetails (PHGroup group) {
			if (TRACE) trace("Philips Hue received group details: " + group.getName());
		}
	};

	PHRuleListener updateRuleListener = new PHRuleListener() {
		public void onSuccess () {
			if (TRACE) trace("Philips Hue rule successfully updated.");
		}

		public void onStateUpdate (Map<String, String> success, List<PHHueError> errors) {
			if (TRACE) trace("Philips Hue rule updated: " + success + ", " + errors);
		}

		public void onError (int code, String message) {
			if (ERROR) error("Philips Hue rule update error: " + message + " (" + code + ")");
		}

		public void onRuleReceived (List<PHRule> rules) {
			if (TRACE) trace("Philips Hue received rules: " + rules);
		}

		public void onReceivingRuleDetails (PHRule rule) {
			if (TRACE) trace("Philips Hue received rule details: " + rule);
		}
	};

	class PhilipsHueTimeline extends ColorTimeline {
		final PhilipsHueLights lights;
		HttpGet httpGetSensor;
		long brightnessDisabled;
		String lastEventDate;
		Timeline timeline;

		PhilipsHueTimeline (PhilipsHueLights lights) {
			super("PhilipsHue " + (lights.switchName != null ? lights.switchName : lights.name), null, 5 * 1000, 1000, 0,
				5 * 1000 - 250);
			this.lights = lights;
			lights.timeline = this;
		}

		protected void update () {
			// Check if switch state has changed.
			if (lights.switchName != null) {
				PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
				if (bridge != null) {
					SwitchEvent event = getSwitchEvent(bridge);
					if (event != null) {
						if (lastEventDate == null) lastEventDate = event.date;
						if (!lastEventDate.equals(event.date)) {
							lastEventDate = event.date;
							handleEvent(event, true);
						}
					}
				}
			}
			super.update();

			// If timeline has 1 entry and it has no time, go back to the on timeline after first update.
			// This allows holding a button to turn everything off without staying on the offHeld timeline.
			if (timeline != Timeline.on && times != null && times.size() == 1 && times.get(0).time == null) setTimeline(Timeline.on);
		}

		void handleEvent (SwitchEvent event, boolean forward) {
			if (TRACE) trace(type + " switch: " + event.button + ", " + event.state + ", " + event.date);

			// Change timeline.
			switch (event.button) {
			case on:
				if (event.state == SwitchState.held || event.state == SwitchState.releasedLong) {
					if (timeline != Timeline.onHeld) setTimeline(Timeline.onHeld);
				} else if (event.state == SwitchState.releasedShort) {
					setTimeline(Timeline.on);
				}
				break;
			case off:
				if (event.state == SwitchState.held || event.state == SwitchState.releasedLong) {
					if (timeline != Timeline.offHeld) setTimeline(Timeline.offHeld);
				}
			}

			// Disable/enable brightness.
			switch (event.button) {
			case on:
			case off:
				if (brightnessDisabled != 0) {
					if (DEBUG) debug(type + " brightness control: Clippy");
					brightnessDisabled = 0;
					reset();
				}
				break;
			case up:
			case down:
				if (DEBUG && brightnessDisabled == 0) debug(type + " brightness control: manual");
				brightnessDisabled = System.currentTimeMillis();
			}

			if (forward && timeline == Timeline.none) {
				// Forward to timelines for other lights with the same name.
				for (PhilipsHueLights otherLights : clippy.config.philipsHue) {
					if (otherLights == lights) continue;
					if (!otherLights.name.equals(lights.name)) continue;
					if (otherLights.timeline != null) otherLights.timeline.handleEvent(event, false);
				}
			}
		}

		public boolean set (float r, float g, float b, float brightness, Power power, int millis) {
			PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
			if (bridge == null) return false;
			PHBridgeResourcesCache cache = bridge.getResourceCache();

			// Find light and model, if needed.
			String name = lights.name, model = lights.model;
			PHLight light = null;
			if (name != null && !name.startsWith("group:")) {
				light = findResource(cache.getAllLights(), name, "light");
				if (light == null) {
					stop();
					return false;
				}
				model = light.getModelNumber();
			}

			// Define new light state.
			PHLightState lightState = new PHLightState();
			if (power == Power.off)
				lightState.setOn(power == Power.on);
			else {
				float[] xy = PHUtilities.calculateXYFromRGB(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255), model);
				lightState.setX(xy[0]);
				lightState.setY(xy[1]);
				if (brightness >= 0
					&& System.currentTimeMillis() - brightnessDisabled > clippy.config.philipsHueDisableMinutes * 60 * 1000) {
					lightState.setBrightness(Math.round(brightness * 254));
				}
				lightState.setTransitionTime(power == Power.on ? 0 : millis / 100);
			}

			// Apply light state, setting "on" after color if needed.
			if (name == null) {
				bridge.setLightStateForDefaultGroup(lightState);
				if (power == Power.on) {
					lightState.setOn(true);
					bridge.setLightStateForDefaultGroup(lightState);
				}
			} else if (light != null) {
				bridge.updateLightState(light, lightState, lightListener);
				if (power == Power.on) {
					lightState.setOn(true);
					bridge.updateLightState(light, lightState, lightListener);
				}
			} else {
				name = name.substring(6);
				PHGroup group = findResource(cache.getAllGroups(), name, "group");
				if (group == null) {
					stop();
					return false;
				}
				bridge.setLightStateForGroup(group.getIdentifier(), lightState, groupListener);
				if (power == Power.on) {
					lightState.setOn(true);
					bridge.setLightStateForGroup(group.getIdentifier(), lightState, groupListener);
				}
			}
			return true;
		}

		void setTimeline (Timeline timeline) {
			if (timeline == Timeline.none) {
				reset();
				if (DEBUG) debug(type + " timeline: none");
				this.times = null;
				this.timeline = timeline;
				return;
			}

			ArrayList<ColorTime> times = lights.times.get(timeline.name());
			if (times == null) return;
			reset(); // Ensure a missed update is easily fixed by changing the timeline again.
			if (times == this.times) return;
			if (DEBUG) debug(type + " timeline: " + timeline.name());
			this.times = times;
			this.timeline = timeline;

			// Clear any timelines for other lights with the same name.
			for (PhilipsHueLights otherLights : clippy.config.philipsHue) {
				if (otherLights == lights) continue;
				if (!otherLights.name.equals(lights.name)) continue;
				if (otherLights.timeline != null) otherLights.timeline.setTimeline(Timeline.none);
			}
		}

		/** @return May be null. */
		<T extends PHBridgeResource> T findResource (List<T> list, String name, String resourceType) {
			for (T resource : list)
				if (name.equals(resource.getName())) return resource;
			if (ERROR) {
				String message = type + " " + resourceType + " not found: " + name + "\n"//
					+ "Available:\n";
				for (PHBridgeResource resource : list)
					message += resource.getName() + "\n";
				error(message);
			}
			return null;
		}

		/** @return May be null. */
		SwitchEvent getSwitchEvent (PHBridge bridge) {
			try {
				if (httpGetSensor == null) {
					PHBridgeResourcesCache cache = bridge.getResourceCache();
					PHSensor sensor = findResource(cache.getAllSensors(), lights.switchName, "switch");
					if (sensor == null) {
						if (ERROR) {
							error(type + " switch not found: " + lights.switchName);
							String groups = "";
							for (PHGroup other : cache.getAllGroups())
								groups += other.getName() + "\n";
							error("Groups available:\n" + groups);
						}
						lights.switchName = null;
						return null;
					}
					String id = sensor.getIdentifier();
					httpGetSensor = new HttpGet(
						"http://" + clippy.config.philipsHueIP + "/api/" + clippy.config.philipsHueUser + "/sensors/" + id);
					if (lights.times.containsKey(Timeline.offHeld.name()))
						changeOffRule(id, bridge, 4000, 4002);
					else
						changeOffRule(id, bridge, 4002, 4000);
				}
				CloseableHttpResponse response = http.execute(httpGetSensor);
				HttpEntity entity = null;
				try {
					entity = response.getEntity();
					InputStream input = entity.getContent();
					if (input == null) return null;
					InputStreamReader reader = new InputStreamReader(input, "UTF-8");
					final StringBuilder buffer = new StringBuilder(32);
					String date = null;
					int event = -1;
					while (true) {
						if (!skipUntil(reader, '\"')) break;
						if (!collectUntil(reader, '\"', -1, buffer)) break;
						String value = buffer.toString();
						if (value.equals("buttonevent")) {
							if (!skipUntil(reader, ':')) break;
							if (!collectUntil(reader, ',', '}', buffer)) break;
							event = Integer.parseInt(buffer.toString());
						} else if (value.equals("lastupdated")) {
							if (!skipUntil(reader, '\"')) break;
							if (!collectUntil(reader, '\"', -1, buffer)) break;
							date = buffer.toString();
						} else
							continue;
						if (date != null && event != -1) return new SwitchEvent(event, date);
					}
				} finally {
					if (entity != null) EntityUtils.consumeQuietly(entity);
					response.close();
				}
			} catch (Exception ex) {
				if (ERROR) error(type + " error getting switch state: " + lights.switchName, ex);
				lights.switchName = null;
			}
			return null;
		}

		boolean skipUntil (InputStreamReader reader, int until) throws IOException {
			while (true) {
				int c = reader.read();
				if (c == -1) return false;
				if (c == until) return true;
			}
		}

		boolean collectUntil (InputStreamReader reader, int until1, int until2, StringBuilder buffer) throws IOException {
			buffer.setLength(0);
			while (true) {
				int c = reader.read();
				if (c == -1) return false;
				if (c == until1 || c == until2) return true;
				buffer.append((char)c);
			}
		}

		void changeOffRule (String id, PHBridge bridge, int from, int to) {
			for (PHRule rule : bridge.getResourceCache().getAllRules()) {
				// Ensure rule has an off action.
				boolean offAction = false;
				for (PHRuleAction action : rule.getActions()) {
					if (action.getBody().equals("{\"on\":false}")) {
						offAction = true;
						break;
					}
				}
				if (!offAction) continue;
				// If rule is for the switch's off button press, change it.
				for (PHRuleCondition condition : rule.getConditions()) {
					if (!(condition instanceof PHSimpleRuleCondition)) continue;
					PHSimpleRuleCondition simple = (PHSimpleRuleCondition)condition;
					if (!simple.getResourceIdentifier().equals(id)) continue;
					if (!simple.getAddress().equals("/sensors/" + id + "/state/buttonevent")) continue;
					if (!simple.getAttributeName().equals(PHSimpleRuleAttributeName.ATTRIBUTE_SENSOR_STATE_BUTTONEVENT)) continue;
					if (!simple.getValue().equals(from)) continue;
					if (WARN) warn(type + " switch off rule changed: " + lights.switchName);
					simple.setValue(to);
					bridge.updateRule(rule, updateRuleListener);
					return;
				}
			}
		}
	}

	static class SwitchEvent {
		final SwitchButton button;
		final SwitchState state;
		final String date;

		public SwitchEvent (int event, String date) {
			int index = ((event / 1000) % 10) - 1;
			button = (index < 0 || index >= SwitchButton.values.length) ? SwitchButton.unknown : SwitchButton.values[index];
			index = event % 1000;
			state = (index < 0 || index >= SwitchState.values.length) ? SwitchState.unknown : SwitchState.values[index];
			this.date = date;
		}
	}

	static enum SwitchButton {
		on, up, down, off, unknown;

		static final SwitchButton[] values = values();
	}

	static enum SwitchState {
		initialPress, held, releasedShort, releasedLong, unknown;

		static final SwitchState[] values = values();
	}

	static enum Timeline {
		on, onHeld, offHeld, none
	}
}
