package application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Set;

public class smtpserver {

	private static Charset messageCharset = null;
	private static CharsetDecoder decoder = null;
	static ByteBuffer buf = ByteBuffer.allocate(4096);

	public static byte[] message_encoding(String code) throws IOException {
		try {
			messageCharset = Charset.forName("US-ASCII");
		} catch (UnsupportedCharsetException uce) {
			throw new IOException(uce.toString());
		}
		byte[] responsecode = code.getBytes(messageCharset);
		return responsecode;
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
		// new socketChannel
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(selector, SelectionKey.OP_WRITE);

	}

	private static void read(SelectionKey key, Selector selector) {
		// TODO Auto-generated method stub
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buf.clear();

		System.out.println("I Bim in read");

		// Flo's Aufgabe!
		/*
		 * ppppp client.configureBlocking(false); SelectionKey.OP_READ); //
		 * System.out.println(message_encoding("220"));
		 * buf.put(message_encoding("220")); buf.flip(); while
		 * (buf.hasRemaining()) { client.write(buf); }
		 */
	}

	private static void write(SelectionKey key, Selector selector) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buf.clear();
		try {
			buf.put(message_encoding("220"));
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
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			e.printStackTrace(System.out);
		}

	}

	public static void main(String[] argv) throws Exception {

		try {

			/*
			 * JL: wird von der Init-Section übernommen *
			 * 
			 * ServerSocketChannel serverSocketChannel =
			 * ServerSocketChannel.open(); serverSocketChannel.socket().bind(new
			 * InetSocketAddress(argv[0], // Integer.parseInt(argv[1])));
			 * serverSocketChannel.socket().bind(new
			 * InetSocketAddress("localhost", 5454)); Selector selector =
			 * Selector.open(); serverSocketChannel.configureBlocking(false);
			 * ByteBuffer buffer = ByteBuffer.allocate(256);
			 * 
			 * serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			 */

			Selector selector = initSelector();

			while (true) {

				int readyChannels = selector.select();

				if (readyChannels == 0)
					continue;

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						System.out.println("accept");
						accept(key, selector);
						// a connection was accepted by a ServerSocketChannel.

					}
					if (key.isConnectable()) {
						// a connection was established with a remote server.

					}
					if (key.isReadable()) {
						// a channel is ready for reading
						System.out.println("read");

						read(key, selector);

					}
					if (key.isWritable()) {
						// a channel is ready for writing
						System.out.println("write");

						write(key, selector);
					}
					keyIterator.remove();
				}

				/*
				 * SocketChannel socketChannel = serverSocketChannel.accept();
				 * if (socketChannel != null) {
				 * 
				 * socketChannel.configureBlocking(false); SelectionKey ourkey =
				 * socketChannel.register(selector, SelectionKey.OP_CONNECT);
				 * Set<SelectionKey> selectedKeys = selector.selectedKeys();
				 * Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				 * 
				 * 
				 * while (keyIterator.hasNext()) {
				 * 
				 * ourkey = keyIterator.next();
				 * 
				 * if (ourkey.isAcceptable()) { // a connection was accepted by
				 * a // ServerSocketChannel. ServerSocketChannel sock =
				 * (ServerSocketChannel) ourkey.channel(); SocketChannel client
				 * = sock.accept(); client.configureBlocking(false);
				 * client.register(selector, SelectionKey.OP_WRITE |
				 * SelectionKey.OP_READ); //
				 * System.out.println(message_encoding("220"));
				 * buf.put(message_encoding("220")); buf.flip(); while
				 * (buf.hasRemaining()) { client.write(buf); }
				 * 
				 * } else if (ourkey.isConnectable()) { // a connection was
				 * established with a remote // server.
				 * 
				 * } else if (ourkey.isReadable()) { // a channel is ready for
				 * reading SocketChannel client = (SocketChannel)
				 * ourkey.channel(); client.read(buffer); buffer.flip(); //
				 * Further processing of data client.write(buffer);
				 * buffer.clear();
				 * 
				 * } else if (ourkey.isWritable()) {
				 * 
				 * // a channel is ready for writing }
				 * 
				 * keyIterator.remove(); }
				 * 
				 * }
				 */
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
