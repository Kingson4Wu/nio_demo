package com.kxw.demo.ChannelInboundHandlerAdapter;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * 1. 创建一个ServerBootstrap实例
 * 2. 创建一个EventLoopGroup来处理各种事件，如处理链接请求，发送接收数据等。
 * 3. 定义一个远程InetSocketAddress好让client连接
 * 4. 当连接完毕之后，Handler会被运行一次
 * 5. 全部准备好之后调用ServerBootstrap.connect()方法连接Server
 */
public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.remoteAddress(new InetSocketAddress(host, port));
            b.handler(new ChannelInitializer<SocketChannel>() {

                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new EchoClientHandler());
                }
            });
            ChannelFuture f = b.connect().sync();
            f.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("client connected");
                    } else {
                        System.out.println("server attemp failed");
                        future.cause().printStackTrace();
                    }

                }
            });
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {

        new EchoClient("127.0.0.1", 3331).start();
    }
}