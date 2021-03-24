
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import com.esotericsoftware.clippy.Config.ColorTime.Power;
import com.esotericsoftware.clippy.Win.Gdi32;
import com.esotericsoftware.clippy.Win.RAMP;
import com.esotericsoftware.clippy.Win.User32;
import com.esotericsoftware.clippy.util.ColorTimeline;

public class Gamma extends ColorTimeline {
	volatile boolean disabled;
	boolean odd;
	final RAMP ramp = new RAMP();

	public Gamma () {
		super("Gamma", Clippy.instance.config.gamma == null ? null : Clippy.instance.config.gamma.getTimes(), 100,
			Integer.MAX_VALUE, 0.25f, 0);
		if (times == null || times.isEmpty()) return;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				set(1, 1, 1, 1, null, 0);
			}
		});

		start();
	}

	public boolean set (float r, float g, float b, float brightness, Power power, int millis) {
		if (disabled) return true;

		synchronized (ramp) {
			float rr = r * brightness * 256;
			float gg = g * brightness * 256;
			float bb = b * brightness * 256;
			for (int i = 1; i < 256; i++) {
				ramp.Red[i] = (char)(i * rr);
				ramp.Green[i] = (char)(i * gg);
				ramp.Blue[i] = (char)(i * bb);
			}

			// Ensure the same gamma is not set twice in a row, as some drivers will ignore it even if the gamma was changed
			// elsewhere.
			if (odd) ramp.Blue[3] = (char)(ramp.Blue[3] == 0 ? 1 : -1);
			odd = !odd;

			// SetDeviceGammaRamp will fail if the ramp has values < 128 unless this registry key is set to 256 (0x100, DWORD).
			// HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\ICM\GdiIcmGammaRange
			if (!Gdi32.SetDeviceGammaRamp(User32.GetDC(null), ramp)) {
				if (WARN) warn("Unable to set gamma ramp: " + r + ", " + g + ", " + b + " * " + brightness);
			}
		}

		return true;
	}

	public void toggle () {
		if (!disabled) set(1, 1, 1, 1, null, 0);
		disabled = !disabled;
		if (!disabled) {
			reset();
			wake();
		}
	}
}
