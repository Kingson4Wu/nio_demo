package com.kxw.demo.pipeline;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * http://www.cnblogs.com/montya/archive/2012/12/26/2834279.html
 */
public class Server {
    public static void main(String args[]) {
        ServerBootstrap bootsrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors
                        .newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootsrap.setPipelineFactory(new PipelineFactoryTest());
        bootsrap.bind(new InetSocketAddress(8888));
    }
}