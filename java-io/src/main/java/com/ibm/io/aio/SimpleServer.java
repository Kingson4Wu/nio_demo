package com.ibm.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

public class SimpleServer {
    public SimpleServer(int port) throws IOException {
        final AsynchronousServerSocketChannel listener =
                AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
        //监听消息，收到后启动 Handle 处理模块
        listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel ch, Void att) {
                listener.accept(null, this);// 接受下一个连接
                handle(ch);// 处理当前连接
            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }

        });
    }

    public void handle(AsynchronousSocketChannel ch) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);//开一个 Buffer
        try {
            ch.read(byteBuffer).get();//读取输入
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        byteBuffer.flip();
        System.out.println(byteBuffer.get());
// Do something
    }

}