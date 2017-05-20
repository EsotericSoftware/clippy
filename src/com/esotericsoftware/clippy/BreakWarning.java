
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.esotericsoftware.clippy.Win.LASTINPUTINFO;
import com.esotericsoftware.clippy.util.Util;

public class BreakWarning {
	final Clippy clippy = Clippy.instance;
	final LASTINPUTINFO lastInputInfo = new LASTINPUTINFO();
	long inactiveTime, lastInactiveTime;
	int inactiveCount;
	long lastBreakTime = System.currentTimeMillis();
	volatile ProgressBar progressBar;
	Clip startClip, flashClip, endClip;
	volatile boolean disabled;

	public BreakWarning () {
		if (clippy.config.breakWarningMinutes <= 0) return;

		if (clippy.config.breakStartSound != null) startClip = loadClip(clippy.config.breakStartSound);
		if (clippy.config.breakFlashSound != null) flashClip = loadClip(clippy.config.breakFlashSound);
		if (clippy.config.breakEndSound != null) endClip = loadClip(clippy.config.breakEndSound);

		Util.timer.schedule(new TimerTask() {
			public void run () {
				if (progressBar != null) return;

				long time = System.currentTimeMillis();

				long inactiveMillis = getInactiveMillis(true);
				long inactiveMinutes = inactiveMillis / 1000 / 60;
				if (inactiveMinutes >= clippy.config.breakResetMinutes) lastBreakTime = time;

				long activeMillis = (time - lastBreakTime) - inactiveMillis;
				long activeMinutes = activeMillis / 1000 / 60;
				if (activeMinutes >= clippy.config.breakWarningMinutes)
					showBreakDialog();
				else {
					clippy.tray.updateTooltip("Clippy\n" //
						+ "Active: " + formatTime(activeMillis) + "\n" //
						+ "Break in: " + formatTime((clippy.config.breakWarningMinutes - activeMinutes) * 60 * 1000));
				}
			}
		}, 5 * 1000, 5 * 1000);
	}

	void showBreakDialog () {
		clippy.tray.updateTooltip("Clippy - Take a break!");

		EventQueue.invokeLater(new Runnable() {
			public void run () {
				progressBar = new ProgressBar("");
				progressBar.clickToDispose = false;
				progressBar.red("");
				if (clippy.config.breakReminderMinutes > 0)
					clippy.tray.balloon("Clippy", "Take a break!", 30000);
				else {
					playClip(startClip, 1);
					progressBar.setVisible(true);
				}
				new Thread("BreakWarning Dialog") {
					{
						setDaemon(true);
					}

					public void run () {
						float indeterminateMillis = 5000;
						float volume = 0.05f;
						while (true) {
							long inactiveMillis = getInactiveMillis(false);
							long inactiveMinutes = inactiveMillis / 1000 / 60;
							if (inactiveMinutes >= clippy.config.breakResetMinutes) break;

							float percent = 1 - inactiveMillis / (float)(clippy.config.breakResetMinutes * 60 * 1000);
							String message;
							if (percent < 0.75f) {
								indeterminateMillis = 0;
								message = "Break: " + formatTime(clippy.config.breakResetMinutes * 60 * 1000 - inactiveMillis);
								progressBar.setVisible(true);
							} else
								message = "Active: " + formatTime(System.currentTimeMillis() - lastBreakTime);
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
								}
							} else {
								if (clippy.config.breakReminderMinutes > 0 && percent >= 0.99f
									&& indeterminateMillis < -clippy.config.breakReminderMinutes * 60 * 1000) indeterminateMillis = 5000;
								progressBar.setProgress(percent); // Sets indeterminate to false.
								progressBar.toFront();
								progressBar.setAlwaysOnTop(true);
							}
							Util.sleep(100);
						}
						lastBreakTime = System.currentTimeMillis();
						playClip(endClip, 1);
						progressBar.done("Break complete!", 2000);
						progressBar = null;
					}
				}.start();
			}
		});
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

	public void toggle () {
		disabled = !disabled;
		ProgressBar progressBar = this.progressBar;
		if (progressBar != null) progressBar.setVisible(!disabled);
	}

	String formatTime (long millis) {
		long activeMinutes = millis / 1000 / 60;
		long hours = activeMinutes / 60, minutes = activeMinutes - hours * 60;
		String minutesMessage = minutes + " minute" + (minutes == 1 ? "" : "s");
		String hoursMessage = hours + " hour" + (hours == 1 ? "" : "s");
		if (hours == 0) return minutesMessage;
		if (minutes == 0) return hoursMessage;
		return hoursMessage + ", " + minutesMessage;
	}
}
