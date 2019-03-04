
package com.esotericsoftware.clippy.util;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;
import static java.util.Calendar.*;

import java.util.ArrayList;
import java.util.Calendar;

import com.esotericsoftware.clippy.Config.ColorTime;
import com.esotericsoftware.clippy.Config.ColorTime.Power;

abstract public class ColorTimeline {
	static private final float[] rgb = new float[3];
	static private final float maxNoticeableChange = 1 / 255f;

	protected final String type;
	protected ArrayList<ColorTime> times;
	final Thread thread;
	final int minSleepMillis, maxSleepMillis, maxTransitionMillis;
	final float minTotal;
	final Calendar calendar = Calendar.getInstance();
	volatile boolean running = true;

	float r, g, b, brightness;
	Power power;
	ColorTime lastFromTime;

	public ColorTimeline (String type, ArrayList<ColorTime> times, int minSleepMillis, int maxSleepMillis, float minTotal,
		int maxTransitionMillis) {
		this.type = type;
		this.times = times;
		this.minSleepMillis = minSleepMillis;
		this.maxSleepMillis = maxSleepMillis;
		this.minTotal = minTotal;
		this.maxTransitionMillis = maxTransitionMillis;
		thread = new Thread(type) {
			public void run () {
				while (running)
					update();
			}
		};
	}

	public void start () {
		thread.start();
	}

	public void stop () {
		running = false;
	}

	public void reset () {
		r = -1;
		g = -1;
		b = -1;
		brightness = -1;
	}

	public void wake () {
		thread.interrupt();
	}

	protected void update () {
		if (times == null) {
			sleep(1000);
			return;
		}

		calendar.setTimeInMillis(System.currentTimeMillis());
		int current = calendar.get(HOUR_OF_DAY) * 60 * 60 * 1000 //
			+ calendar.get(MINUTE) * 60 * 1000 //
			+ calendar.get(SECOND) * 1000 //
			+ calendar.get(MILLISECOND);

		// Find from and to times which contain the current time.
		ColorTime fromTime = times.get(times.size() - 1);
		ColorTime toTime = null;
		for (int i = 0, n = times.size() - 1; i <= n; i++) {
			toTime = times.get(i);
			if (toTime.dayMillis > current) break;
			fromTime = toTime;
			if (i == n) toTime = times.get(0);
		}
		int from = fromTime.dayMillis, to = toTime.dayMillis;
		if (from > to) {
			to += 24 * 60 * 60 * 1000;
			if (current < from) current += 24 * 60 * 60 * 1000;
		}

		int duration = to - from, elapsed = current - from;
		int remaining;
		if (toTime == fromTime)
			remaining = 24 * 60 * 60 * 1000 - elapsed;
		else if (duration > 0)
			remaining = duration - elapsed;
		else
			remaining = 1;

		// Find maximal change in RGB and brightness.
		float dr = toTime.r - fromTime.r;
		float dg = toTime.g - fromTime.g;
		float db = toTime.b - fromTime.b;
		float dbrightness = toTime.brightness - fromTime.brightness;
		float maxChange = Math.max(Math.abs(dr), Math.abs(dg));
		maxChange = Math.max(maxChange, Math.abs(db));
		maxChange = Math.max(maxChange, Math.abs(dbrightness));

		// Compute the delay between changes.
		int changes = (int)Math.floor(maxChange / maxNoticeableChange);
		int millis = changes == 0 ? remaining : clamp(Math.round(duration / (float)changes), minSleepMillis, remaining);
		millis = Math.min(millis, maxSleepMillis);

		// Find target RGB and brightness.
		float tr, tg, tb, tbrightness, kelvin = 0;
		if (duration > 0) {
			float a = elapsed / (float)duration;
			if (fromTime.kelvin != 0 && toTime.kelvin != 0) {
				kelvin = fromTime.kelvin + (toTime.kelvin - fromTime.kelvin) * a;
				float[] rgb = kelvinToRGB(kelvin);
				tr = rgb[0];
				tg = rgb[1];
				tb = rgb[2];
			} else {
				tr = fromTime.r + dr * a;
				tg = fromTime.g + dg * a;
				tb = fromTime.b + db * a;
			}
			tbrightness = fromTime.brightness + dbrightness * a;
		} else {
			tr = toTime.r;
			tg = toTime.g;
			tb = toTime.b;
			tbrightness = toTime.brightness;
		}

		Power power = null;
		if (fromTime != lastFromTime) {
			power = fromTime.power;
			lastFromTime = fromTime;
		}

		set(tr, tg, tb, kelvin, tbrightness, power, Math.min(millis, maxTransitionMillis));

		sleep(millis);
	}

