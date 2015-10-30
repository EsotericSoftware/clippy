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

import static com.esotericsoftware.clippy.Win.CLibrary.*;
import static com.esotericsoftware.clippy.Win.Kernel32.*;
import static com.esotericsoftware.clippy.Win.Shell32.*;
import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.util.concurrent.CyclicBarrier;

import com.esotericsoftware.clippy.Win.MSG;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/** @author Nathan Sweet */
public class Clipboard {
	Pointer hwnd;
	final char[] chars = new char[2048];
	final int maxTextLength;

	public Clipboard (int maxTextLength) {
		this.maxTextLength = maxTextLength;

		final CyclicBarrier barrier = new CyclicBarrier(2);

		new Thread("Clipboard") {
			public void run () {
				if (TRACE) trace("Entered clipboard thread.");

				hwnd = CreateWindowEx(0, new WString("STATIC"), new WString(""), 0, 0, 0, 0, 0, 0, 0, 0, 0);
				if (hwnd == null) {
					if (ERROR) error("Unable to create clipboard window.");
					System.exit(0);
				}

				if (!AddClipboardFormatListener(hwnd)) {
					if (ERROR) error("Unable to install clipboard listener.");
					System.exit(0);
				}

				try {
					barrier.await();
				} catch (Exception ignored) {
				}

				MSG msg = new MSG();
				while (GetMessage(msg, null, WM_CLIPBOARDUPDATE, WM_CLIPBOARDUPDATE)) {
					if (msg.message != WM_CLIPBOARDUPDATE) continue;
					if (hwnd.equals(GetClipboardOwner())) {
						if (TRACE) trace("Clipboard changed (own event).");
						continue;
					}
					if (DEBUG) debug("Clipboard changed.");
					changed();
				}

				if (TRACE) trace("Exited clipboard thread.");
			}
		}.start();

		try {
			barrier.await();
		} catch (Exception ignored) {
		}
	}

	protected void changed () {
	}

	private boolean open (int millis) {
		int i = 0;
		while (!OpenClipboard(hwnd)) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException ex) {
			}
			i += 5;
			if (i > millis) {
				if (WARN) warn("Unable to open clipboard.");
				return false;
			}
		}
		return true;
	}

	/** @return May be null. */
	public String getContents () {
		if (!open(500)) return null;
		try {
			int format;
			if (IsClipboardFormatAvailable(CF_UNICODETEXT)) {
				format = CF_UNICODETEXT;
			} else if (IsClipboardFormatAvailable(CF_TEXT)) {
				format = CF_TEXT;
			} else if (IsClipboardFormatAvailable(CF_HDROP)) {
				format = CF_HDROP;
				if (TRACE) trace("Files clipboard item.");
			} else {
				if (TRACE) trace("Non-text clipboard item.");
				return null;
			}

			Pointer globalData = GetClipboardData(format);
			if (globalData == null) {
				if (WARN) warn("Unable to retrieve clipboard data.");
				return null;
			}

			Pointer data = GlobalLock(globalData);
			if (data == null) {
				if (WARN) warn("Unable to lock clipboard buffer.");
				return null;
			}

			String text = null;
			switch (format) {
			case CF_UNICODETEXT: {
				if (wcslen(data) <= maxTextLength)
					text = data.getWideString(0);
				else
					text = new String(data.getCharArray(0, maxTextLength));
				break;
			}
			case CF_TEXT: {
				if (strlen(data) <= maxTextLength)
					text = data.getString(0);
				else
					text = new String(data.getCharArray(0, maxTextLength));
				break;
			}
			case CF_HDROP:
				int fileCount = DragQueryFile(data, -1, null, 0);
				if (fileCount == 0) {
					if (WARN) warn("Unable to query file count.");
					return null;
				}
				StringBuilder buffer = new StringBuilder(512);
				for (int i = 0; i < fileCount; i++) {
					int charCount = DragQueryFile(data, i, chars, chars.length);
					if (charCount == 0) {
						if (WARN) warn("Unable to query file name.");
						return null;
					}
					buffer.append(chars, 0, charCount);
					buffer.append('\n');
				}
				buffer.setLength(buffer.length() - 1);
				text = buffer.toString();
				break;
			}

			GlobalUnlock(globalData);

			return text;
		} finally {
			if (!CloseClipboard(hwnd)) {
				if (WARN) warn("Unable to close clipboard.");
				return null;
			}
		}
	}

	public boolean setContents (String text) {
		if (TRACE) trace("Setting clipboard text: " + text.trim());

		if (!open(150)) return false;

		try {
			if (!EmptyClipboard()) {
				if (WARN) warn("Unable to empty clipboard.");
				return false;
			}

			Pointer data = GlobalAlloc(GMEM_MOVEABLE, (text.length() + 1) * 2); // 2 is sizeof(WCHAR)
			if (data == null) {
				if (WARN) warn("Unable to allocate data.");
				return false;
			}

			Pointer buffer = GlobalLock(data);
			if (buffer == null) {
				if (WARN) warn("Unable to lock buffer.");
				return false;
			}
			buffer.setWideString(0, text);
			GlobalUnlock(data);

			if (SetClipboardData(CF_UNICODETEXT, buffer) == null) {
				if (WARN) warn("Unable to set clipboard data.");
				return false;
			}
			return true;
		} finally {
			if (!CloseClipboard(hwnd)) {
				if (WARN) warn("Unable to close clipboard.");
			}
		}
	}
}
