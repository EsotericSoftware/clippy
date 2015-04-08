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
import static java.awt.GridBagConstraints.*;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import com.esotericsoftware.clippy.ClipDataStore.ClipConnection;
import com.esotericsoftware.clippy.Win.GUITHREADINFO;
import com.esotericsoftware.clippy.Win.MONITORINFO;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.Win.RECT;
import com.esotericsoftware.clippy.util.DocumentChangeListener;
import com.esotericsoftware.clippy.util.PopupFrame;
import com.esotericsoftware.clippy.util.TextItem;
import com.esotericsoftware.minlog.Log;
import com.sun.jna.Pointer;

/** @author Nathan Sweet */
public class Popup extends PopupFrame {
	final ArrayList<TextItem> items = new ArrayList();
	final ArrayList<String> itemText = new ArrayList();
	final ArrayList<Integer> itemIDs = new ArrayList();
	final POINT popupPosition = new POINT();
	final GridBagConstraints c = new GridBagConstraints();
	final Rectangle rectangle = new Rectangle(0, 0, 0, TextItem.getItemHeight());
	TextItem selectedItem;
	String selectNextPopulate;

	final JPanel itemPanel = new JPanel();
	final JTextField searchField = new JTextField();
	final JPanel blockMouse = new JPanel();
	final JCheckBox lockCheckbox = new JCheckBox("Lock order");
	Point mouseStart;

	final ExecutorService searchExecutor = Executors.newFixedThreadPool(1);
	volatile int startIndex;
	final ArrayList<String> searchText = new ArrayList();
	final ArrayList<Integer> searchIDs = new ArrayList();

