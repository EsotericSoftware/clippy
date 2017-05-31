/* Copyright (c) 2014, Esoteric Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;
import static com.esotericsoftware.clippy.util.Util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.esotericsoftware.clippy.PhilipsHue.PhilipsHueTimeline;
import com.esotericsoftware.clippy.util.ColorTimeline;
import com.esotericsoftware.clippy.util.Sun;
import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonException;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonSerializable;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.JsonValue.PrettyPrintSettings;
import com.esotericsoftware.jsonbeans.OutputType;
import com.esotericsoftware.minlog.Log;

/** @author Nathan Sweet */
public class Config {
	static public final Clippy clippy = Clippy.instance;
	static private final File configFile = new File(System.getProperty("user.home"), ".clippy/config.json");

	public boolean allowDuplicateClips;
	public int maxLengthToStore = 1024 * 1024; // 1 MB
	public String log = "info";
	public String toggleHotkey = "ctrl shift alt Z";
	public String uploadHotkey = "ctrl shift V";

	public int popupWidth = 640;
	public int popupCount = 20;
	public int popupSearchCount = 60;
	public boolean popupDefaultNumbers;
	public String popupHotkey = "ctrl shift INSERT";
	public String font = "Consolas-14";

	public String screenshotHotkey = null;
	public String screenshotAppHotkey = "ctrl alt shift BACK_SLASH";
	public String screenshotRegionHotkey = "ctrl alt BACK_SLASH";
	public String screenshotLastRegionHotkey = "ctrl shift BACK_SLASH";

	public ImageUpload imageUpload = ImageUpload.imgur;
	public TextUpload textUpload = TextUpload.pastebin;
	public FileUpload fileUpload = null;
	public boolean pasteAfterUpload = true;
	public boolean uploadProgressBar = true;

	public String pastebinDevKey = "c835896db1ea7dea6dd60b1c4412f1c3";
	public String pastebinFormat = "text";
	public String pastebinPrivate = "1";
	public String pastebinExpire = "N";
	public boolean pastebinRaw = true;

	public String ftpServer;
	public int ftpPort;
	public String ftpUser;
	public String ftpPassword;
	public String ftpDir;
	public String ftpUrl;

	public int breakWarningMinutes = 55;
	public String breakStartSound = "breakStart";
	public String breakFlashSound = "breakFlash";
	public String breakEndSound = "breakEnd";
	public int breakReminderMinutes = 5;
	public int breakResetMinutes = 5;

	public HashMap<String, ColorTimesReference> colorTimelines;

	public ColorTimesReference gamma;

	public boolean philipsHueEnabled;
	public int philipsHueDisableMinutes = 90;
	public int philipsHueSwitchCheckMillis = 1000;
	public ArrayList<PhilipsHueLights> philipsHue;

	public boolean tobiiEnabled;
	public String tobiiClickHotkey = "CAPS_LOCK";
	public float tobiiHeadSensitivityX = 6;
	public float tobiiHeadSensitivityY = 8;

	public String dnsUser;
	public String dnsPassword;
	public String dnsID;
	public int dnsMinutes = 30;

	public String pluginClass;

	public Config () {
		if (configFile.exists()) {
			JsonValue root = new JsonReader().parse(configFile);
			if (root != null) {
				try {
					json.readFields(this, root);
				} catch (Exception ex) {
					if (ERROR) error("Unable to read config.json.", ex);
					Runtime.getRuntime().halt(0);
				}
			}
		} else {
			// Save default config file.
			configFile.getParentFile().mkdirs();
			try {
				writeJson(this, configFile);
			} catch (Exception ex) {
				if (WARN) warn("Unable to write config.json.", ex);
			}
		}

		try {
			if (System.getProperty("dev") != null)
				Log.TRACE();
			else
				Log.set((Integer)Log.class.getField("LEVEL_" + log.toUpperCase()).get(null));
		} catch (Exception ex) {
			if (WARN) warn("Unable to set logging level.", ex);
		}
		if (TRACE) trace("Config file: " + configFile.getAbsolutePath());
	}

	static public enum ImageUpload {
		ftp, sftp, imgur
	}

	static public enum TextUpload {
		ftp, sftp, pastebin
	}

	static public enum FileUpload {
		ftp, sftp
	}

	static public class ColorTimesReference implements JsonSerializable {
		public String name;
		public ArrayList<ColorTime> times;

		public void write (Json json) {
			throw new UnsupportedOperationException();
		}

		public void read (Json json, JsonValue value) {
			if (value.isNull()) return;
			if (value.isString())
				name = value.asString();
			else if (value.isArray())
				times = json.readValue(ArrayList.class, ColorTime.class, value);
			else
				throw new JsonException("Invalid color timeline reference: " + value);
		}

