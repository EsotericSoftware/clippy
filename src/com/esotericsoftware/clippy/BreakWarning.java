
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

				long inactiveMinutes = getInactiveMillis(true) / 1000 / 60;
				if (inactiveMinutes >= clippy.config.breakResetMinutes) lastBreakTime = System.currentTimeMillis();

				long activeMinutes = (System.currentTimeMillis() - lastBreakTime) / 1000 / 60 - inactiveMinutes;
				if (activeMinutes >= clippy.config.breakWarningMinutes) showBreakDialog();
			}
		}, clippy.config.breakWarningMinutes * 60 * 1000, 60 * 1000);
	}

	void showBreakDialog () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				playClip(startClip);
				progressBar = new ProgressBar("");
				progressBar.clickToDispose = false;
				progressBar.red("");
				new Thread("BreakWarning Dialog") {
					{
						setDaemon(true);
					}

					public void run () {
						float indeterminateMillis = 5000;
						while (true) {
							long inactiveMillis = getInactiveMillis(false);
							long inactiveMinutes = inactiveMillis / 1000 / 60;
							if (inactiveMinutes >= clippy.config.breakResetMinutes) break;

							float percent = 1 - inactiveMillis / (float)(clippy.config.breakResetMinutes * 60 * 1000);
							String message;
							if (percent < 0.75f) {
								indeterminateMillis = 0;
								long breakSeconds = clippy.config.breakResetMinutes * 60 - inactiveMillis / 1000;
								long minutes = breakSeconds / 60, seconds = breakSeconds - minutes * 60;
								String secondsMessage = seconds + " second" + (seconds == 1 ? "" : "s");
								String minutesMessage = minutes + " minute" + (minutes == 1 ? "" : "s");
								if (minutes == 0)
									message = "Break: " + secondsMessage;
								else
									message = "Break: " + minutesMessage + ", " + secondsMessage;
							} else {
								long activeMinutes = (System.currentTimeMillis() - lastBreakTime) / 1000 / 60;
								long hours = activeMinutes / 60, minutes = activeMinutes - hours * 60;
								String minutesMessage = minutes + " minute" + (minutes == 1 ? "" : "s");
								String hoursMessage = hours + " hour" + (hours == 1 ? "" : "s");
								if (hours == 0)
									message = "Active: " + minutesMessage;
								else if (minutes == 0)
									message = "Active: " + hoursMessage;
								else
									message = "Active: " + hoursMessage + ", " + minutesMessage;
							}
							progressBar.progressBar.setString(message);

							indeterminateMillis -= 100;
							if (indeterminateMillis > 0) {
								if (!progressBar.progressBar.isIndeterminate()) {
									playClip(flashClip);
									progressBar.progressBar.setIndeterminate(true);
								}
							} else {
								if (indeterminateMillis < -5 * 60 * 1000 && percent >= 0.99f) indeterminateMillis = 5000;
								progressBar.setProgress(percent);
								progressBar.toFront();
								progressBar.setAlwaysOnTop(true);
							}
							Util.sleep(100);
						}
						lastBreakTime = System.currentTimeMillis();
						playClip(endClip);
						progressBar.done("Break complete!", 2000);
						progressBar = null;
					}
				}.start();
			}
		});
	}

	void playClip (Clip clip) {
		clip.stop();
		clip.setFramePosition(0);
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
}
