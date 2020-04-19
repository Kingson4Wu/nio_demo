package com.kxw.udp.simple;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

/**
 * 1.最简单的接收者和发送者
 *
 * lsof -i:2222
 * COMMAND  PID      USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
 * java    3649 kingsonwu   66u  IPv6 0xb826f29a2025ce65      0t0  UDP *:rockwell-csp2
 *
 * netstat -an|grep 2222
 * udp46      0      0  *.2222                 *.*
 */
public class UdpReceiver {
    private static int R_PORT = 2222; //Reciever的端口
    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者
        NioEventLoopGroup group = new NioEventLoopGroup();
        System.out.println("NioEventLoopGroup in main :"+group);
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();
        //3.配置启动器
        bootstrap.group(group)//3.1指定group
            .channel(NioDatagramChannel.class)//3.2指定channel
            .option(ChannelOption.SO_BROADCAST,true)//3.3指定为广播模式
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    nioDatagramChannel.pipeline().addLast(new MyUdpDecoder());//3.4在pipeline中加入解码器
                }
            });
        try {
            //4.bind到指定端口，并返回一个channel，该端口就是监听UDP报文的端口 R_PORT =0 ,则系统自动分配端口
            Channel channel = bootstrap.bind(R_PORT).sync().channel();
            //5.等待channel的close
            channel.closeFuture().sync();
            //6.关闭group
            group.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class MyUdpDecoder extends MessageToMessageDecoder<DatagramPacket> {
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
            ByteBuf buf = datagramPacket.content();
            String msg = buf.toString(CharsetUtil.UTF_8);

            System.out.println(
                "client:" + datagramPacket.sender().getHostString() + ":" + datagramPacket.sender().getPort()
                    + ", UdpReceiver :" + msg);
        }
    }
}