	public Popup () {
		blockMouse.setOpaque(false);
		setGlassPane(blockMouse); // Prevent mouse from selecting items until the mouse is moved a bit.
		blockMouse.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved (MouseEvent e) {
				Point mouse = MouseInfo.getPointerInfo().getLocation();
				if (mouse.distance(mouseStart) > 15) blockMouse.setVisible(false);
			}
		});

		itemPanel.setLayout(new GridBagLayout());

		JScrollPane scroll = new JScrollPane(itemPanel) {
			public Dimension getPreferredSize () {
				Dimension prefSize = super.getPreferredSize();
				prefSize.width = Math.min(prefSize.width, clippy.config.popupWidth);
				prefSize.height = Math.min(prefSize.height, TextItem.getItemHeight() * clippy.config.popupCount);
				return prefSize;
			}
		};
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		c.gridy = 1;
		c.fill = BOTH;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(scroll, c);

		c.weighty = 0;
		c.fill = HORIZONTAL;
		c.anchor = WEST;

		searchField.setFont(TextItem.font);
		searchField.addFocusListener(focusListener);
		searchField.getDocument().addDocumentListener(new DocumentChangeListener() {
			public void changed () {
				pack();
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						startIndex = 0;
						showSearchItems(searchField.getText());
					}
				});
			}
		});

		lockCheckbox.setFont(TextItem.font);
		lockCheckbox.addFocusListener(focusListener);

		KeyAdapter forwardKeys = new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_PAGE_DOWN:
				case KeyEvent.VK_DELETE:
					popupKeyPressed(e);
					return;
				}
			}
		};
		searchField.addKeyListener(forwardKeys);
		lockCheckbox.addKeyListener(forwardKeys);
		lockCheckbox.addKeyListener(new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) popupKeyPressed(e);
			}
		});

		addKeyListener(new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				popupKeyPressed(e);
			}

			public void keyTyped (KeyEvent e) {
				popupKeyTyped(e);
			}
		});
	}

	void popupKeyPressed (KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_DELETE:
			if (e.isShiftDown() && e.isControlDown()) {
				int index = items.indexOf(selectedItem);
				if (index == -1) return;
				try {
					clippy.db.getThreadConnection().remove(itemText.get(index));
					if (Log.TRACE) trace("Deleted clip.");
				} catch (SQLException ex) {
					if (Log.ERROR) error("Unable to delete clip.", ex);
				}

				if (index < items.size() - 1)
					index++;
				else if (index != 0) //
					index--;
				selectNextPopulate = itemText.get(index);

				refresh();
			}
			return;
		case KeyEvent.VK_ESCAPE:
			hidePopup();
			return;
		case KeyEvent.VK_ENTER: {
			int index = items.indexOf(selectedItem);
			if (index != -1) {
				String text = getText(itemIDs.get(index));
				if (e.isControlDown())
					Pastebin.save(text);
				else
					pasteItem(text);
			}
			return;
		}
		case KeyEvent.VK_PAGE_UP: {
			if (items.isEmpty()) return;
			int selectedIndex = items.indexOf(selectedItem);
			if (selectedIndex == 0 && searchField.getParent() == null) {
				// Page up at top of recent list shows previous page of items.
				int oldStartIndex = startIndex;
				startIndex = Math.max(0, startIndex - clippy.config.popupCount);
				if (showRecentItems())
					keepOnScreen();
				else
					startIndex = oldStartIndex;
				return;
			}
			int index = Math.max(0, selectedIndex - clippy.config.popupCount);
			TextItem item = items.get(index);
			item.setSelected(true);
			item.selected();
			return;
		}
		case KeyEvent.VK_PAGE_DOWN: {
			if (items.isEmpty()) return;
			int selectedIndex = items.indexOf(selectedItem);
			if (selectedIndex == items.size() - 1 && searchField.getParent() == null) {
				// Page down at end of recent list shows next page of items.
				int oldStartIndex = startIndex;
				startIndex += clippy.config.popupCount;
				if (showRecentItems())
					keepOnScreen();
				else
					startIndex = oldStartIndex;
				TextItem lastItem = items.get(items.size() - 1);
				lastItem.setSelected(true);
				lastItem.selected();
				return;
			}
			int index = Math.min(selectedIndex + clippy.config.popupCount, items.size() - 1);
			TextItem item = items.get(index);
			item.setSelected(true);
			item.selected();
			return;
		}
		case KeyEvent.VK_UP: {
			if (items.isEmpty()) return;
			int index = items.indexOf(selectedItem) - (e.isControlDown() ? 5 : 1);
			if (index < 0) index = e.isControlDown() ? 0 : (items.size() - 1);
			TextItem item = items.get(index);
			item.setSelected(true);
			item.selected();
			return;
		}
		case KeyEvent.VK_DOWN: {
			if (items.isEmpty()) return;
			int index = items.indexOf(selectedItem) + (e.isControlDown() ? 5 : 1);
			if (index >= items.size()) index = e.isControlDown() ? (items.size() - 1) : 0;
			TextItem item = items.get(index);
			item.setSelected(true);
			item.selected();
			return;
		}
		case KeyEvent.VK_HOME: {
			TextItem item = items.get(0);
			item.setSelected(true);
			item.selected();
			return;
		}
		case KeyEvent.VK_END: {
			TextItem item = items.get(items.size() - 1);
			item.setSelected(true);
			item.selected();
			return;
		}
		}

		if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
			int index = keyCode - KeyEvent.VK_0 - 1;
			if (index < 0) index = 9;
			if (index < itemText.size()) pasteItem(getText(itemIDs.get(index)));
		}
	}

	private String getText (int id) {
		try {
			ClipConnection conn = clippy.db.getThreadConnection();
			return conn.getText(id);
		} catch (SQLException ex) {
			if (Log.ERROR) error("Unable to retrieve full text.", ex);
			return "";
		}
	}

	void popupKeyTyped (KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE && e.isShiftDown() && e.isControlDown()) return;
		if (e.getKeyChar() == KeyEvent.VK_ENTER && e.isControlDown()) return;

		// Show search field.
		c.gridy = 0;
		panel.add(searchField, c);
		searchField.requestFocus();
		searchField.dispatchEvent(e);
		pack();
		keepOnScreen();
		startIndex = 0;
		showSearchItems(searchField.getText());
	}

	public void altPressed () {
		// Show options.
		if (lockCheckbox.getParent() != null) {
			requestFocus();
			panel.remove(lockCheckbox);
			pack();
			return;
		}
		c.gridy = 2;
		panel.add(lockCheckbox, c);
		pack();
		keepOnScreen();
		lockCheckbox.requestFocus();
	}

	public void showPopup () {
		if (isVisible()) return;
		if (DEBUG && !TRACE) debug("Show popup.");
		if (!showRecentItems()) {
			if (WARN) warn("No clips to show.");
			return;
		}

		Pointer hwnd = GetForegroundWindow();
		POINT position = getPopupPosition(hwnd, getWidth(), getHeight());
		setLocation(position.x, position.y);
		keepOnScreen();

		mouseStart = MouseInfo.getPointerInfo().getLocation();
		blockMouse.setVisible(true);

		super.showPopup();
		requestFocus();
	}

	private void keepOnScreen () {
		POINT position = new POINT();
		position.x = getX();
		position.y = getY();

		Pointer monitor = MonitorFromWindow(GetForegroundWindow(), MONITOR_DEFAULTTONEAREST);
		MONITORINFO monitorInfo = new MONITORINFO();
		if (GetMonitorInfo(monitor, monitorInfo)) {
			position.x = Math.min(Math.max(position.x, monitorInfo.rcMonitor.left), monitorInfo.rcMonitor.right - getWidth());
			position.y = Math.min(Math.max(position.y, monitorInfo.rcMonitor.top), monitorInfo.rcMonitor.bottom - getHeight());
		} else if (TRACE) //
			trace("Unable to get monitor info.");

		setLocation(position.x, position.y);
	}

	boolean showRecentItems () {
		try {
			ClipConnection conn = clippy.db.getThreadConnection();
			conn.last(itemIDs, itemText, clippy.config.popupCount, startIndex);
			if (itemText.size() == 0) return false;
			populate();
			return true;
		} catch (SQLException ex) {
			if (Log.ERROR) error("Unable to retrieve clips.", ex);
			return false;
		}
	}

	void showSearchItems (final String text) {
		searchExecutor.submit(new Runnable() {
			public void run () {
				try {
					ClipConnection conn = clippy.db.getThreadConnection();
					conn.search(searchIDs, searchText, "%" + text + "%", clippy.config.popupSearchCount);
					EventQueue.invokeLater(new Runnable() {
						public void run () {
							itemText.clear();
							itemText.addAll(searchText);
							itemIDs.clear();
							itemIDs.addAll(searchIDs);
							populate();
						}
					});
				} catch (SQLException ex) {
					if (Log.ERROR) error("Unable to retrieve clips.", ex);
				}
			}
		});
	}

	public void refresh () {
		if (searchField.getParent() == null) {
			if (!showRecentItems()) hidePopup();
		} else
			showSearchItems(searchField.getText());
	}

	void populate () {
		clearItems();
		for (int i = 0, n = itemText.size(); i < n; i++) {
			final String text = itemText.get(i);
			String label = text.trim().replace("\r\n", "\n").replace('\n', ' ');
			if (label.isEmpty()) label = text.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t").replace(" ", "\u2022");

			if (i < 9)
				label = (i + 1) + ": " + label;
			else if (i == 9) //
				label = "0: " + label;

			TextItem item = new TextItem(label) {
				public void clicked () {
					pasteItem(text);
				}

				public void selected () {
					if (!isSelected()) setSelected(true);
					if (selectedItem != this) selectedItem.setSelected(false);
					selectedItem = this;
					scrollRectToVisible(rectangle);
				}
			};
			if (item.getPreferredSize().width > clippy.config.popupWidth) item.tooltipText = text;
			items.add(item);

			if (i == 0) {
				selectedItem = item;
				item.setSelected(true);
			}

			c.gridy = i + 1;
			itemPanel.add(item, c);
		}

		pack();

		if (selectNextPopulate != null) {
			int index = itemText.indexOf(selectNextPopulate);
			if (index != -1) {
				TextItem item = items.get(index);
				item.setSelected(true);
				item.selected();
			}
			selectNextPopulate = null;
		}
	}

	void pasteItem (String text) {
		hidePopup();
		clippy.paste(text);
	}

	POINT getScreenCenter (int width, int height) {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		popupPosition.x = size.width / 2 - width / 2;
		popupPosition.y = size.height / 2 - height / 2;
		return popupPosition;
	}

	POINT getPopupPosition (Pointer hwndForeground, int width, int height) {
		if (hwndForeground == null) {
			if (TRACE) trace("Unable to get foreground window, positioning popup using screen center.");
			return getScreenCenter(width, height);
		}

		int threadId = GetWindowThreadProcessId(hwndForeground, null);

		GUITHREADINFO guiInfo = new GUITHREADINFO();
		if (!GetGUIThreadInfo(threadId, guiInfo)) {
			if (TRACE) trace("Unable to get GUI thread info, positioning popup using screen center.");
			return getScreenCenter(width, height);
		}

		if (guiInfo.hwndCaret != null) {
			popupPosition.x = guiInfo.rcCaret.left - 2;
			popupPosition.y = guiInfo.rcCaret.top - 2;
			if (ClientToScreen(guiInfo.hwndCaret, popupPosition)) {
				if (TRACE) trace("Positioning popup using caret position.");
				return popupPosition;
			}
			if (TRACE) trace("Unable to compute caret screen coordinates.");
		}

		Pointer hwnd = guiInfo.hwndFocus;
		if (TRACE && hwnd != null) trace("Positioning popup using window with keyboard focus.");
		if (hwnd == null) {
			hwnd = guiInfo.hwndActive;
			if (TRACE && hwnd != null) trace("Positioning popup using active window.");
		}
		if (hwnd != null) {
			RECT rect = new RECT();
			if (GetWindowRect(hwnd, rect)) {
				popupPosition.x = rect.left + (rect.right - rect.left) / 2 - width / 2;
				popupPosition.y = rect.top + (rect.bottom - rect.top) / 2 - height / 2;
				return popupPosition;
			}
			if (TRACE) trace("Unable to get window rectangle.");
		}

		if (TRACE) trace("Unable to use a window, positioning popup using screen center.");
		return getScreenCenter(width, height);
	}

	public void hidePopup () {
		super.hidePopup();
		clearItems();
		panel.remove(searchField);
		if (!lockCheckbox.isSelected()) panel.remove(lockCheckbox);
		searchField.setText("");
		startIndex = 0;
	}

	public void clearItems () {
		itemPanel.removeAll();
		if (selectedItem != null) selectedItem.setSelected(false);
		selectedItem = null;
		items.clear();
		ToolTipManager.sharedInstance().mouseExited(new MouseEvent(this, 0, 0, 0, 0, 0, 0, 0, 0, false, 0)); // Hide any tooltip.
	}
}
