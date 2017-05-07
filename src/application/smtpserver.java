package application;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class smtpserver {

	public static void main(String[] argv) throws Exception {

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(argv[0], Integer.parseInt(argv[1])));
		Selector selector = Selector.open();
		serverSocketChannel.configureBlocking(false);
		ByteBuffer buffer = ByteBuffer.allocate(256);

		while (true) {
			SocketChannel socketChannel = serverSocketChannel.accept(); // warum
																		// einmal
																		// socketchannel
																		// und
																		// einmal
																		// serversocketchannel?

			if (socketChannel != null) {

				SelectionKey ourkey = socketChannel.register(selector, SelectionKey.OP_CONNECT);

				Set<SelectionKey> selectedKeys = selector.selectedKeys();

				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {

					ourkey = keyIterator.next();

					if (ourkey.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel.
						SocketChannel client = serverSocketChannel.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);

					} else if (ourkey.isConnectable()) {
						// a connection was established with a remote server.

					} else if (ourkey.isReadable()) {
						// a channel is ready for reading
						SocketChannel client = (SocketChannel) ourkey.channel();
						client.read(buffer);
						buffer.flip();
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
