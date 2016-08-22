package com.kxw.demo.pipeline;

import org.jboss.netty.channel.*;

public class UpstreamHandlerA extends SimpleChannelUpstreamHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Channel ctxchannel = ctx.getChannel();
        Channel echannel = e.getChannel();
        System.out.println(1);
        System.out.println(ctxchannel.equals(echannel));//handle和event共享一个channel<br>       System.out.println("UpstreamHandlerA.messageReceived:" + e.getMessage());
        ctx.sendUpstream(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        System.out.println("UpstreamHandlerA.exceptionCaught:" + e.toString());
        e.getChannel().close();
    }

}