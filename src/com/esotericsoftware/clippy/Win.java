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
import java.util.Objects;

import com.sun.jna.Callback;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
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

		static public native int GetCurrentProcessId ();
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
		static public final byte VK_INSERT = 0x2D;
		static public final byte VK_LWIN = 0x5b;
		static public final byte VK_RWIN = 0x5c;
		static public final int KEYEVENTF_KEYUP = 2;

		static public final int GWL_WNDPROC = -4;

		static public final int WM_CLOSE = 0x10;
		static public final int WM_SYSCOMMAND = 0x112;
		static public final int WM_HOTKEY = 0x312;
		static public final int WM_CLIPBOARDUPDATE = 0x31D;
		static public final int WM_USER = 0x400;
		static public final int WM_LBUTTONDOWN = 0x201;
		static public final int WM_LBUTTONUP = 0x202;
		static public final int WM_RBUTTONDOWN = 0x204;
		static public final int WM_RBUTTONUP = 0x205;

		static public final int WM_POWERBROADCAST = 536;
		static public final int WM_SESSION_CHANGE = 689;

		static public final int PBT_APMPOWERSTATUSCHANGE = 0xA;
		static public final int PBT_APMRESUMESUSPEND = 0x7;
		static public final int PBT_APMSUSPEND = 0x4;
		static public final int PBT_POWERSETTINGCHANGE = 0x8013;

		static public final int SC_SCREENSAVE = 0xF140;

		static public final int DEVICE_NOTIFY_WINDOW_HANDLE = 0;

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

		static public native Pointer MonitorFromWindow (Pointer hWnd, int dwFlags);

		static public native boolean GetMonitorInfo (Pointer hMonitor, MONITORINFO lpmi);

		static public native Pointer GetDC (Pointer hWnd);

		static public native void PostQuitMessage (int exitCode);

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

		static public native boolean LockWorkStation ();

		static public native Pointer RegisterPowerSettingNotification (Pointer hRecipient, GUID PowerSettingGuid, int Flags);
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
			super(Native.POINTER_SIZE, value);
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
		public char[] szTip = new char[128];
		public int dwState;
		public int dwStateMask;
		public char[] szInfo = new char[256];
		public int uTimeoutOrVersion; // {UINT uTimeout; UINT uVersion;};
		public char[] szInfoTitle = new char[64];
		public int dwInfoFlags;

		{
			cbSize = size();
		}

		public void setTooltip (String s) {
			uFlags |= NIF_TIP;

			System.arraycopy(s.toCharArray(), 0, szTip, 0, Math.min(s.length(), szTip.length));
			szTip[s.length()] = '\0';
		}

		public void setBalloon (String title, String message, int millis, int niif) {
			uFlags |= NIF_INFO;

			System.arraycopy(message.toCharArray(), 0, szInfo, 0, Math.min(message.length(), szInfo.length));
			szInfo[message.length()] = '\0';

			uTimeoutOrVersion = millis;

			System.arraycopy(title.toCharArray(), 0, szInfoTitle, 0, Math.min(title.length(), szInfoTitle.length));
			szInfoTitle[title.length()] = '\0';

			dwInfoFlags = niif;
		}

		protected List<String> getFieldOrder () {
			return Arrays.asList(new String[] {"cbSize", "hWnd", "uID", "uFlags", "uCallbackMessage", "hIcon", "szTip", "dwState",
				"dwStateMask", "szInfo", "uTimeoutOrVersion", "szInfoTitle", "dwInfoFlags"});
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

	static public class Wtsapi32 {
		static {
			Native.register(NativeLibrary.getInstance("wtsapi32", W32APIOptions.DEFAULT_OPTIONS));
		}

		static public final int WTS_CURRENT_SERVER_HANDLE = 0;

		static public final int NOTIFY_FOR_THIS_SESSION = 0;

		static public final int WTS_SESSION_LOGON = 0x5;
		static public final int WTS_SESSION_LOGOFF = 0x6;
		static public final int WTS_SESSION_LOCK = 0x7;
		static public final int WTS_SESSION_UNLOCK = 0x8;
		static public final int WTS_SESSION_REMOTE_CONTROL = 0x9;

		static public native boolean WTSEnumerateProcesses (int hServer, Pointer Reserved, int Version,
			PointerByReference ppProcessInfo, IntByReference pCount);

		static public native void WTSFreeMemory (Pointer pMemory);

		static public native boolean WTSRegisterSessionNotification (Pointer hWnd, int dwFlags);
	}

	static public class WTS_PROCESS_INFO extends Structure {
		public int SessionId;
		public int ProcessId;
		public WString pProcessName;
		public Pointer pUserSid;

		private WTS_PROCESS_INFO () {
		}

		public WTS_PROCESS_INFO (Pointer pointer) {
			super(pointer);
			read();
		}

		protected List<String> getFieldOrder () {
			return Arrays.asList("SessionId", "ProcessId", "pProcessName", "pUserSid");
		}

		static public final int size = new WTS_PROCESS_INFO().size();
	}

	static public class GUID extends Structure {
		static private final char[] hexChars = "0123456789ABCDEF".toCharArray();

		public int Data1;
		public short Data2;
		public short Data3;
		public byte[] Data4 = new byte[8];

		public GUID () {
		}

		public GUID (Pointer pointer) {
			super(pointer);
			read();
		}

		protected List<String> getFieldOrder () {
			return Arrays.asList("Data1", "Data2", "Data3", "Data4");
		}

		public boolean equals (Object o) {
			if (o == null) return false;
			if (this == o) return true;
			if (getClass() != o.getClass()) return false;
			GUID other = (GUID)o;
			return Data1 == other.Data1 && Data2 == other.Data2 && Data3 == other.Data3 && Arrays.equals(Data4, other.Data4);
		}

		public int hashCode () {
			return Objects.hash(Data1, Data2, Data3) * 31 + Arrays.hashCode(Data4);
		}

		public byte[] toBytes () {
			byte[] guid = new byte[16];
			guid[0] = (byte)(Data1 >> 24);
			guid[1] = (byte)(Data1 >> 16);
			guid[2] = (byte)(Data1 >> 8);
			guid[3] = (byte)Data1;
			guid[4] = (byte)(Data2 >> 8);
			guid[5] = (byte)Data2;
			guid[6] = (byte)(Data3 >> 8);
			guid[7] = (byte)Data3;
			System.arraycopy(Data4, 0, guid, 8, 8);
			return guid;
		}

		public String toString () {
			byte[] bGuid = toBytes();
			StringBuilder buffer = new StringBuilder(2 * bGuid.length);
			buffer.append("{");
			for (int i = 0; i < bGuid.length; i++) {
				char ch1 = hexChars[(bGuid[i] & 0xF0) >> 4];
				char ch2 = hexChars[bGuid[i] & 0x0F];
				buffer.append(ch1).append(ch2);
				if ((i == 3) || (i == 5) || (i == 7) || (i == 9)) buffer.append("-");
			}
			buffer.append("}");
			return buffer.toString();
		}

		static public GUID valueOf (String guid) {
			if (guid.length() > 38) throw new IllegalArgumentException("Invalid GUID length: " + guid.length());

			char[] _cguid = guid.toCharArray();
			char[] _cnewguid = new char[32];
			for (int i = 0, y = 0; i < _cguid.length; i++)
				if ((_cguid[i] != '{') && (_cguid[i] != '-') && (_cguid[i] != '}')) _cnewguid[y++] = _cguid[i];

			byte[] bdata = new byte[16];
			for (int i = 0; i < 32; i += 2)
				bdata[i / 2] = (byte)((Character.digit(_cnewguid[i], 16) << 4) + Character.digit(_cnewguid[i + 1], 16) & 0xff);

			if (bdata.length != 16) {
				throw new IllegalArgumentException("Invalid data length: " + bdata.length);
			}

			GUID newGuid = new GUID();

			long data1Temp = bdata[0] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[1] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[2] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[3] & 0xff;
			newGuid.Data1 = (int)data1Temp;

			int data2Temp = bdata[4] & 0xff;
			data2Temp <<= 8;
			data2Temp |= bdata[5] & 0xff;
			newGuid.Data2 = (short)data2Temp;

			int data3Temp = bdata[6] & 0xff;
			data3Temp <<= 8;
			data3Temp |= bdata[7] & 0xff;
			newGuid.Data3 = (short)data3Temp;

			newGuid.Data4[0] = bdata[8];
			newGuid.Data4[1] = bdata[9];
			newGuid.Data4[2] = bdata[10];
			newGuid.Data4[3] = bdata[11];
			newGuid.Data4[4] = bdata[12];
			newGuid.Data4[5] = bdata[13];
			newGuid.Data4[6] = bdata[14];
			newGuid.Data4[7] = bdata[15];

			for (String name : newGuid.getFieldOrder())
				newGuid.writeField(name);

			return newGuid;
		}
	};

	static public class POWERBROADCAST_SETTING extends Structure {
		public GUID PowerSetting;
		public int DataLength;
		public byte[] Data = new byte[1];

		private POWERBROADCAST_SETTING () {
		}

		public POWERBROADCAST_SETTING (Pointer pointer) {
			super(pointer);
			read();
		}

		protected List<String> getFieldOrder () {
			return Arrays.asList("PowerSetting", "DataLength", "Data");
		}
	};
}
