
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.esotericsoftware.clippy.Win.LASTINPUTINFO;
import com.esotericsoftware.clippy.util.EventQueueRepeat;
import com.esotericsoftware.clippy.util.Util;

public class BreakWarning {
	static final Clippy clippy = Clippy.instance;

	final LASTINPUTINFO lastInputInfo = new LASTINPUTINFO();
	long inactiveTime, lastInactiveTime;
	int inactiveCount;
	long lastBreakTime = System.currentTimeMillis();
	Clip startClip, flashClip, endClip;
	volatile boolean disabled;
	Timer timer;
	volatile BreakReminder breakReminder;

	public BreakWarning () {
		if (clippy.config.breakWarningMinutes <= 0) return;
		timer = new Timer("BreakWarning", true);

		if (clippy.config.breakStartSound != null) startClip = loadClip(clippy.config.breakStartSound);
		if (clippy.config.breakFlashSound != null) flashClip = loadClip(clippy.config.breakFlashSound);
		if (clippy.config.breakEndSound != null) endClip = loadClip(clippy.config.breakEndSound);

		clippy.menu.addSeparator(false);
		clippy.menu.addItem(false, "Start break", new Runnable() {
			public void run () {
				clippy.menu.hidePopup();
				timer.schedule(new TimerTask() {
					public void run () {
						if (showBreakReminder(false)) {
							if (INFO) info("Break started manually.");
						}
					}
				}, 1);
			}
		});

		timer.schedule(new TimerTask() {
			public void run () {
				checkBreakReminder();
			}
		}, 5 * 1000, 5 * 1000);
	}

	synchronized void checkBreakReminder () {
		if (breakReminder != null) return;

		long time = System.currentTimeMillis();

		long inactiveMillis = getInactiveMillis(true);
		long inactiveMinutes = inactiveMillis / 1000 / 60;
		if (inactiveMinutes >= clippy.config.breakResetMinutes) lastBreakTime = time;

		long activeMillis = (time - lastBreakTime) - inactiveMillis;
		long activeMinutes = activeMillis / 1000 / 60;
		if (activeMinutes >= clippy.config.breakWarningMinutes) {
			if (showBreakReminder(true)) {
				if (INFO) info("Break needed.");
			}
		} else {
			clippy.tray.updateTooltip("Clippy\n" //
				+ "Active: " + formatTimeMinutes(activeMillis) + "\n" //
				+ "Break in: " + formatTimeMinutes((clippy.config.breakWarningMinutes - activeMinutes) * 60 * 1000));
		}
	}

	synchronized boolean showBreakReminder (boolean balloonFirst) {
		if (disabled || breakReminder != null) return false;
		breakReminder = new BreakReminder(balloonFirst);
		inactiveTime = Win.Kernel32.GetTickCount();
		return true;
	}

