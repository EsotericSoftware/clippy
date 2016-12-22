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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/** @author Nathan Sweet */
public class Win {
	static public boolean is64Bit = System.getProperty("os.arch").equals("amd64")
		|| System.getProperty("os.arch").equals("x86_64");

	static class CLibrary {
		public static native int strlen (Pointer p);

		public static native int wcslen (Pointer p);

		static {
			Native.register(Platform.C_LIBRARY_NAME);
		}
	}

	static public class Kernel32 {
		static {
			Native.register(NativeLibrary.getInstance("kernel32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int GMEM_MOVEABLE = 0x2;

		static public native Pointer GlobalAlloc (int uFlags, int dwBytes);

		static public native Pointer GlobalLock (Pointer hMem);

		static public native boolean GlobalUnlock (Pointer hMem);

		static public native int GetTickCount ();
	}

	static public class User32 {
		static {
			Native.register(NativeLibrary.getInstance("user32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int MOD_ALT = 0x1;
		static public final int MOD_CONTROL = 0x2;
		static public final int MOD_SHIFT = 0x4;
		static public final int MOD_WIN = 0x8;
		static public final int MOD_NOREPEAT = 0x4000;

		static public final byte VK_SHIFT = 0x10;
		static public final byte VK_CONTROL = 0x11;
		static public final byte VK_MENU = 0x12;
		static public final byte VK_LWIN = 0x5b;
		static public final byte VK_RWIN = 0x5c;
		static public final int KEYEVENTF_KEYUP = 2;

		static public final int GWL_WNDPROC = -4;

		static public final int WM_HOTKEY = 0x312;
		static public final int WM_CLIPBOARDUPDATE = 0x31D;
		static public final int WM_USER = 0x400;
		static public final int WM_LBUTTONDOWN = 0x201;
		static public final int WM_LBUTTONUP = 0x202;
		static public final int WM_RBUTTONDOWN = 0x204;
		static public final int WM_RBUTTONUP = 0x205;

		static public final int CF_TEXT = 1;
		static public final int CF_UNICODETEXT = 13;
		static public final int CF_HDROP = 15;

		static public final int IMAGE_ICON = 1;
		static public final int LR_LOADFROMFILE = 0x10;

		static public final int MONITOR_DEFAULTTONEAREST = 2;

		// Window

		static public native Pointer FindWindow (WString lpClassName, WString lpWindowName);

		static public native Pointer CreateWindowEx (int dwExStyle, WString lpClassName, WString lpWindowName, int dwStyle, int x,
			int y, int nWidth, int nHeight, int hWndParent, int hMenu, int hInstance, int lpParam);

		static public native int SetWindowLong (Pointer hWnd, int nIndex, Callback procedure);

		static public native int DefWindowProc (Pointer hWnd, int uMsg, Parameter wParam, Parameter lParam);

		static public native boolean GetMessage (MSG lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax);

		static public native boolean TranslateMessage (MSG lpMsg);

		static public native boolean DispatchMessage (MSG lpMsg);

		static public native int RegisterWindowMessage (WString lpString);

		static public native Pointer GetForegroundWindow ();

		static public native int GetWindowThreadProcessId (Pointer hWnd, IntByReference lpdwProcessId);

		static public native int GetClassName (Pointer hWnd, char[] lpClassName, int nMaxCount);

		static public native boolean ClientToScreen (Pointer hWnd, POINT lpPoint);

		static public native boolean GetWindowRect (Pointer hWnd, RECT rect);

		static public native Pointer MonitorFromWindow (Pointer hwnd, int dwFlags);

		static public native boolean GetMonitorInfo (Pointer hMonitor, MONITORINFO lpmi);

		static public native Pointer GetDC (Pointer hWnd);

		// Mouse

		static public native boolean GetCursorPos (POINT point);

		static public native boolean SetCursorPos (int x, int y);

		// Keyboard

		static public native boolean RegisterHotKey (Pointer hWnd, int id, int fsModifiers, int vk);

		static public native boolean UnregisterHotKey (Pointer hWnd, int id);

		static public native void keybd_event (byte bVk, byte bScan, int dwFlags, Pointer dwExtraInfo);

		static public native short GetAsyncKeyState (int vKey);

		static public native short GetKeyState (int nVirtKey);

		static public native boolean GetKeyboardState (byte[] lpKeyState);

		static public native boolean SetKeyboardState (byte[] lpKeyState);

		// Clipboard

		static public native boolean AddClipboardFormatListener (Pointer hWnd);

		static public native boolean OpenClipboard (Pointer hWnd);

		static public native boolean CloseClipboard (Pointer hWnd);

		static public native boolean EmptyClipboard ();

		static public native boolean IsClipboardFormatAvailable (int format);

		static public native Pointer GetClipboardData (int format);

		static public native Pointer SetClipboardData (int format, Pointer hMem);

		static public native Pointer GetClipboardOwner ();

		// Misc

		static public native boolean GetGUIThreadInfo (int idThread, GUITHREADINFO lpgui);

		static public native Pointer LoadImage (Pointer hinst, WString name, int type, int xDesired, int yDesired, int load);

		static public native boolean GetLastInputInfo (LASTINPUTINFO result);
	}

	static public class LASTINPUTINFO extends Structure {
		public int cbSize = size();
		public int dwTime;

		protected List getFieldOrder () {
			return Arrays.asList(new String[] {"cbSize", "dwTime"});
		}
	}

	static public class User32_64 {
		static {
			Native.register(NativeLibrary.getInstance("user32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public native int SetWindowLongPtr (Pointer hWnd, int nIndex, Callback procedure);
	}

	static public class Shell32 {
		static {
			Native.register(NativeLibrary.getInstance("shell32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int NIM_ADD = 0x0;
		static public final int NIM_MODIFY = 0x1;
		static public final int NIM_DELETE = 0x2;

		//

		static public native boolean Shell_NotifyIcon (int dwMessage, NOTIFYICONDATA lpdata);

		static public native int DragQueryFile (Pointer hDrop, int iFile, char[] lpszFile, int cch);
	}

	static public class MSG extends Structure {
		public Pointer hWnd;
		public int message;
		public Parameter wParam;
		public Parameter lParam;
		public int time;
		public int x;
		public int y;

		protected List getFieldOrder () {
			return Arrays.asList("hWnd", "message", "wParam", "lParam", "time", "x", "y");
		}
	}

	static public class Parameter extends IntegerType {
		public Parameter () {
			this(0);
		}

		public Parameter (long value) {
			super(Pointer.SIZE, value);
		}
	}

	static public class GUITHREADINFO extends Structure {
		public int cbSize = size();
		public int flags;
		public Pointer hwndActive;
		public Pointer hwndFocus;
		public Pointer hwndCapture;
		public Pointer hwndMenuOwner;
		public Pointer hwndMoveSize;
		public Pointer hwndCaret;
		public RECT rcCaret;

		protected List<String> getFieldOrder () {
			return Arrays.asList(new String[] {"cbSize", "flags", "hwndActive", "hwndFocus", "hwndCapture", "hwndMenuOwner",
				"hwndMoveSize", "hwndCaret", "rcCaret"});
		}
	}

	static public class RECT extends Structure {
		public int left;
		public int top;
		public int right;
		public int bottom;

		protected List getFieldOrder () {
			return Arrays.asList(new String[] {"left", "top", "right", "bottom"});
		}
	}

	static public class POINT extends Structure {
		public int x, y;

		protected List getFieldOrder () {
			return Arrays.asList(new String[] {"x", "y"});
		}
	}

	static public class NOTIFYICONDATA extends Structure {
		static public final int NIF_MESSAGE = 0x1;
		static public final int NIF_ICON = 0x2;
		static public final int NIF_TIP = 0x4;
		static public final int NIF_INFO = 0x10;

		static public final int NIIF_NONE = 0x0;
		static public final int NIIF_INFO = 0x1;
		static public final int NIIF_WARNING = 0x2;
		static public final int NIIF_ERROR = 0x3;
		static public final int NIIF_USER = 0x4;

		public int cbSize;
		public Pointer hWnd;
		public int uID;
		public int uFlags;
		public int uCallbackMessage;
		public Pointer hIcon;
		public char[] szTip = new char[64];
		public int dwState;
		public int dwStateMask;
		public char[] szInfo = new char[256];
		public int uTimeoutOrVersion; // {UINT uTimeout; UINT uVersion;};
		public char[] szInfoTitle = new char[64];
		public int dwInfoFlags;
		public int guidItem;
		public Pointer hBalloonIcon;

		{
			cbSize = size();
		}

		public void setTooltip (String s) {
			uFlags |= NIF_TIP;
			System.arraycopy(s.toCharArray(), 0, szTip, 0, Math.min(s.length(), szTip.length));
		}

		public void setBalloon (String title, String message, int millis, int niif) {
			uFlags |= NIF_INFO;
			System.arraycopy(message.toCharArray(), 0, szInfo, 0, Math.min(message.length(), szInfo.length));
			uTimeoutOrVersion = millis;
			System.arraycopy(title.toCharArray(), 0, szInfoTitle, 0, Math.min(title.length(), szInfoTitle.length));
			dwInfoFlags = niif;
		}

		protected List<?> getFieldOrder () {
			return Arrays.asList(new String[] {"cbSize", "hWnd", "uID", "uFlags", "uCallbackMessage", "hIcon", "szTip", "dwState",
				"dwStateMask", "szInfo", "uTimeoutOrVersion", "szInfoTitle", "dwInfoFlags", "guidItem", "hBalloonIcon"});
		}
	}

	static public class MONITORINFO extends Structure {
		public int cbSize = size();
		public RECT rcMonitor;
		public RECT rcWork;
		public int dwFlags;

		protected List<String> getFieldOrder () {
			return Arrays.asList("cbSize", "rcMonitor", "rcWork", "dwFlags");
		}
	}

	static public class Gdi32 {
		static {
			Native.register(NativeLibrary.getInstance("gdi32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public native boolean SetDeviceGammaRamp (Pointer hDC, RAMP lpRamp);
	}

	static public class RAMP extends Structure {
		public char[] Red = new char[256];
		public char[] Green = new char[256];
		public char[] Blue = new char[256];

		protected List<String> getFieldOrder () {
			return Arrays.asList("Red", "Green", "Blue");
		}
	}
}
