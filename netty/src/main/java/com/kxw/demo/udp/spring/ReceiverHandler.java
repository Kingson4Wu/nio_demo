package com.kxw.demo.udp.spring;

import java.net.InetSocketAddress;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.springframework.stereotype.Component;

@Component("receiverHandler")
public class ReceiverHandler extends SimpleChannelHandler {
    private static final Logger logger = Logger.getLogger(ReceiverHandler.class.getName());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
        byte[] recByte = buffer.copy().toByteBuffer().array();
        String recMsg = new String(recByte);
        logger.info("server received:" + recMsg.trim());
        InetSocketAddress address = (InetSocketAddress)e.getRemoteAddress();

        logger.info("client address:" + address.getHostString() + ":" + address.getPort());
        Random random = new Random();
        //int backWord=random.nextInt(10000);
        //ChannelBuffer responseBuffer=new DynamicChannelBuffer(4);
        //responseBuffer.readBytes(backWord);

        //responseBuffer.readBytes(new String("ok").getBytes());
        e.getChannel().write(e.getMessage());

        //ctx.getChannel().write(new String("ok").getBytes());

        logger.info("client write done ... ");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        System.out.println("exceptionCaught" + e.getCause());
        logger.error("exceptionCaught" + e.getCause());
        //NotYetConnectedException
        if (e.getChannel() != null) {
            e.getChannel().close().addListener(ChannelFutureListener.CLOSE);
        }
    }
}