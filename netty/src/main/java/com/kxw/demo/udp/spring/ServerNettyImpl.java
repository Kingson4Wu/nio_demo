package com.kxw.demo.udp.spring;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
@Component("serverNettyImpl")
public class ServerNettyImpl implements IServer {
	@Autowired
	@Qualifier("serverChannelPipelineFactory")
	private ServerChannelPipelineFactory pipelineFactory;
	private Channel channel;
	private static final Logger logger = Logger.getLogger(ServerNettyImpl.class
			.getName());
	
	public void start() {
		DatagramChannelFactory udpChannelFactory = new NioDatagramChannelFactory(
				Executors.newCachedThreadPool());
		ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(udpChannelFactory);
		bootstrap.setOption("reuseAddress", false);
		bootstrap.setOption("child.reuseAddress", false);
		bootstrap.setOption("readBufferSize", 1024);
		bootstrap.setOption("writeBufferSize", 1024);
		bootstrap.setPipelineFactory(this.pipelineFactory);
		SocketAddress serverAddress = new InetSocketAddress(5000);
		this.channel = bootstrap.bind(serverAddress);
		logger.info("server start on " + serverAddress);
	}
	public void restart() {
		this.stop();
		this.start();
	}
	public void stop() {
		if (this.channel != null) {
			this.channel.close().addListener(ChannelFutureListener.CLOSE);
		}
	}
}