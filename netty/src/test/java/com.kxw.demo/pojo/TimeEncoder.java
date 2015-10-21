package com.kxw.demo.pojo;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class TimeEncoder extends SimpleChannelHandler {
    private final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Persons person = (Persons) e.getMessage();
        buffer.writeInt(person.getName().getBytes("GBK").length);
        buffer.writeBytes(person.getName().getBytes("GBK"));
        buffer.writeInt(person.getAge());
        buffer.writeDouble(person.getSalary());
        Channels.write(ctx, e.getFuture(), buffer);
    }
}  