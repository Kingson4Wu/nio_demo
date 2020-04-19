package com.kxw.udp.simple;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

public class UdpSender {
    private static int S_PORT = 1111; //Sender的端口
    private static int R_PORT = 2222; //Receiver的端口

    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者
        NioEventLoopGroup group = new NioEventLoopGroup();
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();
        //3.配置启动器
        bootstrap.group(group)                         //3.1指定group
            .channel(NioDatagramChannel.class)     //3.2指定channel
            .option(ChannelOption.SO_BROADCAST,true) //3.3指定为广播模式
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    nioDatagramChannel.pipeline().addLast(new MyUdpEncoder());   //3.4在pipeline中加入编码器
                }
            });
        try {
            //4.bind并返回一个channel
            Channel channel = bootstrap.bind(S_PORT).sync().channel();
            for (int i = 0; i < 10; i++) {
                TimeUnit.SECONDS.sleep(1);
                //5.发送数据
                channel.writeAndFlush("hello labali !" + i);
                System.out.println("Send msg :" + i);
            }

            //6.等待channel的close
            channel.closeFuture().sync();
            //7.关闭group
            group.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //编码器，将要发送的消息（这里是一个String）封装到一个DatagramPacket中
    private static class MyUdpEncoder extends MessageToMessageEncoder<String> {
        //这里是广播的地址和端口
        private InetSocketAddress remoteAddress = new InetSocketAddress("255.255.255.255", R_PORT);
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
            byte[] bytes = s.getBytes(CharsetUtil.UTF_8);
            ByteBuf buf = channelHandlerContext.alloc().buffer(bytes.length);
            buf.writeBytes(bytes);
            DatagramPacket packet = new DatagramPacket(buf, remoteAddress);
            list.add(packet);
        }
    }
}
