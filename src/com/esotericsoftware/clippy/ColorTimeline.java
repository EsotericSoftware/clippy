
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;
import static java.util.Calendar.*;

import java.util.ArrayList;
import java.util.Calendar;

import com.esotericsoftware.clippy.Config.ColorTime;
import com.esotericsoftware.clippy.util.Util;

abstract public class ColorTimeline {
	final String type;
	final ArrayList<ColorTime> times;
	final float maxNoticeableChange, minSeconds, minGamma;
	final Calendar calendar = Calendar.getInstance();
	volatile boolean running = true;
	ColorTime lastFromTime;
	int delta;
	float r, g, b;

	public ColorTimeline (String type, ArrayList<ColorTime> times, float maxNoticeableChange, float minSeconds, float minGamma) {
		this.type = type;
		this.times = times;
		this.maxNoticeableChange = maxNoticeableChange;
		this.minSeconds = minSeconds;
		this.minGamma = minGamma;
	}

	public void start () {
		new Thread(type) {
			public void run () {
				while (running)
					update();
			}
		}.start();
	}

	public void stop () {
		running = false;
	}

	void update () {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int current = calendar.get(HOUR_OF_DAY) * 60 * 60 + calendar.get(MINUTE) * 60 + calendar.get(SECOND);

		ColorTime fromTime = times.get(times.size() - 1);
		ColorTime toTime = null;
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
		if (from > to) {
			to += 24 * 60 * 60;
			if (current < from) current += 24 * 60 * 60;
		}
		float duration = to - from, elapsed = current - from, remaining = duration - elapsed;
		if (duration > 0.0001f) {
			float a = elapsed / duration, ia = 1 - a;
			set(fromTime.r + dr * a, fromTime.g + dg * a, fromTime.b + db * a, fromTime.brightness + dbrightness * a, delta);
		} else
			set(toTime.r, toTime.g, toTime.b, toTime.brightness, delta);

		float maxChange = Math.max(dr, Math.abs(dg));
		maxChange = Math.max(maxChange, Math.abs(db));
		maxChange = Math.max(maxChange, Math.abs(dbrightness));
		int changes = (int)Math.floor(maxChange / maxNoticeableChange);
		float seconds = changes == 0 ? remaining : Util.clamp(duration / changes, minSeconds, remaining);
		int millis = Math.round(seconds * 1000);
		Util.sleep(millis);
		delta = fromTime == lastFromTime ? millis : 0;
		lastFromTime = fromTime;
	}

	void set (float red, float green, float blue, float brightness, int delta) {
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
		if (DEBUG) debug(type + ": " + red + ", " + green + ", " + blue + " * " + brightness);
		if (set(r, g, b, delta)) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}

	abstract public boolean set (float r, float g, float b, int delta);
}
