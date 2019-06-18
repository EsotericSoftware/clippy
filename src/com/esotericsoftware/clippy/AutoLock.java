
package com.esotericsoftware.clippy;

import java.util.Timer;
import java.util.TimerTask;

import com.esotericsoftware.clippy.Win.LASTINPUTINFO;

public class AutoLock {
	static final Clippy clippy = Clippy.instance;

	final LASTINPUTINFO lastInputInfo = new LASTINPUTINFO();
	long inactiveTime;
	Timer timer;

	public AutoLock () {
		if (clippy.config.autoLockSeconds <= 0) return;
		timer = new Timer("AutoLock", true);

		int checkInterval = Math.min(5 * 1000, clippy.config.autoLockSeconds);
		timer.schedule(new TimerTask() {
			public void run () {
				if (getInactiveSeconds() >= clippy.config.autoLockSeconds) Win.User32.LockWorkStation();
			}
		}, checkInterval, checkInterval);
	}

	public void resetTimeout () {
		if (clippy.config.autoLockSeconds > 0) inactiveTime = Win.Kernel32.GetTickCount();
	}

	long getInactiveSeconds () {
		Win.User32.GetLastInputInfo(lastInputInfo);
		inactiveTime = Math.max(inactiveTime, lastInputInfo.dwTime);
		return (Win.Kernel32.GetTickCount() - inactiveTime) / 1000;
	}
}
