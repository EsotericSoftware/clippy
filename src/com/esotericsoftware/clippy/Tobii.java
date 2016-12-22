
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

// BOZO - Persist grid offsets.
// BOZO - Click + drag.
// BOZO - Animate gaze jump.

public class Tobii {
	final Clippy clippy = Clippy.instance;
	EyeX eyeX;
	volatile boolean connected;

	final Object lock = new Object();
	boolean hotkeyPressed, ignoreHotkey;
	double gazeX, gazeY, headX, headY;
	double startGazeX, startGazeY, snappedGazeX, snappedGazeY, startHeadX, startHeadY;
	int lastMouseX, lastMouseY;
	long hotkeyReleaseTime;

	Buffer snapping = new Buffer(50 * 2);
	boolean storeAdjustment;
	Dimension screen;

	final int gridRows = 20, gridColumns = Math.round(gridRows * 1.77f);
	final double[] grid = new double[gridColumns * gridRows * 2];
	final double[] gridOffset = new double[2];

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

					// If hotkey is held and gaze has moved far enough, jump the mouse to gaze.
					if (hotkeyPressed && gazeSamples.size > gazeSamples.values.length / 2) {
						x = gazeSamples.average(0, 2); // Prevent jumpiness.
						y = gazeSamples.average(1, 2);
						if (Point.distance(x, y, lastMouseX, lastMouseY) > 250) {
							gazeSamples.clear();
							setMouseToGaze();
							storeAdjustment = false;
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
					// Discard samples too far from the average, unless we've skipped enough that the head really did move.
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

					// Move mouse with head.
					if (hotkeyPressed) {
						double shiftX = (headX - startHeadX) * clippy.config.tobiiHeadSensitivityX;
						double shiftY = (startHeadY - headY) * clippy.config.tobiiHeadSensitivityY;
						setMouse(snappedGazeX + shiftX, snappedGazeY + shiftY);
					}
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

			if (System.currentTimeMillis() - hotkeyReleaseTime < 150) {
				// Double click.
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				return;
			}

			setMouseToGaze();
			storeAdjustment = true;
			hotkeyPressed = true;
		}

		threadPool.submit(new Runnable() {
			public void run () {
				while (true) {
					synchronized (lock) {
						// If mouse was moved manually, abort without clicking.
						Point mouse = MouseInfo.getPointerInfo().getLocation();
						if (lastMouseX != mouse.x || lastMouseY != mouse.y) break;

						// Shift aborts without clicking.
						if (clippy.keyboard.isKeyDown(KeyEvent.VK_SHIFT)) break;

						// Click when hotkey is released.
						if (!clippy.keyboard.isKeyDown(vk)) {
							if (storeAdjustment) {
								if (mouse.distance(snappedGazeX, snappedGazeY) > 12) {
									snapping.add(startGazeX);
									snapping.add(startGazeY);
									snapping.add(mouse.x);
									snapping.add(mouse.y);
								}
								storeOffset(startGazeX, startGazeY, mouse.x - startGazeX, mouse.y - startGazeY);
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
					if (clippy.keyboard.getCapslock()) clippy.keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
					clippy.keyboard.setCapslock(false);
				}

				hotkeyReleaseTime = System.currentTimeMillis();
			}
		});
	}

	void setMouseToGaze () {
		synchronized (lock) {
			double x = gazeX, y = gazeY;
			if (screen != null) {
				x = clamp(x, 0, screen.width);
				y = clamp(y, 0, screen.height);
			}
			startGazeX = x;
			startGazeY = y;
			startHeadX = headX;
			startHeadY = headY;

			// Snapping.
			int best = -1;
			double bestDist = Double.MAX_VALUE;
			for (int i = 0, n = snapping.size; i < n; i += 4) {
				double px = snapping.values[i], py = snapping.values[i + 1];
				double dist = Point.distance(x, y, px, py);
				if (dist < 60 && dist < bestDist) {
					bestDist = dist;
					best = i;
				}
			}
			if (best != -1) {
				x = snapping.values[best + 2];
				y = snapping.values[best + 3];
			} else {
				// Grid offset.
				double[] offset = getOffset(x, y);
				if (offset != null) {
					x += offset[0];
					y += offset[1];
				}
			}

			snappedGazeX = x;
			snappedGazeY = y;

			setMouse(x, y);
		}
	}

	void setMouse (double xd, double yd) {
		int x = (int)Math.round(xd), y = (int)Math.round(yd);
		robot.mouseMove(x, y);
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		lastMouseX = mouse.x;
		lastMouseY = mouse.y;
	}

	void storeOffset (double x, double y, double offsetX, double offsetY) {
		int column = clamp((int)Math.floor(x / screen.width * gridColumns), 0, gridColumns - 1);
		int row = clamp((int)Math.floor(y / screen.height * gridRows), 0, gridRows - 1);
		offsetX = clamp(offsetX, -150, 150);
		offsetY = clamp(offsetY, -150, 150);
		storeOffset(column, row, offsetX, offsetY);
		offsetX *= 0.66f;
		offsetY *= 0.66f;
		storeOffset(column - 1, row + 1, offsetX, offsetY);
		storeOffset(column - 1, row, offsetX, offsetY);
		storeOffset(column - 1, row - 1, offsetX, offsetY);
		storeOffset(column + 1, row + 1, offsetX, offsetY);
		storeOffset(column + 1, row, offsetX, offsetY);
		storeOffset(column + 1, row - 1, offsetX, offsetY);
		storeOffset(column, row + 1, offsetX, offsetY);
		storeOffset(column, row - 1, offsetX, offsetY);
	}

	void storeOffset (int column, int row, double offsetX, double offsetY) {
		if (column < 0 || column >= gridColumns) return;
		if (row < 0 || row >= gridRows) return;
		int index = (row * gridColumns + column) << 1;
		grid[index] = offsetX;
		grid[index + 1] = offsetY;
	}

	/** @return May be null. */
	double[] getOffset (double x, double y) {
		if (screen == null) return null;

		double column = x / screen.width * gridColumns - 0.5;
		double row = y / screen.height * gridRows - 0.5;
		if (row < 0 || row > gridRows - 1) return null;

		int column1 = clamp((int)Math.floor(column), 0, gridColumns - 1);
		int row1 = clamp((int)Math.ceil(row), 0, gridRows - 1);
		int column2 = clamp((int)Math.ceil(column), 0, gridColumns - 1);
		int row2 = clamp((int)Math.floor(row), 0, gridRows - 1);

		double pixelsPerColumn = screen.width / (double)gridColumns;
		double pixelsPerRow = screen.height / (double)gridRows;

		double xPercent = (x - (column1 + 0.5) * pixelsPerColumn) / pixelsPerColumn;
		double yPercent = (y - (row2 + 0.5) * pixelsPerRow) / pixelsPerRow;

		int index = (row1 * gridColumns + column1) * 2;
		double p1x = grid[index];
		double p1y = grid[index + 1];
		index = (row1 * gridColumns + column2) * 2;
		double p2x = grid[index];
		double p2y = grid[index + 1];
		index = (row2 * gridColumns + column2) * 2;
		double p3x = grid[index];
		double p3y = grid[index + 1];
		index = (row2 * gridColumns + column1) * 2;
		double p4x = grid[index];
		double p4y = grid[index + 1];

		double x1 = p1x + (p2x - p1x) * xPercent;
		double x2 = p4x + (p3x - p4x) * xPercent;
		gridOffset[0] = x1 * yPercent + x2 * (1 - yPercent);

		double y1 = p3y + (p2y - p3y) * yPercent;
		double y2 = p4y + (p1y - p4y) * yPercent;
		gridOffset[1] = y1 * xPercent + y2 * (1 - xPercent);

		return gridOffset;
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
