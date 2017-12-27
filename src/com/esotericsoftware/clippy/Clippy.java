/* Copyright (c) 2014-2017, Esoteric Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.esotericsoftware.clippy.ClipDataStore.ClipConnection;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.util.MultiplexOutputStream;
import com.esotericsoftware.clippy.util.TextItem;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.minlog.Log.Logger;
import com.sun.jna.WString;

// BOZO - Favorites that always show up before others when searching.

/** @author Nathan Sweet */
public class Clippy {
	static public Clippy instance;
	static public final File logFile = new File(System.getProperty("user.home"), ".clippy/clippy.log");
	static public final File pidFile = new File(System.getProperty("user.home"), ".clippy/pid");

	final Config config;
	final Data data;
	ClipDataStore db;
	final Popup popup;
	final Menu menu;
	final Tray tray;
	final Keyboard keyboard;
	final Clipboard clipboard;
	final Screenshot screenshot;
	Upload textUpload, imageUpload, fileUpload;
	final Gamma gamma;
	final BreakWarning breakWarning;
	final Tobii tobii;

	public Clippy () {
		instance = this;

		if (Log.ERROR) {
			try {
				FileOutputStream output = new FileOutputStream(logFile);
				System.setOut(new PrintStream(new MultiplexOutputStream(System.out, output), true));
				System.setErr(new PrintStream(new MultiplexOutputStream(System.err, output), true));
			} catch (Throwable ex) {
				if (WARN) warn("Unable to write log file.", ex);
			}
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException (Thread thread, Throwable ex) {
				if (ERROR) error("Uncaught exception, exiting.", ex);
				pidFile.delete();
				Runtime.getRuntime().halt(1);
			}
		});

		config = new Config();
		data = new Data();

		if (config.imageUpload != null) {
			switch (config.imageUpload) {
			case sftp:
				imageUpload = new Upload.Sftp();
				break;
			case ftp:
				imageUpload = new Upload.Ftp();
				break;
			case imgur:
				imageUpload = new Upload.Imgur();
				break;
			}
		}
		if (config.textUpload != null) {
			switch (config.textUpload) {
			case sftp:
				textUpload = new Upload.Sftp();
				break;
			case ftp:
				textUpload = new Upload.Ftp();
				break;
			case pastebin:
				textUpload = new Upload.Pastebin();
				break;
			}
		}
		if (config.fileUpload != null) {
			switch (config.fileUpload) {
			case sftp:
				fileUpload = new Upload.Sftp();
				break;
			case ftp:
				fileUpload = new Upload.Ftp();
				break;
			}
		}

		TextItem.font = Font.decode(config.font);

		try {
			db = new ClipDataStore();
		} catch (SQLException ex) {
			if (ERROR) error("Error opening clip database.", ex);
			System.exit(0);
		}

		screenshot = new Screenshot();

		final KeyStroke toggleHotkey = KeyStroke.getKeyStroke(config.toggleHotkey);
		final KeyStroke popupHotkey = KeyStroke.getKeyStroke(config.popupHotkey);
		final KeyStroke uploadHotkey = KeyStroke.getKeyStroke(config.uploadHotkey);
		final KeyStroke screenshotHotkey = KeyStroke.getKeyStroke(config.screenshotHotkey);
		final KeyStroke screenshotAppHotkey = KeyStroke.getKeyStroke(config.screenshotAppHotkey);
		final KeyStroke screenshotRegionHotkey = KeyStroke.getKeyStroke(config.screenshotRegionHotkey);
		final KeyStroke screenshotLastRegionHotkey = KeyStroke.getKeyStroke(config.screenshotLastRegionHotkey);
		final KeyStroke tobiiPressedHotkey = config.tobiiEnabled ? KeyStroke.getKeyStroke(config.tobiiClickHotkey) : null;
		final KeyStroke tobiiPressedHotkey2 = tobiiPressedHotkey != null && tobiiPressedHotkey.getKeyCode() == KeyEvent.VK_CAPS_LOCK
			? KeyStroke.getKeyStroke("ctrl CAPS_LOCK")
			: null; // Enables ctrl + click.
		List<KeyStroke> keys = Arrays.asList(toggleHotkey, popupHotkey, uploadHotkey, screenshotHotkey, screenshotAppHotkey,
			screenshotRegionHotkey, screenshotLastRegionHotkey, tobiiPressedHotkey, tobiiPressedHotkey2);
		keyboard = new Keyboard() {
			protected void hotkey (KeyStroke keyStroke) {
				if (keyStroke.equals(toggleHotkey)) {
					if (INFO) info("Toggle gamma and break warning.");
					gamma.toggle();
					breakWarning.toggle();
				} else if (keyStroke.equals(popupHotkey))
					showPopup(keyStroke);
				else if (keyStroke.equals(uploadHotkey)) //
					upload();
				else if (keyStroke.equals(screenshotHotkey)) //
					screenshot.screen();
				else if (keyStroke.equals(screenshotAppHotkey)) //
					screenshot.app();
				else if (keyStroke.equals(screenshotRegionHotkey)) //
					screenshot.region();
				else if (keyStroke.equals(screenshotLastRegionHotkey)) //
					screenshot.lastRegion();
				else if (keyStroke.equals(tobiiPressedHotkey) || keyStroke.equals(tobiiPressedHotkey2)) //
					tobii.hotkeyPressed(tobiiPressedHotkey.getKeyCode());
			}
		};
		for (KeyStroke key : keys) {
			if (key == null) continue;
			if (key.getKeyCode() == KeyEvent.VK_CAPS_LOCK && keyboard.getCapslock())
				keyboard.sendKeyPress((byte)KeyEvent.VK_CAPS_LOCK);
			keyboard.registerHotkey(key);
		}
		keyboard.start();

