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
import java.nio.file.StandardOpenOption;
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

	public static int msgcnt1 = 0;
	public static int msgcnt2 = 0;
	public static int msgcnt3 = 0;
	public static int msgcnt4 = 0;

	/*
	 * String wird in US-ASCI kodierte Bytes umgewandelt und in den Bytebuffer
	 * "byte" geschrieben
	 */

	public static byte[] message_encoding(String code) throws IOException {
		try {
			messageCharset = Charset.forName("US-ASCII");
		} catch (UnsupportedCharsetException uce) {
			throw new IOException(uce.toString());
		}
		byte[] responsecode = code.getBytes(messageCharset);
		return responsecode;
	}

	/*
	 * empfangene Nachricht wird aus Buffer ausgelesen und in einen String zur
	 * Weiterverarbeitung umgewandelt
	 */

	public static String message_decoder(ByteBuffer read_buf) throws CharacterCodingException {
		CharsetDecoder decoder = messageCharset.newDecoder();
		CharBuffer charBuf = decoder.decode(read_buf);
		String extracted_text = charBuf.toString();

		return extracted_text;
	}

	/*
	 * Schreibt die empfangenen Nachrichten über einen Filechannel in diese
	 */

	public static boolean printMail(String mailcontent) throws IOException {
		Path file = Paths.get("./maillogging.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("US-ASCII"),
				StandardOpenOption.APPEND)) {
			writer.write(mailcontent);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}

	/*
	 * Erstellt und intitialisiert die Textdatei
	 */

	public static void fileInit() throws IOException {
		Path file = Paths.get("./maillogging.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("US-ASCII"))) {
			writer.write("MAIL FROM:;RCPT TO:;CONTENT;\r\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Absender bzw. Empfängeradresse wird aus der Nachricht extrahiert
	 */
	public static String extract_adress(String comp_resp) {
		String extracted_adress = "";

		for (int i = 0; i < comp_resp.length(); i++) {
			if (comp_resp.charAt(i) == ':') {
				for (int a = i + 2; a < comp_resp.length() - 2; a++) {
					extracted_adress += comp_resp.charAt(a);
				}
				break;
			}
		}

		return extracted_adress;
	}

	/*
	 * Zaehlt die Anzahl der empfangenen Mails pro Senderadresse
	 */
	public static int msg_cnt(String mailAdress) {

		if (mailAdress.equals("abc@def.edu")) {
			msgcnt1 += 1;
			return msgcnt1;
		} else if (mailAdress.equals("ghi@jkl.com")) {
			msgcnt2 += 1;
			return msgcnt2;
		} else if (mailAdress.equals("nmo@pqr.gov")) {
			msgcnt3 += 1;
			return msgcnt3;
		} else if (mailAdress.equals("stu@vwx.de")) {
			msgcnt4 += 1;
			return msgcnt4;
		} else {
			return 0;
		}

	}

	/*
	 * Extrahieren der Messagekennung
	 */
	public static String state_decoder(String Info) {

		String clientcode = "";

		for (int i = 0; i < 4; i++) {
			clientcode = clientcode + Info.charAt(i);
		}
		return clientcode;
	}

	/*
	 * ServerSocketChannel eröffnen, Selector initialisieren und Port und
	 * Adresse zuordnen
	 */
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

	/*
	 * Verbindungsaufbau und Anlegen des Kommunikationskanals (SocketChannel)
	 * und Interest-Set in der "register"-Funktion festlegen
	 */
	public static void accept(SelectionKey key, Selector selector) throws IOException {

		// Init server state
		smtpserverstate state = new smtpserverstate();
		state.setState(smtpserverstate.CONNECTED);

		// new socketChannel
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		try {
			socketChannel.register(selector, SelectionKey.OP_WRITE, state);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}
	}

	/*
	 * Socketchannel wir in Abhängigkeit vom Satus des Servers ausgelesen. Bem:
	 * Buffer mus vor gebrauch gecleart und anschließend geflippt werden, damit
	 * der Pointer an der richtigen Stelle steht Aktueller Serverstatus wird in
	 * "Previous State" geschrieben (notwendig, falls Client "HELP" sendet) und
	 * der Status geupdated
	 */
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
			state.saveMsg(extract_adress(client_response) + "_" + msg_cnt(extract_adress(client_response)) + ";");

		} else if (act_state.equals("RCPT")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.RCPTRECEIVED);
			state.saveMsg("\n" + extract_adress(client_response) + ";");

		} else if (act_state.equals("DATA")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.DATARECEIVED);

		} else if (act_state.equals("QUIT")) {
			state.setPreviousState(state.getState());
			state.setState(smtpserverstate.QUITRECEIVED);
			printMail(state.saveMsg(";\r\n"));
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
			state.saveMsg(client_response.substring(0, client_response.length() - 5));
		}

		try {
			socketChannel.register(selector, SelectionKey.OP_WRITE, state);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}

	}

	/*
	 * Responsecode wird auf Channel geschrieben
	 */
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

		// Buffer mit Messagestatus füllen und auf Channel schreiben
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

		// Init File IO
		fileInit();

		try {

			Selector selector = initSelector();

			// Dauerschleife: Channels die das Interestset bedienen, werden in
			// "readyChannels" geschrieben
			while (true) {

				int readyChannels = selector.select();

				if (readyChannels == 0)
					continue;

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				// keyIterator gibt die Möglichkeit die Sammlung der Keys zu
				// durchlaufen
				// Logikschleife bindet alle Channels ein, die das Interestset
				// bedienen und arbeitet sie je nach Key-Status ab
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel.
						accept(key, selector);
						// System.out.println("accept");

					} else if (key.isReadable()) {
						// a channel is ready for reading
						read(key, selector);
						// System.out.println("read");

					} else if (key.isWritable()) {
						// a channel is ready for writing
						write(key, selector);
						// System.out.println("write");

					}
					// key wird aus Readyset gelöscht
					keyIterator.remove();
				}

			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}

	}
}
