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

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/** @author Nathan Sweet */
public class TextItem extends JLabel {
	static final Color over = new Color(0x3399ff);
	static public Font font;
	static private int height;

	boolean selected, mouseMoved;
	public String tooltipText;
	public String label;

	public TextItem (String label) {
		super(label);
		this.label = label;
		putClientProperty("html.disable", Boolean.TRUE);
		setOpaque(true);
		setFont(font);
		setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));

		addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent e) {
				clicked();
			}

			public void mouseEntered (MouseEvent e) {
				setSelected(true);
				selected();

				if (tooltipText != null) {
					setToolTipText(
						"<html><pre><font size=3>" + tooltipText.replace("\r\n", "\n").replace("\n", "<br>").replace("\t", "   "));
					tooltipText = null;
				}
			}

			public void mouseExited (MouseEvent e) {
				setSelected(false);
				selected();
			}
		});
	}

	private int getPreferredHeight () {
		return super.getPreferredSize().height;
	}

	public void setSelected (boolean selected) {
		this.selected = selected;
		if (selected) {
			setBackground(over);
			setForeground(Color.white);
		} else {
			setBackground(null);
			setForeground(Color.black);
		}

		Container parent = getParent();
		if (parent != null) parent.repaint();
	}

	public boolean isSelected () {
		return selected;
	}

	public void selected () {
	}

	public void clicked () {
	}

	static public int getItemHeight () {
		if (height == 0) height = new TextItem("W").getPreferredHeight();
		return height;
	}
}
