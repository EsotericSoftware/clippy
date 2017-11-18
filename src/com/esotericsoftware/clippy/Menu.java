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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JSeparator;

import com.esotericsoftware.clippy.util.PopupFrame;
import com.esotericsoftware.clippy.util.TextItem;

/** @author Nathan Sweet */
public class Menu extends PopupFrame {
	final ArrayList<JComponent> items = new ArrayList();
	final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		new Insets(0, 0, 0, 0), 0, 0);
	final JSeparator exitSeparator = new JSeparator();
	final TextItem exitItem = new TextItem("Exit") {
		public void clicked () {
			Clippy.pidFile.delete();
			System.exit(0);
		}
	};

	public Object addItem (boolean first, String text, final Runnable runnable) {
		TextItem item = new TextItem(text) {
			public void clicked () {
				runnable.run();
			}
		};
		if (first)
			items.add(0, item);
		else
			items.add(item);
		return item;
	}

	public Object addSeparator (boolean first) {
		JSeparator separator = new JSeparator();
		if (first)
			items.add(0, separator);
		else
			items.add(separator);
		return separator;
	}

	public void remove (Object object) {
		items.remove(object);
		if (isVisible()) populate();
	}

	public void populate () {
		panel.removeAll();
		c.gridy = 0;
		boolean lastSeparator = true;
		for (int i = 0, n = items.size(); i < n; i++) {
			JComponent item = items.get(i);
			boolean separator = item instanceof JSeparator;
			if (separator && lastSeparator) continue;
			lastSeparator = separator;
			panel.add(item, c);
			c.gridy++;
		}
		if (!lastSeparator) {
			panel.add(exitSeparator, c);
			c.gridy++;
		}
		panel.add(exitItem, c);
		pack();
	}
}
