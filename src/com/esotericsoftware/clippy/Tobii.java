
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.esotericsoftware.clippy.tobii.EyeX;
import com.esotericsoftware.clippy.util.SharedLibraryLoader;

public class Tobii {
	final Clippy clippy = Clippy.instance;
	EyeX eyeX;
	volatile boolean connected, hotkeyPressed;
	volatile double gazeX, gazeY, headX, headY;
	double startGazeX, startGazeY, startHeadX, startHeadY;
	volatile int lastMouseX, lastMouseY;
	final Object mouseLock = new Object();
	volatile int ignore;

	public Tobii () {
		if (!clippy.config.tobiiEnabled) return;

		try {
			SharedLibraryLoader loader = new SharedLibraryLoader();
			loader.load("Tobii.EyeX.Client");
			loader.load("clippy");
		} catch (RuntimeException ex) {
			if (ERROR) error("Error loading Tobii EyeX native libraries.", ex);
			return;
		}

		eyeX = new EyeX("clippy") {
			private double[] samples = new double[6 * 2];
			private int sampleIndex, sampleSize, samplesSkipped;

			protected void event (Event event) {
				switch (event) {
				case commitSnapshotFailed:
					break;
				case connectFailed:
					if (ERROR) error("EyeX connection failed.");
					break;
				case connectTrying:
					if (DEBUG) debug("EyeX attempting to connect...");
					connected = false;
					break;
				case connected:
					if (INFO) info("EyeX connected.");
					connected = true;
					break;
				case disconnected:
					if (INFO) info("EyeX disconnected.");
					connected = false;
					break;
				case eventError:
					if (TRACE) info("Error getting event data.");
					break;
				case serverTooHigh:
					if (ERROR) error("EyeX server version is too high.");
					break;
				case serverTooLow:
					if (ERROR) error("EyeX server version is too low.");
					break;
				default:
					break;
				}
			}

			protected void gazeEvent (double timestamp, double x, double y) {
				gazeX = x;
				gazeY = y;
			}

			protected void eyeEvent (double timestamp, boolean hasLeftEyePosition, boolean hasRightEyePosition, double leftEyeX,
				double leftEyeY, double leftEyeZ, double leftEyeXNormalized, double leftEyeYNormalized, double leftEyeZNormalized,
				double rightEyeX, double rightEyeY, double rightEyeZ, double rightEyeXNormalized, double rightEyeYNormalized,
				double rightEyeZNormalized) {
				if (hasLeftEyePosition) {
					int capacity = samples.length;

					// Discard samples too far from the average.
					if (sampleSize > 0 && (Math.abs(leftEyeX - headX) > 50 || Math.abs(leftEyeY - headY) > 50)) {
						samplesSkipped++;
						if (samplesSkipped < capacity) return;
						sampleIndex = 0;
						sampleSize = 0;
					}
					samplesSkipped = 0;

					// Store samples in circular buffer.
					samples[sampleIndex] = leftEyeX;
					samples[sampleIndex + 1] = leftEyeY;
					sampleIndex += 2;
					if (sampleSize < capacity) sampleSize = sampleIndex;
					if (sampleIndex >= capacity) sampleIndex = 0;

					// Use sample average as head position.
					int n = sampleSize;
					float x = 0, y = 0;
					for (int i = 0; i < n; i += 2) {
						x += samples[i];
						y += samples[i + 1];
					}
					n /= 2;
					headX = x / n;
					headY = y / n;

					if (hotkeyPressed) headMoved();
				}
			}
		};
		if (!eyeX.connect()) {
			if (ERROR) error("Unable to initiate EyeX connection.");
		}
	}

	public void hotkeyPressed (final int vk) {
		if (ignore > 0) {
			ignore--;
			return;
		}
		if (!connected) return;
		if (hotkeyPressed) return;

		startGazeX = gazeX;
		startGazeY = gazeY;
		startHeadX = headX;
		startHeadY = headY;
		setMouse(gazeX, gazeY);
		hotkeyPressed = true;

		threadPool.submit(new Runnable() {
			public void run () {
				while (true) {
					// If mouse was moved manually, abort without clicking.
					synchronized (mouseLock) {
						Point mouse = MouseInfo.getPointerInfo().getLocation();
						if (lastMouseX != mouse.x || lastMouseY != mouse.y) break;
					}

					// Shift aborts without clicking.
					if (clippy.keyboard.isKeyDown(KeyEvent.VK_SHIFT)) break;

					// Click when hotkey is released.
					if (!clippy.keyboard.isKeyDown(vk)) {
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
						break;
					}

					sleep(16);
				}

				if (vk == KeyEvent.VK_CAPS_LOCK && clippy.keyboard.getCapslock()) {
					ignore++;
					clippy.keyboard.sendKeyUp((byte)KeyEvent.VK_CAPS_LOCK);
					clippy.keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
				}

				hotkeyPressed = false;
			}
		});
	}

	void headMoved () {
		double shiftX = (headX - startHeadX) * clippy.config.tobiiHeadSensitivityX;
		double shiftY = (startHeadY - headY) * clippy.config.tobiiHeadSensitivityY;
		setMouse(startGazeX + shiftX, startGazeY + shiftY);
	}

	void setMouse (double xd, double yd) {
		int x = (int)Math.round(xd), y = (int)Math.round(yd);
		synchronized (mouseLock) {
			robot.mouseMove(x, y);
			Point mouse = MouseInfo.getPointerInfo().getLocation();
			lastMouseX = mouse.x;
			lastMouseY = mouse.y;
		}
	}
}
