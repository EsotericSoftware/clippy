
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.clippy.Win.User32_64.*;
import static com.esotericsoftware.clippy.Win.Wtsapi32.*;
import static com.esotericsoftware.minlog.Log.*;

import com.esotericsoftware.clippy.Win.GUID;
import com.esotericsoftware.clippy.Win.MSG;
import com.esotericsoftware.clippy.Win.POWERBROADCAST_SETTING;
import com.esotericsoftware.clippy.Win.Parameter;
import com.esotericsoftware.clippy.Win.Wtsapi32;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/** @author Nathan Sweet */
public class SystemMonitor {
	private Pointer hwnd;
	private StdCallCallback wndProc;

	private SystemMonitor () {
		new Thread("") {
			public void run () {
				hwnd = CreateWindowEx(0, new WString("STATIC"), new WString("com.esotericsoftware.clippy.system"), 0, 0, 0, 0, 0, 0,
					0, 0, 0);

				wndProc = new StdCallCallback() {
					public int callback (Pointer hwnd, int message, Parameter wParam, Parameter lParam) {
						switch (message) {
						case WM_SYSCOMMAND: {
							switch (wParam.intValue()) {
							case SC_SCREENSAVE:
								System.out.println("SC_SCREENSAVE");
								System.out.println("    lParam: " + lParam.longValue());
							}
						}
						case WM_POWERBROADCAST: {
							System.out.println("WM_POWERBROADCAST");
							switch (wParam.intValue()) {
							case PBT_APMSUSPEND:
								suspending();
								break;
							case PBT_POWERSETTINGCHANGE:
								System.out.println("  PBT_POWERSETTINGCHANGE");
								System.out.println("    lParam: " + lParam.longValue());
								POWERBROADCAST_SETTING setting = new POWERBROADCAST_SETTING(new Pointer(lParam.longValue()));
								System.out.println("    Data[0]: " + setting.Data[0]);
								System.out.println("    DataLength: " + setting.DataLength);
								System.out.println("    PowerSetting: " + setting.PowerSetting);
								break;
							}
							return 0;
						}
						case WM_SESSION_CHANGE: {
							switch (wParam.intValue()) {
							case Wtsapi32.WTS_SESSION_LOGON:
								logon(lParam.intValue());
								break;
							case Wtsapi32.WTS_SESSION_LOGOFF:
								logoff(lParam.intValue());
								break;
							case Wtsapi32.WTS_SESSION_LOCK:
								locked(lParam.intValue());
								break;
							case Wtsapi32.WTS_SESSION_UNLOCK:
								unlocked(lParam.intValue());
								break;
							}
							return 0;
						}
						}
						return DefWindowProc(hwnd, message, wParam, lParam);
					}
				};
				if (Win.is64Bit)
					SetWindowLongPtr(hwnd, GWL_WNDPROC, wndProc);
				else
					SetWindowLong(hwnd, GWL_WNDPROC, wndProc);

				registerPower("02731015-4510-4526-99e6-e5a17ebd1aea", "power notifications1");
				registerPower("2B84C20E-AD23-4DDF-93DB-05FFBD7EFCA5", "power notifications2");
				registerPower("5d3e9a59-e9D5-4b00-a6bd-ff34ff516548", "power notifications3");
				registerPower("98a7f580-01f7-48aa-9c0f-44352c29e5C0", "power notifications4");

				if (!WTSRegisterSessionNotification(hwnd, NOTIFY_FOR_THIS_SESSION)) {
					if (WARN) warn("Unable to register for session notifications.");
				}

				MSG msg = new MSG();
				while (GetMessage(msg, null, 0, 0)) {
					TranslateMessage(msg);
					DispatchMessage(msg);
				}

				if (TRACE) trace("Exited system monitor thread.");
			}
		}.start();
	}

	void registerPower (String guid, String name) {
		if (RegisterPowerSettingNotification(hwnd, GUID.valueOf(guid), DEVICE_NOTIFY_WINDOW_HANDLE) != null) return;
		if (WARN) warn("Unable to register for " + name + ".");
	}

	protected void suspending () {
	}

	protected void locked (int sessionId) {
	}

	protected void unlocked (int sessionId) {
	}

	protected void logon (int sessionId) {
	}

	protected void logoff (int sessionId) {
	}

	public static void main (String[] args) {
		new SystemMonitor() {
			protected void suspending () {
				System.out.println("suspending");
			}

			protected void locked (int sessionId) {
				System.out.println("locked");
			}

			protected void unlocked (int sessionId) {
				System.out.println("unlocked");
			}

			protected void logon (int sessionId) {
				System.out.println("logon");
			}

			protected void logoff (int sessionId) {
				System.out.println("logoff");
			}
		};
	}
}
