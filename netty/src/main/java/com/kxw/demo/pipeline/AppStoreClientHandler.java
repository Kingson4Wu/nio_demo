package com.kxw.demo.pipeline;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class AppStoreClientHandler extends SimpleChannelUpstreamHandler {

    @Override

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

    }

}