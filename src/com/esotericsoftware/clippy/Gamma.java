
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;
import static java.util.Calendar.*;

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
	static private final float maxNoticeableChange = 0.005f;
	static private final int minSeconds = 1, maxSeconds = 60;
	static private final float minGamma = 0.25f;

	final Clippy clippy = Clippy.instance;
	ArrayList<GammaTime> times;
	float r, g, b;
	final Calendar calendar = Calendar.getInstance();

	public Gamma () {
		times = clippy.config.gamma;
		if (times == null || times.isEmpty()) return;

		Collections.sort(times, new Comparator<GammaTime>() {
			public int compare (GammaTime o1, GammaTime o2) {
				return o1.daySecond - o2.daySecond;
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				set(1, 1, 1, 1, true);
			}
		});

		update();
	}

	void update () {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int current = calendar.get(HOUR_OF_DAY) * 60 * 60 + calendar.get(MINUTE) * 60 + calendar.get(SECOND);

		GammaTime fromTime = times.get(times.size() - 1);
		GammaTime toTime = null;
		for (int i = 0, n = times.size() - 1; i <= n; i++) {
			toTime = times.get(i);
			if (toTime.daySecond > current) break;
			fromTime = toTime;
			if (i == n) toTime = times.get(0);
		}

		float dr = toTime.r - fromTime.r;
		float dg = toTime.g - fromTime.g;
		float db = toTime.b - fromTime.b;
		float dbrightness = toTime.brightness - fromTime.brightness;

		int from = fromTime.daySecond, to = toTime.daySecond;
		if (from > to) to += 24 * 60 * 60;
		float duration = to - from;
		if (duration > 0.0001f) {
			float a = (current - from) / duration, ia = 1 - a;
			set(fromTime.r + dr * a, fromTime.g + dg * a, fromTime.b + db * a, fromTime.brightness + dbrightness * a, false);
		} else
			set(toTime.r, toTime.g, toTime.b, toTime.brightness, false);

		float maxChange = Math.max(dr, dg);
		maxChange = Math.max(maxChange, db);
		maxChange = Math.max(maxChange, dbrightness);
		int changes = (int)Math.floor(maxChange / maxNoticeableChange);
		int seconds = changes == 0 ? maxSeconds : (int)Util.clamp(duration / changes, minSeconds, maxSeconds);
		Util.timer.schedule(new TimerTask() {
			public void run () {
				update();
			}
		}, seconds * 1000);
	}

	void set (float red, float green, float blue, float brightness, boolean force) {
		float r = red * brightness;
		float g = green * brightness;
		float b = blue * brightness;

		float total = r + g + b;
		if (Float.isNaN(total) || Float.isInfinite(total)) {
			r = 1;
			g = 1;
			b = 1;
			if (ERROR) error("Invalid gamma: " + red + ", " + green + ", " + blue + " * " + brightness);
		} else if (total < 0.001f) {
			r = minGamma / 3;
			g = minGamma / 3;
			b = minGamma / 3;
		} else if (total < minGamma) {
			r = r / total * minGamma;
			g = g / total * minGamma;
			b = b / total * minGamma;
		}

		if (Math.abs(r - this.r) <= 0.001f && Math.abs(g - this.g) <= 0.001f && Math.abs(b - this.b) <= 0.001f) return;
		if (TRACE) trace("Gamma: " + red + ", " + green + ", " + blue + " * " + brightness);
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
			if (DEBUG) debug("Unable to set gamma ramp: " + red + ", " + green + ", " + blue + " * " + brightness);
		}
	}
}
