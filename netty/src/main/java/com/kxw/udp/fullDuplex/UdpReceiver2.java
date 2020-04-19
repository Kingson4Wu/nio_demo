package com.kxw.udp.fullDuplex;

import java.net.InetSocketAddress;
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
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

/**
 * 2.互相收发UDP报文
 */
public class UdpReceiver2 {
    private static int S_PORT = 1111; //Sender的端口
    private static int R_PORT = 2222; //Receiver的端口
    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者
        NioEventLoopGroup group = new NioEventLoopGroup();
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();
        //3.配置启动器
        bootstrap.group(group)//3.1指定group
            .channel(NioDatagramChannel.class)//3.2指定channel
            .option(ChannelOption.SO_BROADCAST,true)//3.3指定为广播模式
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    //3.4在pipeline中加入解码器，和编码器（用来发送UDP）
                    nioDatagramChannel.pipeline().addLast(new MyUdpDecoder()).addLast(new EncoderInReciever());
                }
            });
        try {
            //4.bind到指定端口，并返回一个channel，该端口就是监听UDP报文的端口
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

            /** 测试端口是否可以动态指定(测试可行)，不规范写法  */
           // S_PORT = datagramPacket.sender().getPort();

            //将接收到的消息改变一下再发出去
            msg += " from UdpReciever";
            channelHandlerContext.channel().writeAndFlush(msg);
        }
    }

    private static class EncoderInReciever extends MessageToMessageEncoder<String> {
        //这里是广播的地址和端口
        private InetSocketAddress remoteAddress = new InetSocketAddress("255.255.255.255", S_PORT);
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
            //remoteAddress = new InetSocketAddress("255.255.255.255", S_PORT);

            byte[] bytes = s.getBytes(CharsetUtil.UTF_8);
            ByteBuf buf = channelHandlerContext.alloc().buffer(bytes.length);
            buf.writeBytes(bytes);
            DatagramPacket packet = new DatagramPacket(buf, remoteAddress);
            list.add(packet);
        }
    }
}