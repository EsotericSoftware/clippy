
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;

import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.esotericsoftware.clippy.util.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgressBar extends JDialog {
	static int instances;

	final JProgressBar progressBar;
	volatile float progress;
	boolean disposed;
	boolean clickToDispose;

	final Runnable updateProgress = new Runnable() {
		public void run () {
			progressBar.setIndeterminate(false);
			progressBar.setMinimum(0);
			progressBar.setMaximum(1000);
			progressBar.setValue((int)(progress * 1000));
		}
	};

	public ProgressBar (String text) {
		JLabel label = new JLabel();
		label.setText("Clippy");
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		progressBar = new JProgressBar(0, 1000);
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(300, 20));
		progressBar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
		progressBar.setString(text);
		progressBar.setStringPainted(true);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
		panel.add(label);
		panel.add(progressBar, BorderLayout.EAST);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel);

		setLocationRelativeTo(null);
		setUndecorated(true);
		setAlwaysOnTop(true);
		pack();

		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Dimension size = getSize();
		synchronized (ProgressBar.class) {
			setLocation(screen.x + screen.width - size.width - 6,
				screen.y + screen.height - size.height - 6 * (instances + 1) - size.height * instances);
			instances++;
		}

		setAutoRequestFocus(false);
		setFocusableWindowState(false);

		if (clickToDispose) {
			addMouseListener(new MouseAdapter() {
				public void mouseClicked (MouseEvent e) {
					dispose();
					if (progressBar.getString().equals("Failed!")) {
						try {
							Desktop.getDesktop().open(Clippy.logFile);
						} catch (IOException ex) {
						}
					}
				}
			});
		}
	}

	public void setVisible (boolean b) {
		super.setVisible(b);
		setFocusableWindowState(false);
	}

	public void dispose () {
		synchronized (ProgressBar.class) {
			if (!disposed) {
				disposed = true;
				instances--;
			}
		}
		super.dispose();
	}

	public void setProgress (final float progress) {
		this.progress = progress;
		if (EventQueue.isDispatchThread())
			updateProgress.run();
		else
			edt(updateProgress);
	}

	public void green (String message) {
		progressBar.setString(message);
		progressBar.setForeground(new Color(0x4bc841));
	}

	public void red (String message) {
		progressBar.setString(message);
		progressBar.setForeground(new Color(0xff341c));
	}

	public void done (final String message, final int delay) {
		edt(new Runnable() {
			public void run () {
				progressBar.setIndeterminate(false);
				progressBar.setValue(1000);
				green(message);
				if (delay != -1) {
					threadPool.submit(new Runnable() {
						public void run () {
							sleep(delay);
							dispose();
						}
					});
				}
			}
		});
	}

	public void failed (final String message, final int delay) {
		edt(new Runnable() {
			public void run () {
				progressBar.setIndeterminate(false);
				progressBar.setValue(1000);
				red(message);
				if (delay != -1) {
					threadPool.submit(new Runnable() {
						public void run () {
							sleep(20_000);
							dispose();
						}
					});
				}
			}
		});
	}

	static public void main (String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (Throwable ignored) {
		}

		final ProgressBar progressBar = new ProgressBar("1-msDk.png");
		progressBar.setVisible(true);
		new Thread() {
			public void run () {
				Util.sleep(2000);
				progressBar.done("Done!", 1000);
				Util.sleep(2000);
				System.exit(0);
			}
		}.start();
	}
}
