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

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;
import static java.awt.event.KeyEvent.*;

import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.KeyStroke;

import com.esotericsoftware.clippy.Win.GUITHREADINFO;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.Win.MSG;
import com.sun.jna.Pointer;

/** @author Nathan Sweet */
public class Keyboard {
	static private final boolean windows7 = System.getProperty("os.name", "").startsWith("Windows 7");

	static private final Map<Integer, Integer> codeExceptions = new HashMap<Integer, Integer>() {
		{
			put(VK_INSERT, 0x2D);
			put(VK_DELETE, 0x2E);
			put(VK_ENTER, 0x0D);
			put(VK_COMMA, 0xBC);
			put(VK_PERIOD, 0xBE);
			put(VK_PLUS, 0xBB);
			put(VK_MINUS, 0xBD);
			put(VK_SLASH, 0xBF);
			put(VK_SEMICOLON, 0xBA);
			put(VK_PRINTSCREEN, 0x2C);
		}
	};

	final ArrayList<KeyStroke> hotkeys = new ArrayList();
	boolean started;
	final ArrayDeque<KeyStroke> fireEventQueue = new ArrayDeque();
	final Runnable fireEvent = new Runnable() {
		public void run () {
			KeyStroke keyStroke = fireEventQueue.pollFirst();
			if (keyStroke != null) hotkey(keyStroke);
		}
	};

	public void registerHotkey (KeyStroke keyStroke) {
		if (keyStroke == null) throw new IllegalArgumentException("keyStroke cannot be null.");
		if (started) throw new IllegalStateException();
		hotkeys.add(keyStroke);
	}

	public void start () {
		started = true;

		final CyclicBarrier barrier = new CyclicBarrier(2);

		new Thread("Hotkeys") {
			public void run () {
				if (TRACE) trace("Entered keyboard thread.");

				// Register hotkeys.
				for (int i = 0, n = hotkeys.size(); i < n; i++) {
					KeyStroke keyStroke = hotkeys.get(i);
					if (RegisterHotKey(null, i, getModifiers(keyStroke), getVK(keyStroke))) {
						if (DEBUG) debug("Registered hotkey: " + keyStroke);
					} else {
						if (ERROR) error("Unable to register hotkey: " + keyStroke);
						System.exit(0);
					}
				}

				try {
					barrier.await();
				} catch (Exception ignored) {
				}

				// Listen for hotkeys.
				MSG msg = new MSG();
				while (GetMessage(msg, null, WM_HOTKEY, WM_HOTKEY)) {
					if (msg.message != WM_HOTKEY) continue;
					int id = msg.wParam.intValue();
					if (id >= 0 && id < hotkeys.size()) {
						KeyStroke hotkey = hotkeys.get(id);
						if (TRACE) trace("Received hotkey: " + hotkey);
						fireEventQueue.addLast(hotkey);
						EventQueue.invokeLater(fireEvent);
					}
				}

				if (TRACE) trace("Exited keyboard thread.");
			}
		}.start();

		try {
			barrier.await();
		} catch (Exception ignored) {
		}
	}

	protected void hotkey (KeyStroke keyStroke) {
	}

	public void sendKeyPress (byte vk) {
		keybd_event(vk, (byte)0, 0, null);
		keybd_event(vk, (byte)0, KEYEVENTF_KEYUP, null);
	}

	public void sendKeyDown (byte vk) {
		keybd_event(vk, (byte)0, 0, null);
	}

	public void sendKeyUp (byte vk) {
		keybd_event(vk, (byte)0, KEYEVENTF_KEYUP, null);
	}

	static public int getVK (KeyStroke keyStroke) {
		int keyStrokeCode = keyStroke.getKeyCode();
		Integer code = codeExceptions.get(keyStrokeCode);
		if (code != null) return code;
		return keyStrokeCode;
	}

	static int getModifiers (KeyStroke keyStroke) {
		int modifiers = 0;
		int keyStrokeModifiers = keyStroke.getModifiers();
		if ((keyStrokeModifiers & InputEvent.SHIFT_DOWN_MASK) != 0) modifiers |= MOD_SHIFT;
		if ((keyStrokeModifiers & InputEvent.CTRL_DOWN_MASK) != 0) modifiers |= MOD_CONTROL;
		if ((keyStrokeModifiers & InputEvent.META_DOWN_MASK) != 0) modifiers |= MOD_WIN;
		if ((keyStrokeModifiers & InputEvent.ALT_DOWN_MASK) != 0) modifiers |= MOD_ALT;
		if (windows7) modifiers |= MOD_NOREPEAT;
		return modifiers;
	}
}
