package reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by kingsonwu on 17/7/1.
 * Netty Client 重连实现
 * <a href='https://mp.weixin.qq.com/s/IS0URmGMVQsFzYFFuIoi7A'>@link</a>
 */
public class Client {
    private EventLoopGroup loop = new NioEventLoopGroup();
    public static void main( String[] args ) {
        new Client().run();
    }
    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop) {
        if (bootstrap != null) {
            final MyInboundHandler handler = new MyInboundHandler(this);
            bootstrap.group(eventLoop);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(handler);
                }
            });
            bootstrap.remoteAddress("localhost", 8888);
            bootstrap.connect().addListener(new ConnectionListener(this));
        }
        return bootstrap;
    }
    public void run() {
        createBootstrap(new Bootstrap(), loop);
    }
}