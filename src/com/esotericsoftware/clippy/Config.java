
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.minlog.Log;

public class Config {
	public int popupWidth = 640;
	public int popupCount = 20;
	public int popupSearchCount = 5000;
	public String popupHotkey = "ctrl shift INSERT";
	public String plainTextHotkey = "ctrl shift V";
	public boolean allowDuplicateClips;
	public String log = "info";
	public String font = "Consolas-14";
	public int maxLengthToStore = 25000;

	public Config () {
		Json json = new Json();
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);

		File configFile = new File(System.getProperty("user.home"), ".clippy/config.json");
		if (configFile.exists()) {
			try {
				json.readFields(this, new JsonReader().parse(configFile));
			} catch (Exception ex) {
				if (WARN) warn("Unable to read config.json.", ex);
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

		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), "UTF8");
			writer.write(json.prettyPrint(this));
			writer.close();
		} catch (Exception ex) {
			if (WARN) warn("Unable to write config.json.", ex);
		}
	}
}
