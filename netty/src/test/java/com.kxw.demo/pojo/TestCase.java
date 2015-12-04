
package com.kxw.demo.pojo;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.junit.Test;

public class TestCase {
    public void testServer() {
        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new TimeEncoder(), new TimeServerHandler3());
            }
        });
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(new InetSocketAddress("localhost", 9999));
    }

    public void testClient() {
        //创建客户端channel的辅助类,发起connection请求   
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new TimeDecoder());
                //pipeline.addLast("encoder", new TimeEncoder());  
                pipeline.addLast("handler", new TimeClientHandler3());
                return pipeline;
            }
        });
        //创建无连接传输channel的辅助类(UDP),包括client和server  
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(
                "localhost", 9999));
        future.getChannel().getCloseFuture().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }

    @Test
    public void testNetty() {
        testServer();
        testClient();
    }
}  