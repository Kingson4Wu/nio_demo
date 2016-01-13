package com.kxw.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by kingsonwu on 16/1/14.
 */
public class SocketTest {
    /**
     * Java NIO中的SocketChannel是一个连接到TCP网络套接字的通道。可以通过以下2种方式创建SocketChannel：
     * 打开一个SocketChannel并连接到互联网上的某台服务器。
     * 一个新连接到达ServerSocketChannel时，会创建一个SocketChannel。
     */
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("http://baidu.com", 80));

        while (!socketChannel.finishConnect()) {
            //wait, or do something else...

            //从 SocketChannel 读取数据
            //要从SocketChannel中读取数据，调用一个read()的方法之一。
            ByteBuffer buf = ByteBuffer.allocate(48);
            int bytesRead = socketChannel.read(buf);

            while (bytesRead != -1) {

                System.out.println("Read " + bytesRead);
                buf.flip();

                while (buf.hasRemaining()) {
                    System.out.print((char) buf.get());
                }
                buf.clear();
                bytesRead = socketChannel.read(buf);
            }
        }
        socketChannel.close();
    }
}
