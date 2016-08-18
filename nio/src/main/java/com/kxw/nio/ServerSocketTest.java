package com.kxw.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by kingsonwu on 16/1/14.
 */
public class ServerSocketTest {

    private static Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(9999));
        serverSocketChannel.configureBlocking(false);

        while (true) {
            SocketChannel socketChannel =
                    serverSocketChannel.accept();

            if (socketChannel != null) {
                //do something with socketChannel...
                System.out.println("-------------");
                System.out.println(socketChannel.getRemoteAddress());
                socketChannel.write(charset.encode("hello !!"));
                Thread.sleep(5000);
                socketChannel.write(charset.encode("world !!"));
                socketChannel.close();
                //socket 没有close则一直hold住连接
                //浏览器不会显示，要close才显示，telnet则会按程序逻辑显示
            }
        }
    }
}
/**
 * 通过 ServerSocketChannel.accept() 方法监听新进来的连接。当 accept()方法返回的时候，它返回一个包含新进来的连接的 SocketChannel。因此，accept()方法会一直阻塞到有新连接到达。
 * 通常不会仅仅只监听一个连接，在while循环中调用 accept()方法
 * ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接，返回的将是null。 因此，需要检查返回的SocketChannel是否是null。
 */