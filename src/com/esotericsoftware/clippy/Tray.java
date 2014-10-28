
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.NOTIFYICONDATA.*;
import static com.esotericsoftware.clippy.Win.Shell32.*;
import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

import com.esotericsoftware.clippy.Win.MSG;
import com.esotericsoftware.clippy.Win.NOTIFYICONDATA;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.Win.Parameter;
import com.esotericsoftware.clippy.util.Util;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public class Tray {
	final NOTIFYICONDATA notifyIconData = new NOTIFYICONDATA();
	final POINT mousePosition = new POINT();
	StdCallCallback wndProc;

	public Tray () {
		final CyclicBarrier barrier = new CyclicBarrier(2);

		new Thread("Tray") {
			public void run () {
				if (TRACE) trace("Entered tray thread.");

				Pointer hwnd = CreateWindowEx(0, new WString("STATIC"), new WString("com.esotericsoftware.clippy"), 0, 0, 0, 0, 0, 0,
					0, 0, 0);
				if (hwnd == null) {
					if (ERROR) error("Unable to create tray window.");
					System.exit(0);
				}

				final int wmTaskbarCreated = RegisterWindowMessage(new WString("TaskbarCreated"));
				final int wmTrayIcon = WM_USER + 1;

				String iconPath;
				try {
					iconPath = Util.extractFile("icon.ico", "clippy").getAbsolutePath();
				} catch (IOException ex) {
					if (ERROR) error("Unable to read icon.", ex);
					System.exit(0);
					return;
				}
				notifyIconData.hWnd = hwnd;
				notifyIconData.uID = 1000;
				notifyIconData.uFlags = NIF_ICON | NIF_MESSAGE;
				notifyIconData.uCallbackMessage = wmTrayIcon;
				notifyIconData.hIcon = LoadImage(null, new WString(iconPath), IMAGE_ICON, 0, 0, LR_LOADFROMFILE);
				notifyIconData.setTooltip("Clippy");
				Shell_NotifyIcon(NIM_ADD, notifyIconData);

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run () {
						Shell_NotifyIcon(NIM_DELETE, notifyIconData);
					}
				});

				SetWindowLongPtr(hwnd, GWL_WNDPROC, wndProc = new StdCallCallback() {
					public int callback (Pointer hwnd, int message, Parameter wParameter, Parameter lParameter) {
						if (message == wmTrayIcon) {
							int lParam = lParameter.intValue();
							switch (lParam) {
							case WM_LBUTTONDOWN:
								if (GetCursorPos(mousePosition)) mouseDown(mousePosition, 0);
								break;
							case WM_LBUTTONUP:
								if (GetCursorPos(mousePosition)) mouseUp(mousePosition, 0);
								break;
							case WM_RBUTTONDOWN:
								if (GetCursorPos(mousePosition)) mouseDown(mousePosition, 1);
								break;
							case WM_RBUTTONUP:
								if (GetCursorPos(mousePosition)) mouseUp(mousePosition, 1);
								break;
							}
						} else if (message == wmTaskbarCreated) {
							// Add icon again if explorer crashed.
							Shell_NotifyIcon(NIM_ADD, notifyIconData);
						}
						return DefWindowProc(hwnd, message, wParameter, lParameter);
					}
				});

				try {
					barrier.await();
				} catch (Exception ignored) {
				}

				MSG msg = new MSG();
				while (GetMessage(msg, null, 0, 0)) {
					TranslateMessage(msg);
					DispatchMessage(msg);
				}

				if (TRACE) trace("Exited tray thread.");
			}
		}.start();

		try {
			barrier.await();
		} catch (Exception ignored) {
		}
	}

	protected void mouseDown (POINT position, int button) {
	}

	protected void mouseUp (POINT position, int button) {
	}
}
