
package com.esotericsoftware.clippy.tobii;

public class EyeX {
	private final String interactorID;
	private long address;

	public EyeX (String interactorID) {
		this.interactorID = interactorID;
	}

	public boolean connect () {
		if (address != 0) throw new IllegalStateException("Already connected.");
		address = _connect(interactorID);
		return address != 0;
	}

	public boolean disconnect () {
		if (address == 0) throw new IllegalStateException("Already disconnected.");
		boolean success = _disconnect(address);
		address = 0;
		return success;
	}

	private void event (int eventIndex) {
		event(Event.values[eventIndex]);
	}

	protected void event (Event event) {
	}

	protected void gazeEvent (double timestamp, double x, double y) {
	}

	protected void eyeEvent (double timestamp, boolean hasLeftEyePosition, boolean hasRightEyePosition, double leftEyeX,
		double leftEyeY, double leftEyeZ, double leftEyeXNormalized, double leftEyeYNormalized, double leftEyeZNormalized,
		double rightEyeX, double rightEyeY, double rightEyeZ, double rightEyeXNormalized, double rightEyeYNormalized,
		double rightEyeZNormalized) {
	}

	private native long _connect (String id);

	private native boolean _disconnect (long address);

	static public enum Event {
		connectTrying, connected, connectFailed, serverTooHigh, serverTooLow, commitSnapshotFailed, disconnected, eventError;

		static Event[] values = values();
	}
}
