/* Copyright (c) 2014, Esoteric Software
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

import static com.esotericsoftware.clippy.Win.NOTIFYICONDATA.*;
import static com.esotericsoftware.clippy.Win.Shell32.*;
import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.clippy.Win.User32_64.*;
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

/** @author Nathan Sweet */
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

				wndProc = new StdCallCallback() {
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
				};
				if (Win.is64Bit)
					SetWindowLongPtr(hwnd, GWL_WNDPROC, wndProc);
				else
					SetWindowLong(hwnd, GWL_WNDPROC, wndProc);

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
