
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import com.esotericsoftware.clippy.Win.Gdi32;
import com.esotericsoftware.clippy.Win.RAMP;
import com.esotericsoftware.clippy.Win.User32;

public class Gamma extends ColorTimeline {
	public Gamma () {
		super("Gamma", Clippy.instance.config.gamma, 0.003f, 0.25f, 0.25f);
		if (times == null || times.isEmpty()) return;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				set(1, 1, 1, 0);
			}
		});

		start();
	}

	public boolean set (float r, float g, float b, int delta) {
		RAMP ramp = new RAMP();
		for (int i = 1; i < 256; i++) {
			ramp.Red[i] = (char)(i * (r * 256));
			ramp.Green[i] = (char)(i * (g * 256));
			ramp.Blue[i] = (char)(i * (b * 256));
		}
		if (!Gdi32.SetDeviceGammaRamp(User32.GetDC(null), ramp)) {
			if (DEBUG) debug("Unable to set gamma ramp: " + r + ", " + g + ", " + b);
		}
		return true;
	}
}
