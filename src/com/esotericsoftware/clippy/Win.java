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

import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.win32.W32APIOptions;

/** @author Nathan Sweet */
public class Win {
	static public class Kernel32 {
		static {
			Native.register(NativeLibrary.getInstance("kernel32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int GMEM_MOVEABLE = 0x2;

		static public native Pointer GlobalAlloc (int uFlags, int dwBytes);

		static public native Pointer GlobalLock (Pointer hMem);

		static public native boolean GlobalUnlock (Pointer hMem);
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
		static public final int CF_HDROP = 15;

		static public final int IMAGE_ICON = 1;
		static public final int LR_LOADFROMFILE = 0x10;

		static public final int MONITOR_DEFAULTTONEAREST = 2;

		// Window

		static public native Pointer FindWindow (WString lpClassName, WString lpWindowName);

		static public native Pointer CreateWindowEx (int dwExStyle, WString lpClassName, WString lpWindowName, int dwStyle, int x,
			int y, int nWidth, int nHeight, int hWndParent, int hMenu, int hInstance, int lpParam);

		static public native int SetWindowLongPtr (Pointer hWnd, int nIndex, StdCallCallback procedure);

		static public native int DefWindowProc (Pointer hWnd, int uMsg, Parameter wParam, Parameter lParam);

		static public native boolean GetMessage (MSG lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax);

		static public native boolean TranslateMessage (MSG lpMsg);

		static public native boolean DispatchMessage (MSG lpMsg);

		static public native int RegisterWindowMessage (WString lpString);

		static public native Pointer GetForegroundWindow ();

		static public native int GetWindowThreadProcessId (Pointer hWnd, IntByReference lpdwProcessId);

		static public native boolean ClientToScreen (Pointer hWnd, POINT lpPoint);

		static public native boolean GetWindowRect (Pointer hWnd, RECT rect);

		static public native Pointer MonitorFromWindow (Pointer hwnd, int dwFlags);

		static public native boolean GetMonitorInfo (Pointer hMonitor, MONITORINFO lpmi);

		// Keyboard

		static public native boolean RegisterHotKey (Pointer hWnd, int id, int fsModifiers, int vk);

		static public native boolean UnregisterHotKey (Pointer hWnd, int id);

		static public native void keybd_event (byte bVk, byte bScan, int dwFlags, Pointer dwExtraInfo);

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

		static public native boolean GetCursorPos (POINT point);

		static public native boolean GetGUIThreadInfo (int idThread, GUITHREADINFO lpgui);

		static public native Pointer LoadImage (Pointer hinst, WString name, int type, int xDesired, int yDesired, int load);
	}

	static public class Shell32 {
		static {
			Native.register(NativeLibrary.getInstance("shell32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int NIM_ADD = 0;
		static public final int NIM_DELETE = 2;

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
		public int union; // {UINT uTimeout; UINT uVersion;};
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

		protected List<?> getFieldOrder () {
			return Arrays.asList(new String[] {"cbSize", "hWnd", "uID", "uFlags", "uCallbackMessage", "hIcon", "szTip", "dwState",
				"dwStateMask", "szInfo", "union", "szInfoTitle", "dwInfoFlags", "guidItem", "hBalloonIcon"});
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
}
