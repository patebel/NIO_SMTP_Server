package application;

import java.nio.ByteBuffer;

public class smtpserverstate {

	public final static int CONNECTED = 0;
	public final static int HELORECEIVED = 1;
	public final static int MAILFROMRECEIVED = 2;
	public final static int RCPTRECEIVED = 3;
	public final static int DATARECEIVED = 4;
	public final static int MSGRECEIVED = 5;
	public final static int QUITRECEIVED = 6;
	public final static int HELPRECEIVED = 7;

	private int state;
	private int msgcnt;
	private int previousState;
	private ByteBuffer buffer;

	String msgString = "";

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

	public String saveMsg(String Content) {
		msgString += Content;
		return msgString;
	}

	public int msg_cnt(int actcnt) {
		msgcnt = actcnt + 1;
		return msgcnt;
	}

	public int get_msg_cnt() {
		return msgcnt;
	}

}
