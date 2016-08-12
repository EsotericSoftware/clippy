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
import java.util.ArrayList;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonSerializable;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.JsonValue.PrettyPrintSettings;
import com.esotericsoftware.jsonbeans.OutputType;
import com.esotericsoftware.minlog.Log;

/** @author Nathan Sweet */
public class Config {
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

	public ArrayList<GammaTime> gamma;

	public Config () {
		Json json = new Json();
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);

		File configFile = new File(System.getProperty("user.home"), ".clippy/config.json");
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

	static public class GammaTime implements JsonSerializable {
		String time;
		float brightness, r, g, b;

		transient int daySecond;

		public void write (Json json) {
			json.writeFields(this);
		}

		public void read (Json json, JsonValue jsonData) {
			json.readFields(this, jsonData);

			boolean pm = time.contains("pm");
			String[] values = time.replace("am", "").replace("pm", "").split(":");
			if (values.length != 2) {
				if (ERROR) error("Invalid gamma time: " + time);
				return;
			}
			try {
				int hour = Integer.parseInt(values[0]);
				if (hour < 0 || hour >= 24) hour = 0;
				if (pm) {
					if (hour < 12) hour += 12;
				} else if (hour == 12) //
					hour = 0;
				int minute = Integer.parseInt(values[1]);
				if (minute < 0 || minute > 60) minute = 0;
				daySecond = (hour * 60 + minute) * 60;
			} catch (NumberFormatException ex) {
				if (ERROR) error("Invalid gamma time: " + time, ex);
			}
		}

		public String toString () {
			return "[" + (daySecond / 3600) + ":" + (daySecond % 3600) / 60 + ", " + r + "," + g + "," + b + "]";
		}
	}
}
