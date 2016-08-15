
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.clippy.Config.PhilipsHueLights;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
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

public class PhilipsHue {
	final Clippy clippy = Clippy.instance;
	ProgressBar progress;

	public PhilipsHue () {
		if (!clippy.config.philipsHueEnabled) return;
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

				// PHHeartbeatManager heartbeat = PHHeartbeatManager.getInstance();
				// heartbeat.enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
				// hue.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

				if (clippy.config.philipsHueIP != null && !started) {
					started = true;
					for (PhilipsHueLights lights : clippy.config.philipsHue)
						start(lights);
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
		if (lights.timeline == null || lights.timeline.isEmpty()) return;
		new ColorTimeline("PhilipsHue", lights.timeline, 0.005f, 0.25f, 0) {
			public boolean set (float r, float g, float b, int delta) {
				PHHueSDK hue = PHHueSDK.getInstance();
				PHBridge bridge = hue.getSelectedBridge();
				if (bridge == null) return false;

				String name = lights.name, model = lights.model;
				PHBridgeResourcesCache cache = hue.getSelectedBridge().getResourceCache();
				PHLight light = null;
				if (name != null) {
					if (name.startsWith("group:"))
						name = name.substring(6);
					else {
						light = cache.getLights().get(name);
						if (light == null) {
							if (ERROR) error("Light not found: " + name);
							stop();
							return false;
						}
						model = light.getModelNumber();
					}
				}

				float[] xy = PHUtilities.calculateXYFromRGB(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255), model);
				PHLightState lightState = new PHLightState();
				lightState.setX(xy[0]);
				lightState.setY(xy[1]);
				lightState.setTransitionTime(Math.min(delta, 65535));

				if (name == null)
					bridge.setLightStateForDefaultGroup(lightState);
				else if (light != null)
					bridge.updateLightState(light, lightState, lightListener);
				else
					bridge.setLightStateForGroup(name, lightState, groupListener);
				return true;
			}
		}.start();
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
			if (TRACE) trace("Philips Hue light group updated: " + success + ", " + errors);
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
}