		public ArrayList<ColorTime> getTimes () {
			if (name != null) {
				ArrayList<ColorTime> times = clippy.config.colorTimelines.get(name).getTimes();
				if (times == null) throw new JsonException("Color timeline not found: " + name);
				Collections.sort(times);
				return times;
			}
			return times;
		}
	}

	static public class ColorTime implements com.esotericsoftware.jsonbeans.JsonSerializable, Comparable<ColorTime> {
		static private final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma");

		String time;
		public float brightness, r, g, b, kelvin;
		public Power power;

		public transient int dayMillis = -1, sunrise, sunset;

		public ColorTime () {
		}

		public void write (Json json) {
			throw new UnsupportedOperationException();
		}

		public void read (Json json, JsonValue jsonData) {
			json.readFields(this, jsonData);

			if (kelvin != 0) {
				float[] rgb = ColorTimeline.kelvinToRGB(kelvin);
				r = rgb[0];
				g = rgb[1];
				b = rgb[2];
			}

			String time = this.time;
			if (time == null) return;
			if (time.startsWith("sunrise") || time.startsWith("sunset")) {
				double latitude;
				int add = 0;
				try {
					String[] values = time.split(":");
					latitude = Double.parseDouble(values[1]);
					if (values[0].contains("+")) add = Integer.parseInt(values[0].split("\\+")[1]);
					if (values[0].contains("-")) add = -Integer.parseInt(values[0].split("\\-")[1]);
				} catch (Exception ex) {
					throw new RuntimeException("Invalid color time: " + time, ex);
				}
				Date date = new Date();
				Calendar calendar = Calendar.getInstance();
				if (time.startsWith("sunrise")) {
					calendar.setTime(Sun.sunrise(latitude, date.getYear(), date.getMonth(), date.getDate()));
				} else {
					calendar.setTime(Sun.sunset(latitude, date.getYear(), date.getMonth(), date.getDate()));
				}
				calendar.add(Calendar.MINUTE, add);
				time = dateFormat.format(calendar.getTime()).toLowerCase();
			}

			boolean pm = time.contains("pm");
			String[] values = time.replace("am", "").replace("pm", "").split(":");
			if (values.length != 2) throw new RuntimeException("Invalid gamma time: " + time);
			try {
				int hour = Integer.parseInt(values[0]);
				if (hour < 0 || hour >= 24) hour = 0;
				if (pm) {
					if (hour < 12) hour += 12;
				} else if (hour == 12) //
					hour = 0;
				int minute = Integer.parseInt(values[1]);
				if (minute < 0 || minute > 60) minute = 0;
				dayMillis = hour * 60 * 60 * 1000 + minute * 60 * 1000;
			} catch (NumberFormatException ex) {
				throw new RuntimeException("Invalid color time: " + time, ex);
			}
		}

		public int compareTo (ColorTime other) {
			return dayMillis - other.dayMillis;
		}

		public String toString () {
			String timeString;
			if (dayMillis < 0)
				timeString = "";
			else
				timeString = (dayMillis / 3600000) + ":" + (dayMillis % 3600000) / 60000 + ", ";
			if (kelvin != 0)
				return "[" + timeString + kelvin + "K]";
			else
				return "[" + timeString + r + "," + g + "," + b + "]";
		}

		static public enum Power {
			on, off, unchanged
		}
	}

	static public class PhilipsHueLights implements com.esotericsoftware.jsonbeans.JsonSerializable {
		/** May be null to specify all lights. Prefix with "group:" for a group name. */
		public String name;
		/** Ignored if name is the name of a light (may be null).
		 * http://www.developers.meethue.com/documentation/supported-lights */
		public String model;
		/** May be null. */
		public String switchName;
		/** May be null. */
		public HashMap<String, ColorTimesReference> times;

		public transient PhilipsHueTimeline timeline;

		public void write (Json json) {
			json.writeField(this, "name");
			json.writeField(this, "model");
			json.writeField(this, "switchName", "switch");
			try {
				json.getWriter().object("timelines");
				for (Entry<String, ColorTimesReference> entry : times.entrySet())
					json.writeValue(entry.getKey(), entry.getValue(), ArrayList.class, ColorTime.class);
				json.getWriter().pop();
			} catch (IOException ex) {
				throw new JsonException(ex);
			}
		}

		public void read (Json json, JsonValue data) {
			json.readField(this, "name", data);
			json.readField(this, "model", data);
			json.readField(this, "switchName", "switch", null, data);
			times = new HashMap();
			for (JsonValue map = data.getChild("timelines"); map != null; map = map.next)
				times.put(map.name, json.readValue(ColorTimesReference.class, map));
		}
	}
}
