package application;

import java.nio.ByteBuffer;

public class smtpserverstate {

	public final static int CONNECTED = 0;
	public final static int READYSENT = 1;
	public final static int HELORECEIVED = 2;
	public final static int MAILFROMRECEIVED = 3;
	public final static int RCPTRECEIVED = 4;
	public final static int DATARECEIVED = 5;
	public final static int QUITRECEIVED = 6;
	public final static int HELPRECEIVED = 7;

	private int state;
	private int previousState;
	private ByteBuffer buffer;

	public smtpserverstate() {

		this.state = CONNECTED;
		this.buffer = ByteBuffer.allocate(8192);
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getPreviousState() {
		return previousState;
	}

	public void setPreviousState(int previousState) {
		this.previousState = previousState;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

}
