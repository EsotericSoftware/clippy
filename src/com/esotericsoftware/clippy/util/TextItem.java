
package com.esotericsoftware.clippy.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class TextItem extends JLabel {
	static final Color over = new Color(0x3399ff);
	static public Font font;
	static private int height;

	boolean selected, mouseMoved;
	public String tooltipText;

	public TextItem (String text) {
		super(text);
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
					setToolTipText("<html><pre><font size=3>"
						+ tooltipText.replace("\r\n", "\n").replace("\n", "<br>").replace("\t", "   "));
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
