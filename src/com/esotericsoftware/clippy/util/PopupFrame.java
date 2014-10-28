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

package com.esotericsoftware.clippy.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.esotericsoftware.clippy.Clippy;

/** @author Nathan Sweet */
public class PopupFrame extends JFrame {
	public final Clippy clippy = Clippy.instance;
	public final JPanel panel = new JPanel();
	protected final FocusAdapter focusListener;

	public PopupFrame () {
		setType(JFrame.Type.UTILITY);
		setUndecorated(true);
		setResizable(false);
		setAlwaysOnTop(true);

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panel);

		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		addKeyListener(new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					hidePopup();
					break;
				}
			}
		});

		focusListener = new FocusAdapter() {
			private final KeyEventDispatcher disableAlt = new KeyEventDispatcher() {
				public boolean dispatchKeyEvent (KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ALT) {
						if (e.getID() == KeyEvent.KEY_PRESSED) altPressed();
						return true; // Don't show OS window menu.
					}
					return false;
				}
			};

			public void focusGained (FocusEvent e) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(disableAlt);
			}

			public void focusLost (FocusEvent e) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(disableAlt);

				Component focused = e.getOppositeComponent();
				if (focused == null || (focused != PopupFrame.this && SwingUtilities.getWindowAncestor(focused) != PopupFrame.this))
					hidePopup();
			}
		};
		addFocusListener(focusListener);
	}

	public void setVisible (boolean b) {
		if (b)
			showPopup();
		else
			hidePopup();
	}

	public void showPopup () {
		super.setVisible(true);
	}

	public void hidePopup () {
		super.setVisible(false);
	}

	public void altPressed () {
	}
}
