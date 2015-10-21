package com.kxw.demo.helloworld;



import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;


public class HelloWorldServerHandler extends SimpleChannelHandler {
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		e.getChannel().write("Hello, World,Kingson4Wu");
	}

	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		System.out.println("Unexpected exception from downstream."
				+ e.getCause());
		e.getChannel().close();
	}
}


