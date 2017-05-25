
package com.esotericsoftware.clippy.util;

import java.awt.EventQueue;
import java.util.TimerTask;

public abstract class EventQueueRepeat {
	final Runnable repeatRunnable = new Runnable() {
		public void run () {
			if (repeat()) {
				repeatTask.cancel();
				end();
			}
		}
	};

	final TimerTask repeatTask = new TimerTask() {
		public void run () {
			try {
				EventQueue.invokeLater(repeatRunnable);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	};

	public void run (final int delay) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				start();
				Util.timer.schedule(repeatTask, delay, delay);
			}
		});
	}

	abstract protected void start ();

	/** Returns true when finished. */
	abstract protected boolean repeat ();

	abstract protected void end ();
}
