
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
	double startGazeX, startGazeY, startGazeCalibratedX, startGazeCalibratedY, startHeadX, startHeadY;
	volatile int lastMouseX, lastMouseY;
	final Object mouseLock = new Object();
	volatile int ignore;
	Buffer calibration = new Buffer(50 * 4);

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
			private Buffer samples = new Buffer(6 * 2);
			private int samplesSkipped;

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
				synchronized (eyeX) {
					gazeX = x;
					gazeY = y;
				}
			}

			protected void eyeEvent (double timestamp, boolean hasLeftEyePosition, boolean hasRightEyePosition, double leftEyeX,
				double leftEyeY, double leftEyeZ, double leftEyeXNormalized, double leftEyeYNormalized, double leftEyeZNormalized,
				double rightEyeX, double rightEyeY, double rightEyeZ, double rightEyeXNormalized, double rightEyeYNormalized,
				double rightEyeZNormalized) {
				if (hasLeftEyePosition) {
					// Discard samples too far from the average.
					if (samples.size > 0 && (Math.abs(leftEyeX - headX) > 50 || Math.abs(leftEyeY - headY) > 50)) {
						samplesSkipped++;
						if (samplesSkipped < 12) return;
						samples.clear();
					}
					samplesSkipped = 0;

					samples.add(leftEyeX);
					samples.add(leftEyeY);
					synchronized (eyeX) {
						headX = samples.average(0, 2);
						headY = samples.average(1, 2);
					}

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

		synchronized (eyeX) {
			startGazeX = gazeX;
			startGazeY = gazeY;
			startHeadX = headX;
			startHeadY = headY;
		}
		double x = startGazeX, y = startGazeY;

		// Find two closest calibration points.
		int best1 = -1, best2 = -1;
		double bestDist1 = Double.MAX_VALUE, bestDist2 = Double.MAX_VALUE;
		for (int i = 0, n = calibration.size; i < n; i += 4) {
			double px = calibration.values[i], py = calibration.values[i + 1];
			double dist = Point.distance(x, y, px, py);
			if (dist > 400) continue;
			if (dist < bestDist1) {
				bestDist2 = bestDist1;
				best2 = best1;
				bestDist1 = dist;
				best1 = i;
			} else if (dist < bestDist2) {
				bestDist2 = dist;
				best2 = i;
			}
		}

		// Bilinear interpolation to determine amount to shift gaze point.
		if (best1 != -1 && best2 != -1) {
			double p1x = calibration.values[best1];
			double p1y = calibration.values[best1 + 1];
			double v1x = calibration.values[best1 + 2];
			double v1y = calibration.values[best1 + 3];

			double p2x = calibration.values[best2];
			double p2y = calibration.values[best2 + 1];
			double v2x = calibration.values[best2 + 2];
			double v2y = calibration.values[best2 + 3];

			double shiftX = (p2x - x) / (p2x - p1x) * v1x + (x - p1x) / (p2x - p1x) * v2x;
			shiftX = v1x < v2x ? clamp(shiftX, v1x, v2x) : clamp(shiftX, v2x, v1x);
			double shiftY = (p2y - y) / (p2y - p1y) * v1y + (y - p1y) / (p2y - p1y) * v2y;
			shiftY = v1y < v2y ? clamp(shiftY, v1y, v2y) : clamp(shiftY, v2y, v1y);

			x += shiftX;
			y += shiftY;
		}
		startGazeCalibratedX = x;
		startGazeCalibratedY = y;

		setMouse(x, y);
		hotkeyPressed = true;

		threadPool.submit(new Runnable() {
			public void run () {
				while (true) {
					// If mouse was moved manually, abort without clicking.
					Point mouse;
					synchronized (mouseLock) {
						mouse = MouseInfo.getPointerInfo().getLocation();
						if (lastMouseX != mouse.x || lastMouseY != mouse.y) break;
					}

					// Shift aborts without clicking.
					if (clippy.keyboard.isKeyDown(KeyEvent.VK_SHIFT)) break;

					// Click when hotkey is released.
					if (!clippy.keyboard.isKeyDown(vk)) {
						if (mouse.distance(startGazeCalibratedX, startGazeCalibratedY) > 10) {
							calibration.add(startGazeX);
							calibration.add(startGazeY);
							calibration.add(mouse.x - startGazeCalibratedX);
							calibration.add(mouse.y - startGazeCalibratedY);
						}
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
						break;
					}

					sleep(16);
				}

				if (vk == KeyEvent.VK_CAPS_LOCK && clippy.keyboard.getCapslock()) {
					ignore++;
					if (clippy.keyboard.isKeyDown(vk))
						clippy.keyboard.sendKeyUp((byte)KeyEvent.VK_CAPS_LOCK);
					else
						clippy.keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
				}

				hotkeyPressed = false;
			}
		});
	}

	void headMoved () {
		double shiftX, shiftY;
		synchronized (eyeX) {
			shiftX = (headX - startHeadX) * clippy.config.tobiiHeadSensitivityX;
			shiftY = (startHeadY - headY) * clippy.config.tobiiHeadSensitivityY;
		}
		setMouse(startGazeCalibratedX + shiftX, startGazeCalibratedY + shiftY);
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
