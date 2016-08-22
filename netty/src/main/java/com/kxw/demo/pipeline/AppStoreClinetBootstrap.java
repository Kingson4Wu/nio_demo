package com.kxw.demo.pipeline;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppStoreClinetBootstrap {
    public static void main(String args[]){
        ExecutorService bossExecutor = Executors.newCachedThreadPool();
        ExecutorService workerExecutor = Executors.newCachedThreadPool();
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
        ClientBootstrap bootstarp = new ClientBootstrap(channelFactory);
        bootstarp.setPipelineFactory(new AppClientChannelPipelineFactory());

        ChannelFuture future = bootstarp.connect(new InetSocketAddress("localhost", 8888));
        future.awaitUninterruptibly();
        if(future.isSuccess()){
            String msg = "hello word";
            ChannelBuffer buffer = ChannelBuffers.buffer(msg.length());
            buffer.writeBytes(msg.getBytes());
            future.getChannel().write(buffer);
        }
    }
}