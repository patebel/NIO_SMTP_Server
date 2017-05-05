package application;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class smtpserver {

	private static String host = "localhost";
	private static int port = 5454;

	public static void main(String[] argv) throws Exception {

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

		serverSocketChannel.socket().bind(new InetSocketAddress(host, port));

		Selector selector = Selector.open();

		serverSocketChannel.configureBlocking(false);

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

					} else if (ourkey.isConnectable()) {
						// a connection was established with a remote server.

					} else if (ourkey.isReadable()) {
						// a channel is ready for reading

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
