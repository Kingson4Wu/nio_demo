package com.kxw.demo.pojo;


import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * 用POJO代替ChannelBuffer
 */

public class TimeServerHandler3 extends SimpleChannelHandler {

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        Persons person = new Persons("David Beckham 7 英格兰", 31, 10000.44);
        ChannelFuture future = e.getChannel().write(person);
        future.addListener(ChannelFutureListener.CLOSE);
    }
}  