	void set (float r, float g, float b, float kelvin, float brightness, Power power, int millis) {
		if (Math.abs(r - this.r) <= 0.001f && Math.abs(g - this.g) <= 0.001f && Math.abs(b - this.b) <= 0.001f
			&& Math.abs(brightness - this.brightness) <= 0.001f && this.power == power) return;

		float total = r + g + b;
		if (Float.isNaN(total) || Float.isInfinite(total)) {
			r = 1;
			g = 1;
			b = 1;
			if (ERROR) {
				if (kelvin != 0)
					error(type + ", invalid color: " + kelvin + "K * " + brightness);
				else
					error(type + ", invalid color: " + r + ", " + g + ", " + b + " * " + brightness);
			}
		} else if (total < 0.001f) {
			r = minTotal / 3;
			g = minTotal / 3;
			b = minTotal / 3;
		} else if (total < minTotal) {
			r = r / total * minTotal;
			g = g / total * minTotal;
			b = b / total * minTotal;
		}

		if (DEBUG) {
			if (power == Power.off)
				debug(type + ": off");
			else if (kelvin != 0)
				debug(type + ": " + (power != null ? "on, " : "") + kelvin + "K * " + brightness);
			else
				debug(type + ": " + (power != null ? "on, " : "") + r + ", " + g + ", " + b + " * " + brightness);
		}
		if (set(r, g, b, brightness, power, millis)) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.brightness = brightness;
			this.power = power;
		}
	}

	abstract public boolean set (float r, float g, float b, float brightness, Power power, int millis);

	/** From: https://github.com/neilbartlett/color-temperature
	 * 
	 * The MIT License (MIT)
	 * 
	 * Copyright (c) 2015 Neil Bartlett
	 * 
	 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
	 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
	 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
	 * Software is furnished to do so, subject to the following conditions:
	 * 
	 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
	 * Software.
	 * 
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
	 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
	 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
	 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
	static public float[] kelvinToRGB (float kelvin) {
		float temp = kelvin / 100f;
		float r = 255;
		if (temp >= 66) {
			r = temp - 55;
			r = (float)(351.97690566805693 + 0.114206453784165 * r - 40.25366309332127 * Math.log(r));
			if (r < 0) r = 0;
			if (r > 255) r = 255;
		}
		float g;
		if (temp < 66) {
			g = temp - 2;
			g = (float)(-155.25485562709179 - 0.44596950469579133 * g + 104.49216199393888 * Math.log(g));
			if (g < 0) g = 0;
			if (g > 255) g = 255;
		} else {
			g = temp - 50;
			g = (float)(325.4494125711974 + 0.07943456536662342 * g - 28.0852963507957 * Math.log(g));
			if (g < 0) g = 0;
			if (g > 255) g = 255;
		}
		float b = 255;
		if (temp < 66) {
			if (temp <= 20)
				b = 0;
			else {
				b = temp - 10;
				b = (float)(-254.76935184120902 + 0.8274096064007395 * b + 115.67994401066147 * Math.log(b));
				if (b < 0) b = 0;
				if (b > 255) b = 255;
			}
		}
		rgb[0] = Math.round(r) / 255f;
		rgb[1] = Math.round(g) / 255f;
		rgb[2] = Math.round(b) / 255f;
		return rgb;
	}
}
