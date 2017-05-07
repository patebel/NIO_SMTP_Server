package application;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
	static ByteBuffer buf = ByteBuffer.allocate(256);

	public static byte[] message_encoding(String code) {
		try {
			messageCharset = Charset.forName("US-ASCII");
		} catch (UnsupportedCharsetException uce) {
		}
		byte[] responsecode = code.getBytes(messageCharset);
		return responsecode;
	}

	public static void main(String[] argv) throws Exception {

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		// serverSocketChannel.socket().bind(new InetSocketAddress(argv[0],
		// Integer.parseInt(argv[1])));
		serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 5454));
		Selector selector = Selector.open();
		serverSocketChannel.configureBlocking(false);
		ByteBuffer buffer = ByteBuffer.allocate(256);

		while (true) {
			SocketChannel socketChannel = serverSocketChannel.accept();

			if (socketChannel != null) {

				SelectionKey ourkey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {

					ourkey = keyIterator.next();

					if (ourkey.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel.
						ServerSocketChannel sock = (ServerSocketChannel) ourkey.channel();
						SocketChannel client = sock.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
						// System.out.println(message_encoding("220"));
						buf.put(message_encoding("220"));
						buf.flip();
						client.write(buf);

					} else if (ourkey.isConnectable()) {
						// a connection was established with a remote server.

					} else if (ourkey.isReadable()) {
						// a channel is ready for reading
						SocketChannel client = (SocketChannel) ourkey.channel();
						client.read(buffer);
						buffer.flip();
						/* Further processing of data */
						client.write(buffer);
						buffer.clear();

					} else if (ourkey.isWritable()) {
						// a channel is ready for writing
					}

					keyIterator.remove();
				}

			}

			/*
			 * // TODO Auto-generated method stub
			 * 
			 * // rename with correct data String servername = null; int port =
			 * 80;
			 * 
			 * // Declaration; Socket, input, output ServerSocketChannel sc =
			 * ServerSocketChannel.open();
			 * 
			 * sc.socket().bind(new InetSocketAddress(servername, port));
			 * sc.configureBlocking(false);
			 * 
			 * // size? int capacity = 8192; String filepath =
			 * "./user/test.txt"; ByteBuffer bbuf =
			 * ByteBuffer.allocate(capacity); FileOutputStream fos = new
			 * FileOutputStream(filepath);
			 * 
			 * while (true) { SocketChannel socketChannel = sc.accept();
			 * 
			 * if (socketChannel != null) { // hier die Buffer verwenden? } }
			 */
		}
	}

}
