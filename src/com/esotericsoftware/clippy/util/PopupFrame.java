
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
