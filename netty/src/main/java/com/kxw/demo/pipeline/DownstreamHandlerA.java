package com.kxw.demo.pipeline;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

public class DownstreamHandlerA extends SimpleChannelDownstreamHandler {
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        System.out.println(3);
        System.out.println("DownstreamHandlerA.handleDownstream");
        super.handleDownstream(ctx, e);
    }
}
