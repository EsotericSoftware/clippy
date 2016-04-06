
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
import java.awt.geom.Ellipse2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.esotericsoftware.clippy.Win.RECT;
import com.esotericsoftware.clippy.imgur.ImageResponse;
import com.esotericsoftware.clippy.imgur.Imgur;
import com.esotericsoftware.clippy.imgur.Upload;
import com.sun.jna.Pointer;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Screenshot {
	static final int mag = 8, magOffsetX = 8, magOffsetY = 8, magW = 280, magH = 280;
	static final int crosshair = 16;

	final Clippy clippy = Clippy.instance;
	Robot robot;
	final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4}, 0);

	public Screenshot () {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (Exception ex) {
			if (ERROR) error("Error creating robot.", ex);
		}
	}

	void upload (BufferedImage image) {
		File file = null;
		try {
			file = File.createTempFile("clippy", null);
			if (TRACE) trace("Writing screenshot file: " + file);
			ImageIO.write(image, "png", file);
		} catch (IOException ex) {
			if (ERROR) error("Error writing screenshot.", ex);
			if (file != null) file.delete();
			return;
		}

		if (TRACE) trace("Uploading to imgur: " + file);
		final Upload upload = new Upload();
		upload.image = file;
		Imgur.upload("213cecec326ed89", upload, new Callback<ImageResponse>() {
			public void success (ImageResponse imageResponse, Response response) {
				if (TRACE) trace("Upload success: " + imageResponse.data.link);
				upload.image.delete();
				clippy.clipboard.setContents(imageResponse.data.link);
				clippy.store(imageResponse.data.link);
				// Doesn't work?!
				// clippy.tray.message("Upload complete", imageResponse.data.link, 10000);
			}

			public void failure (RetrofitError ex) {
				if (ERROR) error("Error uploading to imgur: ", ex);
				upload.image.delete();
			}
		});
	}

	public void screen () {
		if (robot == null) return;
		upload(robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
	}

	public void app () {
		if (robot == null) return;
		Pointer hwnd = GetForegroundWindow();
		if (hwnd == null) {
			if (ERROR) error("Unable to get foreground window.");
			return;
		}

		RECT rect = new RECT();
		if (GetWindowRect(hwnd, rect))
			upload(robot.createScreenCapture(new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)));
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
			int lastX, lastY, x, y, x1, y1;
			int magX = magOffsetX, magY = magOffsetY;
			boolean drag, lock, lockX, robotEvent;

			{
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

						int diffX = ex - lastX, diffY = ey - lastY;

						// Shift to move slow.
						if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
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
						if (x < 0)
							x = 0;
						else if (x > width) //
							x = width;
						if (y < 0)
							y = 0;
						else if (y > height) //
							y = height;

						repaint();

						// Prevent mouse from getting stuck against screen edge.
						if (ex < 10 || width - ex < 10 || ey < 10 || height - ey < 10) {
							robot.mouseMove(width / 2, height / 2);
							robotEvent = true;
						}
					}
				});
				addMouseListener(new MouseAdapter() {
					public void mousePressed (MouseEvent e) {
						lastX = e.getX();
						lastY = e.getY();
						drag = true;
						x1 = x;
						y1 = y;
					}

					public void mouseReleased (MouseEvent e) {
						dispose();
						if (x == x1 || y == y1) return;
						if (x < x1) {
							int temp = x;
							x = x1;
							x1 = temp;
						}
						if (y < y1) {
							int temp = y;
							y = y1;
							y1 = temp;
						}
						BufferedImage subimage = new BufferedImage(x - x1, y - y1, type);
						Graphics2D g = subimage.createGraphics();
						g.drawImage(image, 0, 0, subimage.getWidth(), subimage.getHeight(), x1, y1, x, y, null);
						g.dispose();
						upload(subimage);
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
					public void windowClosed (WindowEvent e) {
						robot.mouseMove(x, y);
					}
				});
			}

			public void paint (Graphics graphics) {
				Graphics2D g = (Graphics2D)graphics;

				if (x + magX < 0)
					magX = magOffsetX;
				else if (x + magX + magW > width) //
					magX = -(magW + magOffsetX);
				if (y + magY < 0)
					magY = magOffsetY;
				else if (y + magY + magH > height) //
					magY = -(magH + magOffsetY);

				// Draw whole screen image.
				g.drawImage(image, 0, 0, width, height, null);
				if (drag) {
					g.setColor(new Color(0, 0, 0, 0.3f));
					g.fillRect(0, 0, width, height);
					g.drawImage(image, x1, y1, x, y, x1, y1, x, y, null);
				}

				// Magnified image.
				Float circle = new Ellipse2D.Float(x + magX, y + magY, magW, magH);
				g.setClip(circle);
				g.drawImage(image, //
					x + magX + mag, y + magY + mag, x + magX + magW, y + magY + magH, //
					x - magW / (mag * 2), y - magH / (mag * 2), x + magW / (mag * 2), y + magW / (mag * 2), null);
				g.setClip(null);

				// Magnified crosshair.
				int space = mag * 2;
				int cx = x + magX + magW / 2 - mag / 2;
				int cy = y + magY + space;
				int ch = magH / 2 - mag * 3 / 2 - space;
				g.setColor(Color.black);
				g.fillRect(cx, cy, mag, ch);
				g.fillRect(cx, cy + ch + mag * 3, mag, ch);
				g.setColor(Color.white);
				cx--;
				cy--;
				g.drawRect(cx, cy, mag + 1, ch + 1);
				g.drawRect(cx, cy + ch + mag * 3, mag + 1, ch + 1);
				cx = x + magX + space;
				cy = y + magY + magH / 2 - mag / 2;
				int cw = magW / 2 - mag * 3 / 2 - space;
				g.setColor(Color.black);
				g.fillRect(cx, cy, cw, mag);
				g.fillRect(cx + cw + mag * 3, cy, cw, mag);
				g.setColor(Color.white);
				cx--;
				cy--;
				g.drawRect(cx, cy, cw + 1, mag + 1);
				g.drawRect(cx + cw + mag * 3, cy, cw + 1, mag + 1);

				// Dotted guide lines.
				g.setColor(Color.black);
				g.drawLine(x - crosshair - 1, y, 0, y);
				g.drawLine(x + crosshair + 1, y, width, y);
				g.drawLine(x, y - crosshair - 1, x, 0);
				g.drawLine(x, y + crosshair + 1, x, height);
				g.setColor(Color.white);
				Stroke solid = g.getStroke();
				g.setStroke(dashed);
				g.drawLine(x - crosshair, y, 0, y);
				g.drawLine(x + crosshair, y, width, y);
				g.drawLine(x, y - crosshair, x, 0);
				g.drawLine(x, y + crosshair, x, height);
				g.setXORMode(Color.white);
				g.setColor(Color.black);
				g.fillRect(x - 1, y - crosshair, 3, crosshair * 2 + 1);
				g.fillRect(x - crosshair, y - 1, crosshair - 1, 3);
				g.fillRect(x + 2, y - 1, crosshair - 1, 3);
			}
		};
		frame.setType(Frame.Type.UTILITY);
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setCursor(toolkit.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
		frame.setSize(width, height);
		frame.setVisible(true);
		frame.validate();
	}
}
