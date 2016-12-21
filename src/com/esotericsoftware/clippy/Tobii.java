
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.esotericsoftware.clippy.tobii.EyeX;
import com.esotericsoftware.clippy.util.SharedLibraryLoader;

public class Tobii {
	final Clippy clippy = Clippy.instance;
	EyeX eyeX;
	volatile boolean connected;

	final Object lock = new Object();
	boolean hotkeyPressed, ignoreHotkey;
	double gazeX, gazeY, headX, headY;
	double startGazeX, startGazeY, snappedGazeX, snappedGazeY, startHeadX, startHeadY;
	int lastMouseX, lastMouseY;

	Buffer snapping = new Buffer(50 * 2);
	boolean storeSnap;
	Dimension screen;

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
			private Buffer headSamples = new Buffer(15 * 2);
			private Buffer gazeSamples = new Buffer(100 * 2);
			private int headSamplesSkipped;

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
				synchronized (lock) {
					gazeX = x;
					gazeY = y;
					if (screen != null) {
						x = clamp(x, 0, screen.width);
						y = clamp(y, 0, screen.height);
					}
					gazeSamples.add(x);
					gazeSamples.add(y);
					if (hotkeyPressed && gazeSamples.size > gazeSamples.values.length / 2) {
						x = gazeSamples.average(0, 2); // Prevent jumpiness.
						y = gazeSamples.average(1, 2);
						if (Point.distance(x, y, lastMouseX, lastMouseY) > 250) {
							gazeSamples.clear();
							setMouseToGaze();
							storeSnap = false;
						}
					}
				}
			}

			protected void eyeEvent (double timestamp, boolean hasLeftEyePosition, boolean hasRightEyePosition, double leftEyeX,
				double leftEyeY, double leftEyeZ, double leftEyeXNormalized, double leftEyeYNormalized, double leftEyeZNormalized,
				double rightEyeX, double rightEyeY, double rightEyeZ, double rightEyeXNormalized, double rightEyeYNormalized,
				double rightEyeZNormalized) {

				if (!hasLeftEyePosition) return;
				synchronized (lock) {
					// Discard samples too far from the average.
					if (headSamples.size > 0 && (Math.abs(leftEyeX - headX) > 50 || Math.abs(leftEyeY - headY) > 50)) {
						headSamplesSkipped++;
						if (headSamplesSkipped < 12) return;
						headSamples.clear();
					}
					headSamplesSkipped = 0;

					headSamples.add(leftEyeX);
					headSamples.add(leftEyeY);
					headX = headSamples.average(0, 2);
					headY = headSamples.average(1, 2);

					if (hotkeyPressed) headMoved();
				}
			}
		};
		if (!eyeX.connect()) {
			if (ERROR) error("Unable to initiate EyeX connection.");
		}
	}

	public void hotkeyPressed (final int vk) {
		synchronized (lock) {
			if (ignoreHotkey) {
				ignoreHotkey = false;
				return;
			}
			if (!connected) return;
			if (hotkeyPressed) return;

			screen = Toolkit.getDefaultToolkit().getScreenSize();

			setMouseToGaze();
			storeSnap = true;
			hotkeyPressed = true;
		}

		threadPool.submit(new Runnable() {
			public void run () {
				while (true) {
					// If mouse was moved manually, abort without clicking.
					synchronized (lock) {
						Point mouse = MouseInfo.getPointerInfo().getLocation();
						if (lastMouseX != mouse.x || lastMouseY != mouse.y) break;

						// Shift aborts without clicking.
						if (clippy.keyboard.isKeyDown(KeyEvent.VK_SHIFT)) break;

						// Click when hotkey is released.
						if (!clippy.keyboard.isKeyDown(vk)) {
							if (storeSnap && mouse.distance(snappedGazeX, snappedGazeY) > 12) {
								snapping.add(startGazeX);
								snapping.add(startGazeY);
								snapping.add(mouse.x);
								snapping.add(mouse.y);
							}
							robot.mousePress(InputEvent.BUTTON1_MASK);
							robot.mouseRelease(InputEvent.BUTTON1_MASK);
							break;
						}
					}
					sleep(16);
				}

				synchronized (lock) {
					hotkeyPressed = false;
				}

				if (vk == KeyEvent.VK_CAPS_LOCK) {
					while (clippy.keyboard.isKeyDown(vk))
						sleep(16);
					synchronized (lock) {
						ignoreHotkey = true;
					}
					clippy.keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
				}
			}
		});
	}

	void setMouseToGaze () {
		synchronized (lock) {
			double x = gazeX, y = gazeY;
			startGazeX = x;
			startGazeY = y;
			startHeadX = headX;
			startHeadY = headY;

			if (false) {
				int best = -1;
				double bestDist = Double.MAX_VALUE;
				for (int i = 0, n = snapping.size; i < n; i += 4) {
					double px = snapping.values[i], py = snapping.values[i + 1];
					double dist = Point.distance(x, y, px, py);
					if (dist < 75 && dist < bestDist) {
						bestDist = dist;
						best = i;
					}
				}
				if (best != -1) {
					x = snapping.values[best + 2];
					y = snapping.values[best + 3];
				}
			}
			snappedGazeX = x;
			snappedGazeY = y;

			setMouse(x, y);
		}
	}

	void headMoved () {
		double shiftX = (headX - startHeadX) * clippy.config.tobiiHeadSensitivityX;
		double shiftY = (startHeadY - headY) * clippy.config.tobiiHeadSensitivityY;
		setMouse(snappedGazeX + shiftX, snappedGazeY + shiftY);
	}

	void setMouse (double xd, double yd) {
		int x = (int)Math.round(xd), y = (int)Math.round(yd);
		robot.mouseMove(x, y);
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		lastMouseX = mouse.x;
		lastMouseY = mouse.y;
	}

	static private class Buffer {
		final double[] values;
		int index, size;

		public Buffer (int size) {
			values = new double[size];
		}

		public void add (double value) {
			values[index++] = value;
			int capacity = values.length;
			if (size < capacity) size = index;
			if (index >= capacity) index = 0;
		}

		public double average (int offset, int stride) {
			int n = size;
			double sum = 0;
			for (int i = offset; i < n; i += stride)
				sum += values[i];
			return sum / (n >> 1);
		}

		public void clear () {
			index = 0;
			size = 0;
		}
	}
}
