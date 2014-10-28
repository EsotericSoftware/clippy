
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.SQLException;

import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.esotericsoftware.clippy.ClipDataStore.ClipConnection;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.util.MultiplexOutputStream;
import com.esotericsoftware.clippy.util.TextItem;
import com.esotericsoftware.minlog.Log;
import com.sun.jna.WString;

public class Clippy {
	static public Clippy instance;

	Config config;
	ClipDataStore db;
	Popup popup;
	Menu menu;
	Tray tray;
	Keyboard keyboard;
	Clipboard clipboard;

	public Clippy () {
		instance = this;

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException (Thread thread, Throwable ex) {
				if (ERROR) error("Uncaught exception, exiting.", ex);
				System.exit(0);
			}
		});

		config = new Config();
		TextItem.font = Font.decode(config.font);

		if (Log.ERROR) {
			try {
				FileOutputStream logFile = new FileOutputStream(new File(System.getProperty("user.home"), ".clippy/clippy.log"));
				System.setOut(new PrintStream(new MultiplexOutputStream(System.out, logFile), true));
				System.setErr(new PrintStream(new MultiplexOutputStream(System.err, logFile), true));
			} catch (Throwable ex) {
				if (WARN) warn("Unable to write log file.", ex);
			}
		}

		try {
			db = new ClipDataStore();
		} catch (SQLException ex) {
			if (ERROR) error("Error opening clip database.", ex);
			System.exit(0);
		}

		final KeyStroke popupHotkey = KeyStroke.getKeyStroke(config.popupHotkey);
		final KeyStroke plainTextHotkey = KeyStroke.getKeyStroke(config.plainTextHotkey);
		keyboard = new Keyboard() {
			protected void hotkey (KeyStroke keyStroke) {
				if (keyStroke.equals(popupHotkey))
					showPopup(keyStroke);
				else if (keyStroke.equals(plainTextHotkey)) //
					paste(clipboard.getContents());
			}
		};
		if (popupHotkey != null) keyboard.registerHotkey(popupHotkey);
		if (plainTextHotkey != null) keyboard.registerHotkey(plainTextHotkey);
		keyboard.start();

		clipboard = new Clipboard() {
			protected void changed () {
				storeClipboard();
			}
		};

		popup = new Popup();

		menu = new Menu();

		tray = new Tray() {
			protected void mouseDown (POINT position, int button) {
				menu.setLocation(position.x, position.y - menu.getHeight());
				menu.showPopup();
			}
		};

		storeClipboard();

		if (INFO) info("Started.");
	}

	void showPopup (KeyStroke keyStroke) {
		popup.showPopup();
	}

	void storeClipboard () {
		String text = clipboard.getContents();
		if (text == null) return;
		if (text.length() > config.maxLengthToStore) {
			if (TRACE) trace("Text too large to store: " + text.length());
			return;
		}
		if (TRACE) trace("Store clipboard text: " + text.trim());
		try {
			ClipConnection conn = db.getThreadConnection();
			if (!config.allowDuplicateClips) conn.removeClip(text);
			conn.addClip(text);
		} catch (SQLException ex) {
			if (ERROR) error("Error storing clipboard text.", ex);
		}
	}

	public void paste (String text) {
		if (text == null) return;
		if (!clipboard.setContents(text)) return;

		try {
			if (!popup.lockCheckbox.isSelected()) db.getThreadConnection().makeLast(text);
		} catch (SQLException ex) {
			if (ERROR) error("Error moving clipboard text to last.", ex);
		}

		keyboard.sendKeyUp(VK_SHIFT);
		keyboard.sendKeyUp(VK_CONTROL); // 10
		keyboard.sendKeyDown(VK_CONTROL); // 10
		keyboard.sendKeyDown((byte)'V'); // 50
		keyboard.sendKeyUp((byte)'V'); // 50
		keyboard.sendKeyUp(VK_CONTROL); // 10
	}

	public static void main (String[] args) throws Exception {
		if (FindWindow(new WString("STATIC"), new WString("com.esotericsoftware.clippy")) != null) {
			if (ERROR) error("Already running.");
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable ignored) {
		}

		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new Clippy();
			}
		});
	}
}
