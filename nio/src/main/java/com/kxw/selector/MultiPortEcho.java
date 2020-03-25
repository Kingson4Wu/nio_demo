package com.kxw.selector;

/**
 * Created by kingsonwu on 17/2/11.
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * <a href='http://www.importnew.com/2000.html'>@link</a>
 *
 * 选择器和异步IO：通过选择器来提高多路复用
 *
 * NIO新手一般都把它和“非阻塞输入/输出”联系在一起。NIO不仅仅只是非阻塞I/O，不过这种认知也不完全是错的：Java的基本I/O是阻塞式I/O——意味着它会一直等待到操作完成——然而，非阻塞或者异步I/O是NIO
 * 里最常用的一个特点，而非NIO的全部。
 *
 * NIO的非阻塞I/O是事件驱动的，并且在列表1里文件系统监听示例里进行了展示。这就意味着给一个I/O
 * 通道定义一个选择器（回调或者监听器），然后程序可以继续运行。当一个事件发生在这个选择器上时——例如接收到一行输入——选择器会“醒来”并且执行。所有的这些都是通过一个单线程来实现的，这和Java的标准I/O有着显著的差别的。
 *
 * 列表2里展示了使用NIO的选择器实现的一个多端口的网络程序echo-er，这里是修改了Greg
 * Travis在2003年创建的一个小程序（参考资源列表）。Unix和类Unix系统很早就已经实现高效的选择器，它是Java网络高性能编程模型的一个很好的参考模型。
 */

/**
 * 选择器和异步IO：通过选择器来提高多路复用

 NIO新手一般都把它和“非阻塞输入/输出”联系在一起。NIO不仅仅只是非阻塞I/O，不过这种认知也不完全是错的：Java的基本I/O是阻塞式I/O——意味着它会一直等待到操作完成——然而，非阻塞或者异步I/O是NIO
 里最常用的一个特点，而非NIO的全部。

 NIO的非阻塞I/O是事件驱动的，并且在列表1里文件系统监听示例里进行了展示。这就意味着给一个I/O
 通道定义一个选择器（回调或者监听器），然后程序可以继续运行。当一个事件发生在这个选择器上时——例如接收到一行输入——选择器会“醒来”并且执行。所有的这些都是通过一个单线程来实现的，这和Java的标准I/O有着显著的差别的。

 列表2里展示了使用NIO的选择器实现的一个多端口的网络程序echo-er，这里是修改了Greg
 Travis在2003年创建的一个小程序（参考资源列表）。Unix和类Unix系统很早就已经实现高效的选择器，它是Java网络高性能编程模型的一个很好的参考模型。
 */

/**
 * 编译这段代码，然后通过类似于java MultiPortEcho 8005
 * 8006这样的命令来启动它。一旦这个程序运行成功，启动一个简单的telnet或者其他的终端模拟器来连接8005和8006接口。你会看到这个程序会回显它接收到的所有字符——并且它是通过一个Java线程来实现的。
 */
public class MultiPortEcho {
    private int ports[];
    private ByteBuffer echoBuffer = ByteBuffer.allocate(1024);

    public MultiPortEcho(int ports[]) throws IOException {
        this.ports = ports;

        configure_selector();
    }

    private void configure_selector() throws IOException {
        // Create a new selector
        Selector selector = Selector.open();

        // Open a listener on each port, and register each one
        // with the selector
        for (int i = 0; i < ports.length; ++i) {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ServerSocket ss = ssc.socket();
            InetSocketAddress address = new InetSocketAddress(ports[i]);
            ss.bind(address);

            SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Going to listen on " + ports[i]);
        }

        while (true) {
            int num = selector.select();

            Set selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = (SelectionKey)it.next();

                if ((key.readyOps() & SelectionKey.OP_ACCEPT)
                    == SelectionKey.OP_ACCEPT) {
                    // Accept the new connection
                    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);

                    // Add the new connection to the selector
                    SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);
                    it.remove();

                    System.out.println("Got connection from " + sc);
                } else if ((key.readyOps() & SelectionKey.OP_READ)
                    == SelectionKey.OP_READ) {
                    // Read the data
                    SocketChannel sc = (SocketChannel)key.channel();

                    // Echo data
                    int bytesEchoed = 0;
                    while (true) {
                        echoBuffer.clear();

                        int number_of_bytes = sc.read(echoBuffer);

                        if (number_of_bytes <= 0) {
                            break;
                        }

                        echoBuffer.flip();

                        sc.write(echoBuffer);
                        bytesEchoed += number_of_bytes;
                    }

                    System.out.println("Echoed " + bytesEchoed + " from " + sc);

                    it.remove();
                }

            }
        }
    }

    static public void main(String args[]) throws Exception {
        if (args.length <= 0) {
            System.err.println("Usage: java MultiPortEcho port [port port ...]");
            System.exit(1);
        }

        int ports[] = new int[args.length];

        for (int i = 0; i < args.length; ++i) {
            ports[i] = Integer.parseInt(args[i]);
        }

        new MultiPortEcho(ports);
    }
}