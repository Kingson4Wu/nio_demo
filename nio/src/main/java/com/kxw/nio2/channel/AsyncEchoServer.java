package com.kxw.nio2.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * <a href='http://zjumty.iteye.com/'>@link</a>
 * 异步socket服务器
 */
public class AsyncEchoServer {

    private AsynchronousServerSocketChannel serverChannel;

    public void start() throws IOException {
        System.out.println(String.format("start: name: %s", Thread.currentThread().getName()));
        serverChannel = AsynchronousServerSocketChannel.open();
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverChannel.bind(new InetSocketAddress(8000));
        serverChannel.accept(serverChannel, new Acceptor());
        //以前是while(true){accept();},现在不是了,所以要在回调函数中,回调接收新的连接成功之后继续accept,这样才能形成一个不断接收新连接的循环
    }

    class Acceptor implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

        private final ByteBuffer buffer = ByteBuffer.allocate(1024);

        public Acceptor() {
            System.out.println("an acceptor has created.");
        }

        public void completed(final AsynchronousSocketChannel channel, AsynchronousServerSocketChannel serverChannel) {
            System.out.println(String.format("write: name: %s", Thread.currentThread().getName()));
            channel.read(buffer, channel, new Reader(buffer));
            serverChannel.accept(serverChannel, new Acceptor());
        }

        public void failed(Throwable exception, AsynchronousServerSocketChannel serverChannel) {
            throw new RuntimeException(exception);
        }
    }

    class Reader implements CompletionHandler<Integer, AsynchronousSocketChannel> {

        private ByteBuffer buffer;

        public Reader(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public void completed(Integer result, AsynchronousSocketChannel channel) {
            System.out.println(String.format("read: name: %s", Thread.currentThread().getName()));
            if (result != null && result < 0) {
                try {
                    channel.close();
                    return;
                } catch (IOException ignore) {
                }
            }
            buffer.flip();
            channel.write(buffer, channel, new Writer(buffer));
        }

        public void failed(Throwable exception, AsynchronousSocketChannel channel) {
            throw new RuntimeException(exception);
        }
    }

    class Writer implements CompletionHandler<Integer, AsynchronousSocketChannel> {

        private ByteBuffer buffer;

        public Writer(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public void completed(Integer result, AsynchronousSocketChannel channel) {
            System.out.println(String.format("write: name: %s", Thread.currentThread().getName()));
            buffer.clear();
            channel.read(buffer, channel, new Reader(buffer));
        }

        public void failed(Throwable exception, AsynchronousSocketChannel channel) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new AsyncEchoServer().start();
        while (true) {
            Thread.sleep(1000L);
        }
    }
}