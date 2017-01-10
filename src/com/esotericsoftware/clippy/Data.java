
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;

import java.io.File;

import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonValue;

public class Data {
	static private final File dataFile = new File(System.getProperty("user.home"), ".clippy/data.json");

	public String philipsHueIP;
	public String philipsHueUser;

	public String dnsLastIP;

	public Data () {
		if (dataFile.exists()) {
			JsonValue root = new JsonReader().parse(dataFile);
			if (root != null) {
				try {
					json.readFields(this, root);
				} catch (Exception ex) {
					if (ERROR) error("Unable to read data.json.", ex);
					Runtime.getRuntime().halt(0);
				}
			}
		} else {
			// Save default config file.
			dataFile.getParentFile().mkdirs();
			try {
				writeJson(this, dataFile);
			} catch (Exception ex) {
				if (WARN) warn("Unable to write data.json.", ex);
			}
		}
	}

	public void save () {
		dataFile.getParentFile().mkdirs();
		try {
			writeJson(this, dataFile);
		} catch (Exception ex) {
			if (WARN) warn("Unable to write data.json.", ex);
		}
	}
}
