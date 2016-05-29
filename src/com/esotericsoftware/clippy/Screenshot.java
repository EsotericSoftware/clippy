
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

import com.esotericsoftware.clippy.Win.RECT;
import com.sun.jna.Pointer;

public class Screenshot {
	static final int mag = 8, magOffsetX = 20, magOffsetY = 20, magD = 280;
	static final int crosshair = 16;

	final Clippy clippy = Clippy.instance;
	Robot robot;
	final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4}, 0);
	int lastX1 = -1, lastY1, lastX2, lastY2, lastType;

	public Screenshot () {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (Exception ex) {
			if (ERROR) error("Error creating robot.", ex);
		}
	}

	public void screen () {
		if (robot == null) return;
		Upload.uploadImage(robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
	}

	public void app () {
		if (robot == null) return;
		Pointer hwnd = GetForegroundWindow();
		if (hwnd == null) {
			if (ERROR) error("Unable to get foreground window.");
			return;
		}

		RECT rect = new RECT();
		if (GetWindowRect(hwnd, rect)) Upload.uploadImage(
			robot.createScreenCapture(new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)));
	}

	public void region () {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		final int width = screen.width, height = screen.height;
		BufferedImage robotImage = robot.createScreenCapture(new Rectangle(screen));
		final int type = robotImage.getType();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		final VolatileImage image = gc.createCompatibleVolatileImage(robotImage.getWidth(), robotImage.getHeight(),
			Transparency.OPAQUE);
		image.getGraphics().drawImage(robotImage, 0, 0, null);

		JFrame frame = new JFrame() {
			int x1, y1;
			float x, y, lastX, lastY;
			int magX = -(magD + magOffsetX), magY = -(magD + magOffsetY);
			boolean drag, lock, lockX, robotEvent;
			final Ellipse2D.Float circle = new Ellipse2D.Float(0, 0, magD, magD);

			{
				Point mouse = MouseInfo.getPointerInfo().getLocation();
				x = mouse.x;
				y = mouse.y;
				lastX = x;
				lastY = y;
				addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseDragged (MouseEvent e) {
						mouseMoved(e);
					}

					public void mouseMoved (MouseEvent e) {
						int ex = e.getX(), ey = e.getY();
						if (robotEvent) {
							robotEvent = false;
							lastX = ex;
							lastY = ey;
							return;
						}

						float diffX = ex - lastX, diffY = ey - lastY;

						// Shift to move slow.
						boolean shift = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
						if (shift) {
							diffX /= mag;
							diffY /= mag;
						}
						if (diffX != 0) lastX = ex;
						if (diffY != 0) lastY = ey;

						// Ctrl to lock axis.
						if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
							if (!lock) {
								lock = true;
								lockX = diffY != 0;
							}
							if (lockX)
								diffX = 0;
							else
								diffY = 0;
						} else {
							lock = false;
						}

						x += diffX;
						y += diffY;
						if (shift) {
							robot.mouseMove((int)x, (int)y);
							robotEvent = true;
						}
						if (x < 0)
							x = 0;
						else if (x > width) //
							x = width;
						if (y < 0)
							y = 0;
						else if (y > height) //
							y = height;

						if (drag) {
							if (x1 < x && y1 < y) {
								magX = magOffsetX;
								magY = magOffsetY;
							} else if (x1 > x && y1 > y) {
								magX = -(magD + magOffsetX);
								magY = -(magD + magOffsetY);
							} else if (x1 < x && y1 > y) {
								magX = magOffsetX;
								magY = -(magD + magOffsetY);
							} else if (x1 > x && y1 < y) {
								magX = -(magD + magOffsetX);
								magY = magOffsetY;
							}
						}

						repaint();
					}
				});
				addMouseListener(new MouseAdapter() {
					public void mousePressed (MouseEvent e) {
						lastX = e.getX();
						lastY = e.getY();
						drag = true;
						x1 = (int)x - 1;
						y1 = (int)y - 1;
					}

					public void mouseReleased (MouseEvent e) {
						dispose();
						int x2 = (int)x, y2 = (int)y;
						if (x2 == x1 || y2 == y1) return;
						if (x2 < x1) {
							int temp = x2;
							x2 = x1;
							x1 = temp;
						}
						if (y2 < y1) {
							int temp = y2;
							y2 = y1;
							y1 = temp;
						}
						lastX1 = x1;
						lastY1 = y1;
						lastX2 = x2;
						lastY2 = y2;
						lastType = type;
						uploadRegion(image, x1, y1, x2, y2, type);
					}
				});
				addKeyListener(new KeyAdapter() {
					public void keyPressed (KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_SHIFT) return;
						if (e.getKeyCode() == KeyEvent.VK_CONTROL) return;
						dispose();
					}

					public void keyReleased (KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_CONTROL) lock = false;
					};
				});
				addWindowListener(new WindowAdapter() {
					public void windowDeactivated (WindowEvent e) {
						dispose();
					};

					public void windowClosed (WindowEvent e) {
						// robot.mouseMove(x, y);
					}
				});
			}

			public void paint (Graphics graphics) {
				Graphics2D g = (Graphics2D)graphics;

				int x = (int)this.x, y = (int)this.y;

				// Keep within screen bounds.
				if (x + magX < 0)
					magX = magOffsetX;
				else if (x + magX + magD > width) //
					magX = -(magD + magOffsetX);
				if (y + magY < 0)
					magY = magOffsetY;
				else if (y + magY + magD > height) //
					magY = -(magD + magOffsetY);

				// Draw whole screen image.
				g.drawImage(image, 0, 0, width, height, null);
				if (drag) {
					g.setColor(new Color(0, 0, 0, 0.6f));
					g.fillRect(0, 0, width, height);
					g.drawImage(image, x1, y1, x, y, x1, y1, x, y, null);
				}

				// Magnified image.
				circle.x = x + magX;
				circle.y = y + magY;
				g.setClip(circle);
				g.drawImage(image, //
					x + magX, y + magY, x + magX + magD, y + magY + magD, //
					x - (magD + mag) / (mag * 2), y - (magD + mag) / (mag * 2), x + magD / (mag * 2), y + magD / (mag * 2), null);
				g.setClip(null);

				// Magnified crosshair.
				int space = mag * 2;
				int cx = x + magX + magD / 2 - mag / 2;
				int cy = y + magY + space;
				int ch = magD / 2 - mag * 3 / 2 - space;
				g.setColor(Color.black);
				g.fillRect(cx, cy, mag, ch);
				g.fillRect(cx, cy + ch + mag * 3, mag, ch);
				g.setColor(Color.white);
				cx--;
				cy--;
				g.drawRect(cx, cy, mag + 1, ch + 1);
				g.drawRect(cx, cy + ch + mag * 3, mag + 1, ch + 1);
				cx = x + magX + space;
				cy = y + magY + magD / 2 - mag / 2;
				int cw = magD / 2 - mag * 3 / 2 - space;
				g.setColor(Color.black);
				g.fillRect(cx, cy, cw, mag);
				g.fillRect(cx + cw + mag * 3, cy, cw, mag);
				g.setColor(Color.white);
				cx--;
				cy--;
				g.drawRect(cx, cy, cw + 1, mag + 1);
				g.drawRect(cx + cw + mag * 3, cy, cw + 1, mag + 1);

				// Dotted guide lines.
				// g.setColor(Color.black);
				// g.drawLine(x - crosshair - 1, y, 0, y);
				// g.drawLine(x + crosshair + 1, y, width, y);
				// g.drawLine(x, y - crosshair - 1, x, 0);
				// g.drawLine(x, y + crosshair + 1, x, height);
				// g.setColor(Color.white);
				// Stroke solid = g.getStroke();
				// g.setStroke(dashed);
				// g.drawLine(x - crosshair, y, 0, y);
				// g.drawLine(x + crosshair, y, width, y);
				// g.drawLine(x, y - crosshair, x, 0);
				// g.drawLine(x, y + crosshair, x, height);
				// g.setXORMode(Color.white);
				// g.setColor(Color.black);
				// g.fillRect(x - 1, y - crosshair, 3, crosshair * 2 + 1);
				// g.fillRect(x - crosshair, y - 1, crosshair - 1, 3);
				// g.fillRect(x + 2, y - 1, crosshair - 1, 3);
			}
		};
		frame.setType(Frame.Type.UTILITY);
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setSize(width, height);
		frame.setVisible(true);
		frame.validate();
	}

	public void lastRegion () {
		if (lastX1 == -1) return;
		BufferedImage robotImage = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		uploadRegion(robotImage, lastX1, lastY1, lastX2, lastY2, robotImage.getType());
	}

	void uploadRegion (Image image, int x1, int y1, int x2, int y2, int type) {
		BufferedImage subimage = new BufferedImage(x2 - x1, y2 - y1, type);
		Graphics2D g = subimage.createGraphics();
		g.drawImage(image, 0, 0, subimage.getWidth(), subimage.getHeight(), x1, y1, x2, y2, null);
		g.dispose();
		Upload.uploadImage(subimage);
	}

	static public void main (String[] args) throws Exception {
		new Screenshot().region();
	}
}
