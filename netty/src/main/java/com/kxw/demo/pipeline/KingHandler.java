package com.kxw.demo.pipeline;


import org.jboss.netty.channel.*;

public class KingHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Channel ctxchannel = ctx.getChannel();
        Channel echannel = e.getChannel();
        System.out.println(6);
        System.out.println("KingHandler.messageReceived：" + e.getMessage());
        ctx.sendUpstream(e);
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        System.out.println(6);
        System.out.println("KingHandler.handleDownstream:");
        super.handleDownstream(ctx, e);
    }
}
