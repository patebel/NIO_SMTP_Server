import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class smtpserver {

	public static void main(String[] argv) throws Exception {

		// TODO Auto-generated method stub

		// rename with correct data
		String servername = null;
		int port = 80;

		// Declaration; Socket, input, output
		ServerSocketChannel sc = ServerSocketChannel.open();

		sc.socket().bind(new InetSocketAddress(servername, port));
		sc.configureBlocking(false);

		// size?
		int capacity = 8192;
		String filepath = "./user/test.txt";
		ByteBuffer bbuf = ByteBuffer.allocate(capacity);
		FileOutputStream fos = new FileOutputStream(filepath);

		while (true) {
			SocketChannel socketChannel = sc.accept();

			if (socketChannel != null) {
				// hier die Buffer verwenden?
			}
		}

	}

}
