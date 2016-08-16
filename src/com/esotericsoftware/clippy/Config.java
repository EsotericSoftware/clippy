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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.esotericsoftware.clippy.util.ColorTimeline;
import com.esotericsoftware.clippy.util.Sun;
import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonSerializable;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.JsonValue.PrettyPrintSettings;
import com.esotericsoftware.jsonbeans.OutputType;
import com.esotericsoftware.minlog.Log;

/** @author Nathan Sweet */
public class Config {
	static private final File configFile = new File(System.getProperty("user.home"), ".clippy/config.json");
	static private final Json json = new Json();
	static {
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
	}

	public boolean allowDuplicateClips;
	public int maxLengthToStore = 1024 * 1024; // 1 MB
	public String log = "info";
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
	public int breakResetMinutes = 5;

	public ArrayList<ColorTime> gamma;

	public boolean philipsHueEnabled;
	public String philipsHueIP;
	public String philipsHueUser;
	public ArrayList<PhilipsHueLights> philipsHue;

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

		Comparator<ColorTime> colorTimeComparator = new Comparator<ColorTime>() {
			public int compare (ColorTime o1, ColorTime o2) {
				return o1.dayMillis - o2.dayMillis;
			}
		};
		if (gamma != null) Collections.sort(gamma, colorTimeComparator);
		if (philipsHue != null) {
			for (PhilipsHueLights lights : philipsHue)
				if (lights.timeline != null) Collections.sort(lights.timeline, colorTimeComparator);
		}

		save();
	}

	public void save () {
		configFile.getParentFile().mkdirs();
		try {
			PrettyPrintSettings pretty = new PrettyPrintSettings();
			pretty.outputType = OutputType.minimal;
			pretty.singleLineColumns = 130;
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), "UTF8");
			writer.write(json.prettyPrint(this, pretty));
			writer.close();
		} catch (Exception ex) {
			if (WARN) warn("Unable to write config.json.", ex);
		}
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

	static public class ColorTime implements JsonSerializable {
		static private final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma");

		String time;
		public float brightness, r, g, b, temp;
		public Power power;

		public transient int dayMillis, sunrise, sunset;

		public ColorTime () {
		}

		public void write (Json json) {
			json.writeField(this, "time");
			if (power == Power.on || power == Power.off) json.writeField(this, "power");
			json.writeField(this, "brightness");
			if (temp == 0) {
				json.writeField(this, "r");
				json.writeField(this, "g");
				json.writeField(this, "b");
			} else
				json.writeField(this, "temp");
		}

		public void read (Json json, JsonValue jsonData) {
			json.readFields(this, jsonData);

			if (temp != 0) {
				float[] rgb = ColorTimeline.kelvinToRGB(temp);
				r = rgb[0];
				g = rgb[1];
				b = rgb[2];
			}

			String time = this.time;
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

		public String toString () {
			if (temp != 0)
				return "[" + (dayMillis / 3600000) + ":" + (dayMillis % 3600000) / 60000 + ", " + temp + "K]";
			else
				return "[" + (dayMillis / 3600000) + ":" + (dayMillis % 3600000) / 60000 + ", " + r + "," + g + "," + b + "]";
		}

		static public enum Power {
			on, off, unchanged
		}
	}

	static public class PhilipsHueLights {
		/** May be null to specify all lights. Prefix with "group:" for a group name. */
		public String name;
		/** Ignored (may be null) if name is the name of a light.
		 * http://www.developers.meethue.com/documentation/supported-lights */
		public String model;
		public ArrayList<ColorTime> timeline;
	}
}