	void playClip (Clip clip, float volume) {
		if (clip == null) return;
		if (volume < 0) volume = 0;
		if (volume > 1) volume = 1;
		clip.stop();
		clip.setFramePosition(0);
		((FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(20f * (float)Math.log10(volume));
		clip.start();
	}

	private Clip loadClip (String sound) {
		AudioInputStream audioInput = null;
		try {
			InputStream input;
			if (sound.equals("breakStart"))
				input = BreakWarning.class.getResourceAsStream("/breakStart.wav");
			else if (sound.equals("breakFlash"))
				input = BreakWarning.class.getResourceAsStream("/breakFlash.wav");
			else if (sound.equals("breakEnd"))
				input = BreakWarning.class.getResourceAsStream("/breakEnd.wav");
			else
				input = new FileInputStream(sound);
			Clip clip = AudioSystem.getClip();
			audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(input));
			clip.open(audioInput);
			return clip;
		} catch (Exception ex) {
			if (ERROR) error("Unable to load sound: " + sound, ex);
			return null;
		} finally {
			if (audioInput != null) {
				try {
					audioInput.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	long getInactiveMillis (boolean sensitive) {
		Win.User32.GetLastInputInfo(lastInputInfo);
		long time = lastInputInfo.dwTime;
		if (sensitive)
			inactiveTime = time;
		else {
			if (time != lastInactiveTime) {
				if (time - lastInactiveTime < 4000) {
					inactiveCount++;
					if (inactiveCount > 6) inactiveTime = time;
				} else
					inactiveCount = 0;
				lastInactiveTime = time;
			}
		}
		return Win.Kernel32.GetTickCount() - inactiveTime;
	}

	public synchronized void toggle () {
		disabled = !disabled;
		BreakReminder breakReminder = this.breakReminder;
		if (breakReminder != null) breakReminder.progressBar.setVisible(!disabled);
		this.breakReminder = null;
	}

	static String formatTimeMinutes (long timeMillis) {
		long timeMinutes = timeMillis / 1000 / 60;
		long hours = timeMinutes / 60, minutes = timeMinutes - hours * 60;
		String minutesMessage = minutes + " minute" + (minutes == 1 ? "" : "s");
		String hoursMessage = hours + " hour" + (hours == 1 ? "" : "s");
		if (hours == 0) return minutesMessage;
		if (minutes == 0) return hoursMessage;
		return hoursMessage + ", " + minutesMessage;
	}

	static String formatTimeSeconds (long timeMillis) {
		long timeSeconds = timeMillis / 1000;
		long minutes = timeSeconds / 60, seconds = timeSeconds - minutes * 60;
		String secondsMessage = seconds + " second" + (seconds == 1 ? "" : "s");
		String minutesMessage = minutes + " minute" + (minutes == 1 ? "" : "s");
		if (minutes == 0) return secondsMessage;
		return minutesMessage + ", " + secondsMessage;
	}

	class BreakReminder extends EventQueueRepeat {
		float indeterminateMillis = 5000;
		float volume = 0.05f;

		final boolean balloonFirst;
		private Object cancelItem, cancelSeparator;
		boolean cancel;
		final ProgressBar progressBar = new ProgressBar("");

		public BreakReminder (boolean balloonFirst) {
			this.balloonFirst = balloonFirst;
			run(100);
		}

		protected void start () {
			cancelSeparator = clippy.menu.addSeparator(false);
			cancelItem = clippy.menu.addItem(false, "Cancel break", new Runnable() {
				public void run () {
					clippy.menu.hidePopup();
					cancel = true;
				}
			});

			progressBar.clickToDispose = false;
			progressBar.red("");
			clippy.tray.updateTooltip("Clippy - Take a break!");
			if (balloonFirst && clippy.config.breakReminderMinutes > 0)
				clippy.tray.balloon("Clippy", "Take a break!", 30000);
			else {
				playClip(startClip, 1);
				progressBar.setVisible(true);
			}
		}

		protected boolean repeat () {
			if (cancel || breakReminder != this) return true;

			long inactiveMillis = getInactiveMillis(false);
			long inactiveMinutes = inactiveMillis / 1000 / 60;
			if (inactiveMinutes >= clippy.config.breakResetMinutes) return true;

			float percent = 1 - inactiveMillis / (float)(clippy.config.breakResetMinutes * 60 * 1000);
			String message;
			if (percent < 0.75f) {
				indeterminateMillis = 0;
				message = "Break: " + formatTimeSeconds(clippy.config.breakResetMinutes * 60 * 1000 - inactiveMillis);
				progressBar.setVisible(true);
			} else
				message = "Active: " + formatTimeMinutes(System.currentTimeMillis() - lastBreakTime);
			progressBar.progressBar.setString(message);

			indeterminateMillis -= 100;
			if (indeterminateMillis > 0) {
				if (!progressBar.progressBar.isIndeterminate()) {
					if (!progressBar.isVisible()) {
						// First time after balloon.
						playClip(startClip, 1);
						progressBar.setVisible(true);
					} else {
						// Every breakReminderMinutes.
						playClip(flashClip, volume);
						volume += 0.1f;
					}
					progressBar.progressBar.setIndeterminate(true);
					if (INFO) info("Break reminder.");
				}
			} else {
				if (clippy.config.breakReminderMinutes > 0 && percent >= 0.99f
					&& indeterminateMillis < -clippy.config.breakReminderMinutes * 60 * 1000) indeterminateMillis = 5000;
				progressBar.setProgress(percent); // Sets indeterminate to false.
				progressBar.toFront();
				progressBar.setAlwaysOnTop(true);
				clippy.menu.toFront();
			}
			return false;
		}

		protected void end () {
			clippy.menu.remove(cancelSeparator);
			clippy.menu.remove(cancelItem);
			lastBreakTime = System.currentTimeMillis();
			playClip(endClip, 1);
			progressBar.done("Break complete!", 2000);
			synchronized (BreakWarning.this) {
				breakReminder = null;
			}
			if (INFO) info("Break complete!");
		}
	};
}
