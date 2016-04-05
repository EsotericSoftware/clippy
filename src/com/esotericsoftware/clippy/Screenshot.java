
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

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
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
	final Clippy clippy = Clippy.instance;
	private Robot robot;

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
			int x, y, x1, y1;
			boolean drag;

			{
				addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseDragged (MouseEvent e) {
						x = e.getX();
						y = e.getY();
						if (!drag) {
							drag = true;
							x1 = x;
							y1 = y;
						}
						repaint();
					}

					public void mouseMoved (MouseEvent e) {
						x = e.getX();
						y = e.getY();
						repaint();
					}
				});
				addMouseListener(new MouseAdapter() {
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
						dispose();
					}
				});
			}

			public void paint (Graphics g) {
				g.drawImage(image, 0, 0, width, height, null);
				if (drag) {
					g.setColor(new Color(0, 0, 0, 0.3f));
					g.fillRect(0, 0, width, height);
					g.drawImage(image, x1, y1, x, y, x1, y1, x, y, null);
				}
				g.setColor(Color.green);
				g.drawLine(0, y, width, y);
				g.drawLine(x, 0, x, height);
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
