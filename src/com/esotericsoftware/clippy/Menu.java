
package com.esotericsoftware.clippy;

import static java.awt.GridBagConstraints.*;

import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import com.esotericsoftware.clippy.util.PopupFrame;
import com.esotericsoftware.clippy.util.TextItem;

public class Menu extends PopupFrame {
	public Menu () {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = HORIZONTAL;
		c.anchor = WEST;
		c.weightx = 1;

		c.gridy = 0;
		panel.add(new TextItem("Exit") {
			public void clicked () {
				System.exit(0);
			}
		}, c);

		pack();
	}
}
