
package com.esotericsoftware.clippy.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

abstract public class DocumentChangeListener implements DocumentListener {
	public void changedUpdate (DocumentEvent e) {
		changed();
	}

	public void removeUpdate (DocumentEvent e) {
		changed();
	}

	public void insertUpdate (DocumentEvent e) {
		changed();
	}

	abstract public void changed ();
}
