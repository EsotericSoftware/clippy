
package com.esotericsoftware.clippy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.esotericsoftware.clippy.util.Util;

public class ProgressBar extends JDialog {
	static int instances;

	final JProgressBar progressBar;
	volatile float progress;
	boolean disposed;

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
		setVisible(true);
		setFocusableWindowState(false);

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
		EventQueue.invokeLater(updateProgress);
	}

	public void done () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				progressBar.setIndeterminate(false);
				progressBar.setValue(1000);
				progressBar.setString("Done!");
				progressBar.setForeground(new Color(0x4bc841));
				Util.threadPool.submit(new Runnable() {
					public void run () {
						Util.sleep(1000);
						dispose();
					}
				});
			}
		});
	}

	public void failed () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				progressBar.setIndeterminate(false);
				progressBar.setValue(1000);
				progressBar.setString("Failed!");
				progressBar.setForeground(new Color(0xff341c));
				Util.threadPool.submit(new Runnable() {
					public void run () {
						Util.sleep(20000);
						dispose();
					}
				});
			}
		});
	}

	static public void main (String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (Throwable ignored) {
		}

		final ProgressBar progressBar = new ProgressBar("1-msDk.png");
		new Thread() {
			public void run () {
				Util.sleep(2000);
				progressBar.done();
				Util.sleep(2000);
				System.exit(0);
			}
		}.start();
	}
}
