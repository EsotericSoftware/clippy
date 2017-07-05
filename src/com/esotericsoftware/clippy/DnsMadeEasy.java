
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.TimerTask;

import com.esotericsoftware.clippy.util.Util;

public class DnsMadeEasy {
	final Clippy clippy = Clippy.instance;

	public DnsMadeEasy () {
		if (clippy.config.dnsUser == null || clippy.config.dnsUser.isEmpty()) return;

		Util.timer.schedule(new TimerTask() {
			public void run () {
				new Thread("DnsMadeEasy") {
					public void run () {
						try {
							update(clippy.config.dnsUser, clippy.config.dnsPassword, clippy.config.dnsID);
						} catch (IOException ex) {
							if (ERROR) error("Error updating dynamic DNS.", ex);
						}
					}
				}.start();
			}
		}, 0, clippy.config.dnsMinutes * 60 * 1000);
	}

	void update (String user, String pass, String id) throws IOException {
		String newIP;
		try {
			newIP = http("http://www.dnsmadeeasy.com/myip.jsp").trim();
		} catch (IOException ex) {
			if (WARN) warn("Error obtaining IP.", ex);
			return;
		}
		if (newIP.equals(clippy.data.dnsLastIP)) return;

		String result = http(
			"http://cp.dnsmadeeasy.com/servlet/updateip?username=" + user + "&password=" + pass + "&id=" + id + "&ip=" + newIP);
		if (result.equals("success")) {
			clippy.data.dnsLastIP = newIP;
			clippy.data.save();
			if (INFO) info("IP updated: " + newIP);
		} else {
			if (WARN) warn("IP " + newIP + " could not be updated: " + result);
		}
	}

	String http (String url) throws IOException {
		InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
		StringWriter writer = new StringWriter(128);
		char[] buffer = new char[1024];
		while (true) {
			int count = reader.read(buffer);
			if (count == -1) break;
			writer.write(buffer, 0, count);
		}
		return writer.toString();
	}
}
