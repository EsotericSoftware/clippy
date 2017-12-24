
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.esotericsoftware.clippy.tobii.EyeX;
import com.esotericsoftware.clippy.util.SharedLibraryLoader;

public class Tobii {
	static private final int mouseAnimationMillis = 100; // Zero disables animating mouse movement.
	static private final int screenLeft = 10, screenTop = 10; // Margins to keep mouse on screen, useful for bottom and right.
	static private final int screenRight = 15, screenBottom = 30;
	static private final int doubleClickTime = 350;
	static private final int doubleTapDragTime = 150;
	static private final int snapCount = 50; // Number of snap points to remember.
	static private final int snapDistance = 60; // A gaze within this distance of a snap point will use the snap point instead.
	static private final int snapStoreDistance = 12; // Head adjustments smaller than this aren't stored as a snap point.
	static private final int gridRows = 20, gridColumns = Math.round(gridRows * 1.77f);
	static private final double gridAdjacentPercent = 0.66f; // When storing an offset, the percent to store in adjacent 8 cells.
	static private final int headSampleCount = 15; // Smooth head movement.
	static private final int headMovementMax = 50; // Ignore head movements larger than this.
	static private final int headMovementMaxCount = 12; // Number of large head movements to consider the head really moved.
	static private final int gazeJumpDistance = 250; // While holding hotkey, distance from gaze to mouse needed for mouse jump.
	static private final int gazeJumpSampleCount = 100; // Delays gaze jump while holding hotkey.
	static private final int gridOffsetMax = 150; // Max grid offset to apply to a gaze point.

	final Clippy clippy = Clippy.instance;
	EyeX eyeX;
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	boolean connected, hotkeyPressed, mouseControl, storeHeadAdjustment, ignoreNextHotkey;

	double gazeX, gazeY, gazeStartX, gazeStartY, gazeSnappedX, gazeSnappedY;
	final Buffer gazeJumpSamples = new Buffer(gazeJumpSampleCount * 2);

	double headX, headY, headStartX, headStartY;
	final Buffer headSamples = new Buffer(headSampleCount * 2);
	int headSamplesSkipped;

	final Point mouse = new Point();
	int mouseLastX, mouseLastY, mouseStartX, mouseStartY, mouseEndX, mouseEndY;
	long mouseMoveLastTime, mouseDownTime;
	double mouseMoveTime;
	boolean mouseDrag;

	long hotkeyReleaseTime;
	int hotkeyReleaseX, hotkeyReleaseY;

	final Buffer snapping = new Buffer(snapCount * 2);
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
			protected void event (Event event) {
				switch (event) {
				case commitSnapshotFailed:
					break;
				case connectFailed:
					if (ERROR) error("EyeX connection failed.");
					break;
				case connectTrying:
					if (DEBUG) debug("EyeX attempting to connect...");
					synchronized (Tobii.this) {
						connected = false;
					}
					break;
				case connected:
					if (INFO) info("EyeX connected.");
					synchronized (Tobii.this) {
						connected = true;
					}
					break;
				case disconnected:
					if (INFO) info("EyeX disconnected.");
					synchronized (Tobii.this) {
						connected = false;
					}
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
				}
			}

			protected void gazeEvent (double timestamp, double x, double y) {
				setGazePosition(x, y);
			}

