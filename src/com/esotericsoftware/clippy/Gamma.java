
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.TimerTask;

import com.esotericsoftware.clippy.Config.GammaTime;
import com.esotericsoftware.clippy.Win.Gdi32;
import com.esotericsoftware.clippy.Win.RAMP;
import com.esotericsoftware.clippy.Win.User32;
import com.esotericsoftware.clippy.util.Util;

public class Gamma {
	final Clippy clippy = Clippy.instance;
	ArrayList<GammaTime> times;
	float r, g, b;

	public Gamma () {
		times = clippy.config.gamma;
		if (times == null || times.isEmpty()) return;

		Collections.sort(times, new Comparator<GammaTime>() {
			public int compare (GammaTime o1, GammaTime o2) {
				return o1.dayMinute - o2.dayMinute;
			}
		});

		final Calendar calendar = Calendar.getInstance();
		Util.timer.schedule(new TimerTask() {
			public void run () {
				calendar.setTimeInMillis(System.currentTimeMillis());
				int current = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

				GammaTime fromTime = times.get(times.size() - 1);
				GammaTime toTime = null;
				for (int i = 0, n = times.size() - 1; i <= n; i++) {
					toTime = times.get(i);
					if (toTime.dayMinute > current) break;
					fromTime = toTime;
					if (i == n) toTime = times.get(0);
				}

				int from = fromTime.dayMinute, to = toTime.dayMinute;
				if (from > to) to += 24;
				float a = (current - from) / (float)(to - from), ia = 1 - a;
				float r = fromTime.r + (toTime.r - fromTime.r) * a;
				float g = fromTime.g + (toTime.g - fromTime.g) * a;
				float b = fromTime.b + (toTime.b - fromTime.b) * a;
				float brightness = fromTime.brightness + (toTime.brightness - fromTime.brightness) * a;
				set(r, g, b, brightness, false);
			}
		}, 0, 60 * 1000);
	}

	void set (float r, float g, float b, float brightness, boolean force) {
		if (Math.abs(r - this.r) <= 0.001f && Math.abs(g - this.g) <= 0.001f && Math.abs(b - this.b) <= 0.001f) return;
		if (TRACE) trace("Gamma: " + r + ", " + g + ", " + b);
		this.r = r;
		this.g = g;
		this.b = b;
		RAMP ramp = new RAMP();
		for (int i = 1; i < 256; i++) {
			ramp.Red[i] = (char)(i * (r * 256));
			ramp.Green[i] = (char)(i * (g * 256));
			ramp.Blue[i] = (char)(i * (b * 256));
		}
		if (!Gdi32.SetDeviceGammaRamp(User32.GetDC(null), ramp)) {
			if (DEBUG) debug("Unable to set gamma ramp.");
		}
	}
}