		clipboard = new Clipboard(config.maxLengthToStore) {
			protected void changed () {
				String text = clipboard.getContents();
				if (text != null) store(text);
			}
		};

		popup = new Popup();

		menu = new Menu();

		tray = new Tray() {
			protected void mouseDown (POINT position, int button) {
				gamma.reset();
				gamma.wake();
				menu.populate();
				menu.setLocation(position.x, position.y - menu.getHeight());
				menu.showPopup();
			}
		};

		String text = clipboard.getContents();
		if (text != null) store(text);

		breakWarning = new BreakWarning();
		gamma = new Gamma();
		new PhilipsHue();
		tobii = new Tobii();
		new DnsMadeEasy();

		menu.addSeparator(true);
		menu.addItem(true, "Upload clipboard", new Runnable() {
			public void run () {
				menu.hidePopup();
				upload();
			}
		});
		menu.addItem(true, "Screenshot", new Runnable() {
			public void run () {
				menu.hidePopup();
				screenshot.region();
			}
		});

		if (config.pluginClass != null) {
			try {
				Class.forName(config.pluginClass).newInstance();
			} catch (Exception ex) {
				if (ERROR) error("Error initializing plugin: " + config.pluginClass, ex);
			}
		}

		try {
			FileWriter writer = new FileWriter(pidFile);
			writer.write(Integer.toString(Win.Kernel32.GetCurrentProcessId()));
			writer.close();
		} catch (IOException ex) {
			if (WARN) warn("Unable to write PID file.", ex);
		}

		if (INFO) info("Started.");
	}

	void upload () {
		String text = clipboard.getContents();
		switch (clipboard.getDataType()) {
		case text:
			Upload.uploadText(text);
			break;
		case files:
			String[] files = text.split("\n");
			if (files.length > 0) Upload.uploadFiles(files);
			break;
		}
	}

	void showPopup (KeyStroke keyStroke) {
		popup.showPopup();
	}

	void store (String text) {
		if (text.length() > config.maxLengthToStore) {
			if (TRACE) trace("Text too large to store: " + text.length());
			return;
		}
		if (TRACE) trace("Store clipboard text: " + text.trim());
		try {
			ClipConnection conn = db.getThreadConnection();
			if (!config.allowDuplicateClips) conn.removeText(text);
			int id = conn.add(text);
			popup.addRecentItem(id, text);
		} catch (SQLException ex) {
			if (ERROR) error("Error storing clipboard text.", ex);
		}
	}

	/** @param text May be null.
	 * @return The new ID for the clipboard item that was moved to last, or -1. */
	public int current (String text) {
		if (text == null) return -1;
		if (!clipboard.setContents(text)) return -1;

		int newID = -1;
		try {
			if (!popup.lockCheckbox.isSelected()) {
				newID = db.getThreadConnection().makeLast(text);
				popup.makeLast(newID, text);
			}
		} catch (SQLException ex) {
			if (ERROR) error("Error moving clipboard text to last.", ex);
		}

		return newID;
	}

	/** @param text May be null.
	 * @return The new ID for the clipboard item that was moved to last, or -1. */
	public int paste (String text) {
		int newID = current(text);
		if (newID == -1) return -1;

		// Could use SendInput or menu->Edit->Paste, or users could install the clink CMD prompt addon or use Windows 10.
		// char[] chars = new char[2048];
		// int count = GetClassName(GetForegroundWindow(), chars, chars.length);
		// if (count > 0) {
		// if (new String(chars, 0, count).equals("ConsoleWindowClass")) {
		// }
		// }

		// Reset modifier key state in case they were down.
		keyboard.sendKeyUp(VK_MENU);
		keyboard.sendKeyUp(VK_SHIFT);
		keyboard.sendKeyUp(VK_CONTROL);

		keyboard.sendKeyDown(VK_CONTROL);
		keyboard.sendKeyDown((byte)'V');
		keyboard.sendKeyUp((byte)'V');
		keyboard.sendKeyUp(VK_CONTROL);
		return newID;
	}

	public static void main (String[] args) throws Exception {
		if (FindWindow(new WString("STATIC"), new WString("com.esotericsoftware.clippy")) != null) {
			if (ERROR) error("Already running.");
			return;
		}

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (Throwable ignored) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Throwable ignored2) {
			}
		}

		setLogger(new Logger() {
			private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");

			public void log (int level, String category, String message, Throwable ex) {
				StringBuilder builder = new StringBuilder(256);

				builder.append(dateFormat.format(new Date()));

				switch (level) {
				case LEVEL_ERROR:
					builder.append(" ERROR: ");
					break;
				case LEVEL_WARN:
					builder.append("  WARN: ");
					break;
				case LEVEL_INFO:
					builder.append("  INFO: ");
					break;
				case LEVEL_DEBUG:
					builder.append(" DEBUG: ");
					break;
				case LEVEL_TRACE:
					builder.append(" TRACE: ");
					break;
				}

				if (category != null) {
					builder.append('[');
					builder.append(category);
					builder.append("] ");
				}

				builder.append(message);

				if (ex != null) {
					StringWriter writer = new StringWriter(256);
					ex.printStackTrace(new PrintWriter(writer));
					builder.append('\n');
					builder.append(writer.toString().trim());
				}

				System.out.println(builder.toString());
			}
		});

		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new Clippy();
			}
		});
	}
}