			protected void eyeEvent (double timestamp, boolean hasLeftEyePosition, boolean hasRightEyePosition, double leftEyeX,
				double leftEyeY, double leftEyeZ, double leftEyeXNormalized, double leftEyeYNormalized, double leftEyeZNormalized,
				double rightEyeX, double rightEyeY, double rightEyeZ, double rightEyeXNormalized, double rightEyeYNormalized,
				double rightEyeZNormalized) {
				if (hasLeftEyePosition) setHeadPosition(leftEyeX, leftEyeY);
			}
		};
		if (!eyeX.connect()) {
			if (ERROR) error("Unable to initiate EyeX connection.");
		}
	}

	synchronized void setGazePosition (double x, double y) {
		x = clamp(x, screenLeft, screen.width - 1 - screenRight);
		y = clamp(y, screenTop, screen.height - 1 - screenBottom);
		gazeX = x;
		gazeY = y;

		// If hotkey is held and gaze has moved far enough, jump the mouse to the gaze.
		gazeJumpSamples.add(x);
		gazeJumpSamples.add(y);
		if (mouseControl && gazeJumpSamples.size > gazeJumpSamples.values.length / 2) {
			x = gazeJumpSamples.average(0, 2);
			y = gazeJumpSamples.average(1, 2);
			if (Point.distance(x, y, mouseLastX, mouseLastY) > gazeJumpDistance) {
				gazeJumpSamples.clear();
				setMouseToGaze();
				storeHeadAdjustment = false;
			}
		}
	}

	synchronized void setHeadPosition (double x, double y) {
		// Discard head samples that are too far away, unless we've skipped enough that we can believe the head really did move.
		if (headSamples.size > 0 && Point.distance(x, y, headX, headY) > headMovementMax) {
			headSamplesSkipped++;
			if (headSamplesSkipped < headMovementMaxCount) return;
			headSamples.clear();
		}
		headSamplesSkipped = 0;

		headSamples.add(x);
		headSamples.add(y);
		headX = headSamples.average(0, 2);
		headY = headSamples.average(1, 2);

		// Adjust mouse position with head.
		if (mouseControl && mouseMoveTime <= 0) {
			double shiftX = (headX - headStartX) * clippy.config.tobiiHeadSensitivityX;
			double shiftY = (headStartY - headY) * clippy.config.tobiiHeadSensitivityY;
			setMousePosition(gazeSnappedX + shiftX, gazeSnappedY + shiftY, false);
		}
	}

	public synchronized void hotkeyPressed (final int vk) {
		if (ignoreNextHotkey) {
			ignoreNextHotkey = false;
			return;
		}
		if (!connected) return;
		if (hotkeyPressed) return;

		hotkeyPressed = true;
		mouseControl = true;
		storeHeadAdjustment = true;
		screen = Toolkit.getDefaultToolkit().getScreenSize();

		mouseDrag = mouseDownTime > 0;
		mouseDownTime = 0;
		if (mouseDrag)
			setMousePosition(hotkeyReleaseX, hotkeyReleaseY, false);
		else
			setMouseToGaze();

		threadPool.submit(new Runnable() {
			public void run () {
				while (hotkeyPressedTick(vk))
					sleep(8);
				hotkeyReleased(vk);
			}
		});
	}

	synchronized boolean hotkeyPressedTick (int vk) {
		// If mouse was moved manually, abort without clicking.
		getMouse(mouse);
		if (mouseLastX != mouse.x || mouseLastY != mouse.y) return false;

		// Shift aborts without clicking.
		if (clippy.keyboard.isKeyDown(KeyEvent.VK_SHIFT)) return false;

		// Click when hotkey is released.
		if (!clippy.keyboard.isKeyDown(vk)) {
			if (System.currentTimeMillis() - hotkeyReleaseTime < doubleClickTime) {
				// If a double click, use the same position as the mouse down.
				setMousePosition(hotkeyReleaseX, hotkeyReleaseY, false);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			} else if (!mouseDrag) {
				// Finish animation.
				if (mouseMoveTime > 0) {
					mouseMoveTime = 0;
					setMousePosition(mouseEndX, mouseEndY, false);
				}

				if (storeHeadAdjustment) {
					// Store head adjustment to snap or offset future gazes.
					if (mouse.distance(gazeSnappedX, gazeSnappedY) > snapStoreDistance) {
						snapping.add(gazeStartX);
						snapping.add(gazeStartY);
						snapping.add(mouse.x);
						snapping.add(mouse.y);
					}
					setGridOffset(gazeStartX, gazeStartY, mouse.x - gazeStartX, mouse.y - gazeStartY);
				}
				robot.mousePress(InputEvent.BUTTON1_MASK);
				mouseDownTime = System.currentTimeMillis();
			}
			return false;
		}

		// Animate mouse movement.
		if (mouseMoveTime > 0) {
			long time = System.currentTimeMillis();
			mouseMoveTime -= time - mouseMoveLastTime;
			mouseMoveLastTime = time;
			if (mouseMoveTime <= 0)
				setMousePosition(mouseEndX, mouseEndY, false);
			else {
				double a = mouseMoveTime / mouseAnimationMillis;
				setMousePosition(mouseEndX + (mouseStartX - mouseEndX) * a, mouseEndY + (mouseStartY - mouseEndY) * a, false);
			}
		}
		return true;
	}

	void hotkeyReleased (int vk) {
		// Stop animation.
		if (mouseMoveTime > 0) {
			mouseMoveTime = 0;
			setMousePosition(mouseEndX, mouseEndY, false);
		}

		// Stop mouse control.
		boolean mouseDown;
		synchronized (this) {
			if (mouseDrag) robot.mouseRelease(InputEvent.BUTTON1_MASK);
			mouseDown = mouseDownTime > 0;
			mouseControl = false;
		}

		// If the mouse isn't down, mouse control was aborted. Wait for the hotkey to actually be released.
		if (!mouseDown) {
			while (clippy.keyboard.isKeyDown(vk))
				sleep(8);
		}

		// Hotkey has actually been released.
		synchronized (this) {
			if (vk == KeyEvent.VK_CAPS_LOCK) { // Reset capslock state.
				if (clippy.keyboard.getCapslock()) {
					ignoreNextHotkey = true;
					clippy.keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
				}
				clippy.keyboard.setCapslock(false);
			}

			hotkeyPressed = false;
			getMouse(mouse);
			hotkeyReleaseX = mouse.x;
			hotkeyReleaseY = mouse.y;
			hotkeyReleaseTime = System.currentTimeMillis();
		}

		// Mouse up after a delay to enable dragging.
		if (mouseDown) {
			sleep(Math.max(0, doubleTapDragTime - (System.currentTimeMillis() - mouseDownTime)));
			synchronized (this) {
				if (mouseDownTime > 0) {
					mouseDownTime = 0;
					robot.mouseRelease(InputEvent.BUTTON1_MASK);
				}
			}
		}
	}

	void setMouseToGaze () {
		double x = gazeX, y = gazeY;
		gazeStartX = x;
		gazeStartY = y;
		headStartX = headX;
		headStartY = headY;

		// Snapping.
		int best = -1;
		double bestDist = Double.MAX_VALUE;
		for (int i = 0, n = snapping.size; i < n; i += 4) {
			double dist = Point.distance(x, y, snapping.values[i], snapping.values[i + 1]);
			if (dist < snapDistance && dist < bestDist) {
				bestDist = dist;
				best = i;
			}
		}
		if (best != -1) {
			x = snapping.values[best + 2];
			y = snapping.values[best + 3];
		} else {
			// Grid offset.
			double[] offset = getGridOffset(x, y);
			if (offset != null) {
				x += offset[0];
				y += offset[1];
			}
		}

		gazeSnappedX = x;
		gazeSnappedY = y;

		setMousePosition(x, y, true);
	}

	void setMousePosition (double xd, double yd, boolean animate) {
		int x = (int)Math.round(xd), y = (int)Math.round(yd);
		x = clamp(x, screenLeft, screen.width - 1 - screenRight);
		y = clamp(y, screenTop, screen.height - 1 - screenBottom);
		if (!animate || mouseAnimationMillis == 0) {
			setMouse(x, y);
			mouse.x = x;
			mouse.y = y;
			mouseLastX = x;
			mouseLastY = y;
			return;
		}
		getMouse(mouse);
		mouseLastX = mouse.x;
		mouseLastY = mouse.y;
		mouseStartX = mouse.x;
		mouseStartY = mouse.y;
		mouseEndX = x;
		mouseEndY = y;
		if (mouseMoveTime <= 0) {
			mouseMoveLastTime = System.currentTimeMillis();
			mouseMoveTime = mouseAnimationMillis;
		}
	}

	void setGridOffset (double x, double y, double offsetX, double offsetY) {
		int column = clamp((int)Math.floor(x / (screen.width - 1) * gridColumns), 0, gridColumns - 1);
		int row = clamp((int)Math.floor(y / (screen.height - 1) * gridRows), 0, gridRows - 1);
		offsetX = clamp(offsetX, -gridOffsetMax, gridOffsetMax);
		offsetY = clamp(offsetY, -gridOffsetMax, gridOffsetMax);
		setGridOffset(column, row, offsetX, offsetY);
		offsetX *= gridAdjacentPercent;
		offsetY *= gridAdjacentPercent;
		setGridOffset(column - 1, row, offsetX, offsetY);
		setGridOffset(column + 1, row, offsetX, offsetY);
		setGridOffset(column, row + 1, offsetX, offsetY);
		setGridOffset(column, row - 1, offsetX, offsetY);
		offsetX *= 0.7071f; // Diagonals get slightly less (1 / sqrt(2)).
		offsetY *= 0.7071f;
		setGridOffset(column - 1, row + 1, offsetX, offsetY);
		setGridOffset(column - 1, row - 1, offsetX, offsetY);
		setGridOffset(column + 1, row + 1, offsetX, offsetY);
		setGridOffset(column + 1, row - 1, offsetX, offsetY);
	}

	void setGridOffset (int column, int row, double offsetX, double offsetY) {
		if (column < 0 || column >= gridColumns) return;
		if (row < 0 || row >= gridRows) return;
		int index = (row * gridColumns + column) << 1;
		grid[index] = offsetX;
		grid[index + 1] = offsetY;
	}

	/** @return May be null. */
	double[] getGridOffset (double x, double y) {
		double column = x / (screen.width - 1) * gridColumns - 0.5;
		double row = y / (screen.height - 1) * gridRows - 0.5;
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

		Buffer (int size) {
			values = new double[size];
		}

		void add (double value) {
			values[index++] = value;
			int capacity = values.length;
			if (size < capacity) size = index;
			if (index >= capacity) index = 0;
		}

		double average (int offset, int stride) {
			int n = size;
			double sum = 0;
			for (int i = offset; i < n; i += stride)
				sum += values[i];
			return sum / (n >> 1);
		}

		void clear () {
			index = 0;
			size = 0;
		}
	}
}
