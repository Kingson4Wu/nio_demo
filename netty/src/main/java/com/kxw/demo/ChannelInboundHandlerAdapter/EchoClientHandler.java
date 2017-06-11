package com.kxw.demo.ChannelInboundHandlerAdapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.CharsetUtil;

@Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    /**
     * 此方法会在连接到server后被调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    /**
     * 此方法会在接收到server数据后调用
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("Client received: " + ByteBufUtil.hexDump(in.readBytes(in.readableBytes())));
    }

    /**
     * 捕捉到异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 须要注意的是channelRead0()方法，此方法接收到的可能是一些数据片段，比方server发送了5个字节数据，
     * Client端不能保证一次所有收到，比方第一次收到3个字节，第二次收到2个字节。
     * 我们可能还会关心收到这些片段的顺序是否可发送顺序一致，这要看详细是什么协议，比方基于TCP协议的字节流是能保证顺序的。
     另一点，在Client端我们的业务Handler继承的是SimpleChannelInboundHandler，而在server端继承的是ChannelInboundHandlerAdapter，
     那么这两个有什么差别呢？最基本的差别就是SimpleChannelInboundHandler
     在接收到数据后会自己主动release掉数据占用的Bytebuffer资源(自己主动调用Bytebuffer.release())。
     而为何server端不能用呢，由于我们想让server把client请求的数据发送回去，而server端有可能在channelRead方法返回前还没有写完数据，
     因此不能让它自己主动release。
     */

}