
package com.esotericsoftware.clippy.usbuirt;

import static com.esotericsoftware.clippy.Win.Kernel32.*;

import com.esotericsoftware.clippy.util.SharedLibraryLoader;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public class UsbUirt {
	static public final int FORMAT_UUIRT = 0x0000;
	static public final int FORMAT_PRONTO = 0x0010;
	static public final int FORMAT_LEARN_FORCERAW = 0x0100;
	static public final int FORMAT_LEARN_FORCESTRUC = 0x0200;
	static public final int FORMAT_LEARN_FORCEFREQ = 0x0400;
	static public final int FORMAT_LEARN_FREQDETECT = 0x0800;

	static public final int ERROR_CONNECTION = 0x20000001;
	static public final int ERROR_COMMUNICATION = 0x20000002;
	static public final int ERROR_DRIVER_NOT_FOUND = 0x20000003;
	static public final int ERROR_INCOMPATIBLE_FIRMWARE = 0x20000004;
	static public final int ERROR_UNKNOWN = -1;

	static {
		new SharedLibraryLoader().load("uuirtdrv");
		Native.register(NativeLibrary.getInstance("uuirtdrv", W32APIOptions.DEFAULT_OPTIONS));
	}

	private Pointer handle;
	private boolean isLearning;
	private final Memory learnedCode = new Memory(32768);
	private final IntByReference abortLearning = new IntByReference();

	private final PUUCALLBACKPROC receive = new PUUCALLBACKPROC() {
		public void invoke (String IREventStr, Pointer userData) {
			receive(IREventStr);
		}
	};

	private final PLEARNCALLBACKPROC learn = new PLEARNCALLBACKPROC() {
		public void invoke (int progress, int sigQuality, long carrierFreq, Pointer userData) {
			learn(progress, sigQuality, carrierFreq);
		}
	};

	public void connect () throws Exception {
		long result = UUIRTOpen();
		if ((int)result == -1) {
			int error = Native.getLastError();
			switch (error) {
			case ERROR_DRIVER_NOT_FOUND:
				throw new Exception("Driver not found.");
			case ERROR_CONNECTION:
				throw new Exception("Unable to connect to device.");
			case ERROR_COMMUNICATION:
				throw new Exception("Unable to communicate with device.");
			case ERROR_INCOMPATIBLE_FIRMWARE:
				throw new Exception("Incompatible firmware.");
			}
			throw new Exception("Unknown error: " + error);
		}

		Pointer pointer = new Pointer(result);
		if (!UUIRTSetReceiveCallback(pointer, receive, null)) throw new Exception("Unable to set receive callback.");
		handle = pointer;
	}

	public boolean disconnect () {
		if (handle == null) return true;
		try {
			return UUIRTClose(handle);
		} finally {
			handle = null;
		}
	}

	/** Transmits the specified IR code.
	 * @param format See FORMAT_* constants.
	 * @param repeat Number of times to repeat the code. For a two-piece code the first piece is sent once followed by the second
	 *           piece repeat times.
	 * @param inactivityWaitTime Milliseconds since the last received IR activity to wait before sending the IR code. Normally pass
	 *           0.
	 * @param blockExecution If true, execution will be blocked until the IR code is sent. */
	public boolean transmit (String code, int format, int repeat, int inactivityWaitTime, boolean blockExecution) {
		if (handle == null) return false;
		Pointer doneEvent = null;
		if (!blockExecution) doneEvent = CreateEvent(null, false, false, "hUSBUIRTXAckEvent");
		return UUIRTTransmitIR(handle, code, format, repeat, inactivityWaitTime, doneEvent, null, null);
	}

	/** Transmits the specified IR code with the format=UIRT, repeat=1, inactivityWaitTime=0. */
	public boolean transmit (String code, boolean blockExecution) {
		return transmit(code, FORMAT_UUIRT, 1, 0, blockExecution);
	}

	/** Learns and returns a code in the specified format but only for the specified frequency or null if learning was aborted or
	 * failed. Blocks execution. */
	public String learn (int format, int forcedFrequency) {
		if (handle == null) return null;
		if (isLearning) return null;
		isLearning = true;
		try {
			abortLearning.setValue(0);
			if (!UUIRTLearnIR(handle, format, learnedCode, learn, null, abortLearning, forcedFrequency, null, null)) return null;
			return learnedCode.getString(0);
		} finally {
			isLearning = false;
		}
	}

	/** Learns and returns a code in the specified format or null if learning was aborted or failed. Blocks execution. */
	public synchronized String learn (int format) {
		return learn(format, 0);
	}

	/** Learns and returns a code in the Pront format or null if learning was aborted or failed. Blocks execution. The Pronto
	 * format is the most reliable, though it generates the longest codes. */
	public synchronized String learn () {
		return learn(FORMAT_PRONTO, 0);
	}

	public void abortLearn () {
		abortLearning.setValue(1);
	}

	public boolean isConnected () {
		return handle != null;
	}

	protected void receive (String code) {
	}

	protected void learn (int progress, int signalQuality, long carrierFrequency) {
	}

	static private native long UUIRTOpen ();

	static private native boolean UUIRTClose (Pointer hHandle);

	static private native boolean UUIRTSetReceiveCallback (Pointer hHandle, PUUCALLBACKPROC receiveProc, Pointer userData);

	static private native boolean UUIRTTransmitIR (Pointer hHandle, String IRCode, int codeFormat, int repeatCount,
		int inactivityWaitTime, Pointer hEvent, Pointer reserved0, Pointer reserved1);

	static private native boolean UUIRTLearnIR (Pointer hHandle, int codeFormat, Pointer IRCode, PLEARNCALLBACKPROC progressProc,
		Pointer userData, IntByReference pAbort, int param1, Pointer reserved0, Pointer reserved1);

	static private interface PUUCALLBACKPROC extends Callback {
		void invoke (String IREventStr, Pointer userData);
	}

	static private interface PLEARNCALLBACKPROC extends Callback {
		void invoke (int progress, int sigQuality, long carrierFreq, Pointer userData);
	}

	static public void main (String[] args) throws Exception {
		new UsbUirt();
	}
}
