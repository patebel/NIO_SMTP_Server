package application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class smtpserver {

	// responsecodes festlegen
	private static String servicereadymsg = "220 \r\n";
	private static String okmsg = "250 \r\n";
	private static String startmailinputmsg = "354 \r\n";
	private static String closingchannelmsg = "221 \r\n";
	private static String helpmsg = "214 \r\n";

	private static Charset messageCharset = null;
	static ByteBuffer buf = ByteBuffer.allocate(8192);

	public static byte[] message_encoding(String code) throws IOException {
		try {
			messageCharset = Charset.forName("US-ASCII");
		} catch (UnsupportedCharsetException uce) {
			throw new IOException(uce.toString());
		}
		byte[] responsecode = code.getBytes(messageCharset);
		return responsecode;
	}

	public static String message_decoder(ByteBuffer read_buf) throws CharacterCodingException {
		CharsetDecoder decoder = messageCharset.newDecoder();
		CharBuffer charBuf = decoder.decode(read_buf);
		String extracted_text = charBuf.toString();

		return extracted_text;
	}

	public static boolean printMail(String mailcontent) throws IOException {
		Path file = Paths.get("./example.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("US-ASCII"))) {
			writer.write(mailcontent);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}

	public static String state_decoder(String Info) {

		String clientcode = "";

		for (int i = 0; i < 4; i++) {
			clientcode = clientcode + Info.charAt(i);
		}
		return clientcode;
	}

	// JL: OOP neue Methode
	public static Selector initSelector() throws IOException {
		// create a new selector
		Selector selector = Selector.open();

		// create a non-blocking server socket channel
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		// Binding
		serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 5454));

		// Register and Accept server socket
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		return selector;
	}

	public static void accept(SelectionKey key, Selector selector) throws IOException {

		// Init server state
		smtpserverstate state = new smtpserverstate();
		state.setState(smtpserverstate.CONNECTED);

		// new socketChannel
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		try {
			socketChannel.register(selector, SelectionKey.OP_WRITE, state);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}
	}

	public static void read(SelectionKey key, Selector selector) throws IOException {

		// retrieve the server state from the key attachment
		smtpserverstate state = (smtpserverstate) key.attachment();

		SocketChannel socketChannel = (SocketChannel) key.channel();
		buf.clear();
		socketChannel.read(buf);
		buf.flip();

		String client_response = message_decoder(buf);
		String act_state = state_decoder(client_response);
		System.out.println(client_response + "\n" + act_state);

		if (act_state.equals("HELO")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.HELORECEIVED);

		} else if (act_state.equals("MAIL")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.MAILFROMRECEIVED);
			// TODO Textverarbeitung

		} else if (act_state.equals("RCPT")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.RCPTRECEIVED);
			// TODO Textverarbeitung

		} else if (act_state.equals("DATA")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.DATARECEIVED);

		} else if (act_state.equals("QUIT")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.QUITRECEIVED);
			socketChannel.close();
			key.cancel();
			return;

		} else if (act_state.equals("HELP")) {

			if (state.getState() != smtpserverstate.HELPRECEIVED) {
				state.setPreviousState(state.getState());
				state.setState(smtpserverstate.HELPRECEIVED);
			}

		} else {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.MSGRECEIVED);
			// TODO Textverarbeitung

		}

		// key.attach(state);

		try {
			socketChannel.register(selector, SelectionKey.OP_WRITE, state);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}

	}

	private static void write(SelectionKey key, Selector selector) {

		// retrieve the server state from the key attachment
		smtpserverstate state = (smtpserverstate) key.attachment();

		SocketChannel socketChannel = (SocketChannel) key.channel();
		String msgstatus = null;

		switch (state.getState()) {
		case smtpserverstate.CONNECTED:
			msgstatus = servicereadymsg;
			break;
		case smtpserverstate.HELORECEIVED:
			msgstatus = okmsg;
			break;
		case smtpserverstate.MAILFROMRECEIVED:
			msgstatus = okmsg;
			break;
		case smtpserverstate.RCPTRECEIVED:
			msgstatus = okmsg;
			break;
		case smtpserverstate.DATARECEIVED:
			msgstatus = startmailinputmsg;
			break;
		case smtpserverstate.MSGRECEIVED:
			msgstatus = okmsg;
			break;
		case smtpserverstate.QUITRECEIVED:
			msgstatus = closingchannelmsg;
			break;
		case smtpserverstate.HELPRECEIVED:
			msgstatus = helpmsg;
			break;
		}

		buf.clear();
		try {
			buf.put(message_encoding(msgstatus));
		} catch (IOException IO) {
			IO.printStackTrace(System.out);
		}
		buf.flip();
		while (buf.hasRemaining()) {
			try {
				socketChannel.write(buf);
			} catch (IOException IO) {
				IO.printStackTrace(System.out);
			}
		}
		buf.clear();

		try {
			socketChannel.register(selector, SelectionKey.OP_READ, state);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Main program loop
	 * 
	 * @param argv
	 *            the parameters to start the program with
	 */
	public static void main(String[] argv) throws Exception {

		try {

			Selector selector = initSelector();

			while (true) {

				int readyChannels = selector.select();

				// String mailcontent = "hallo I bims in Datai";

				if (readyChannels == 0)
					continue;

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				// printMail(mailcontent);

				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel.
						accept(key, selector);
						System.out.println("accept");

						// key.attach(state);

					} else if (key.isReadable()) {
						// a channel is ready for reading
						read(key, selector);
						System.out.println("read");

					} else if (key.isWritable()) {
						// a channel is ready for writing
						write(key, selector);
						System.out.println("write");

					}
					keyIterator.remove();
				}

			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}

		/*
		 * // TODO Auto-generated method stub
		 * 
		 * // rename with correct data String servername = null; int port = 80;
		 * 
		 * // Declaration; Socket, input, output ServerSocketChannel sc =
		 * ServerSocketChannel.open();
		 * 
		 * sc.socket().bind(new InetSocketAddress(servername, port));
		 * sc.configureBlocking(false);
		 * 
		 * // size? int capacity = 8192; String filepath = "./user/test.txt";
		 * ByteBuffer bbuf = ByteBuffer.allocate(capacity); FileOutputStream fos
		 * = new FileOutputStream(filepath);
		 * 
		 * while (true) { SocketChannel socketChannel = sc.accept();
		 * 
		 * if (socketChannel != null) { // hier die Buffer verwenden? } }
		 */
	}
}
