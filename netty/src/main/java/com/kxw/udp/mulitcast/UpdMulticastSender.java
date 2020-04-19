package com.kxw.udp.mulitcast;

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
import io.netty.util.NetUtil;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * 没成功！！！算了，先看mulitcast2
 */
public class UpdMulticastSender {
    private static int S_PORT = 1111; //Sender的端口
    private static int R_PORT = 2222; //Reciever的端口
    //目的地址
    private static InetSocketAddress remoteAddress = new InetSocketAddress("239.255.27.1", R_PORT);

    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者，也是线程池，线程池中初始化两个线程
        NioEventLoopGroup group = new NioEventLoopGroup(10);
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();
        //3.配置启动器
        bootstrap.group(group)                         //3.1指定group
            .channel(NioDatagramChannel.class)     //3.2指定channel
            .remoteAddress(remoteAddress)          //3.3指定目的地址
            .option(ChannelOption.IP_MULTICAST_IF, NetUtil.LOOPBACK_IF)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    //3.4在pipeline中加入编码器
                    nioDatagramChannel.pipeline().addLast(new MyUdpEncoder());
                }
            });
        try {
            //4.bind并返回一个channel
            final Channel channel = bootstrap.bind(S_PORT).sync().channel();
            //从线程池中取一个线程，每隔两秒发送一个报文
            int times = 0;
            final ScheduledFuture future = group.scheduleAtFixedRate(new Runnable() {
                private int times = 0;
                @Override
                public void run() {
                    //5.发送数据
                    String msg = "Send msg-" + times++;
                    channel.writeAndFlush(msg);
                    System.out.println(msg);
                }
            }, 2, 2, TimeUnit.SECONDS);

            //从线程池中取一个线程，10秒后运行代码，结束定时发送报文，并关闭channel
            group.schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("cancel sending msg.");
                    future.cancel(true);
                    System.out.println("close channel.");
                    //发送一个"close"，提醒接收方关闭channel
                    channel.writeAndFlush("close");
                    channel.close();
                }
            }, 10, TimeUnit.SECONDS);

            //6.等待channel的close
            channel.closeFuture().sync();
            //7.关闭group
            group.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //编码器，将要发送的消息封装到一个DatagramPacket中
    private static class MyUdpEncoder extends MessageToMessageEncoder<String> {
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
            byte[] bytes = s.getBytes(CharsetUtil.UTF_8 );
            ByteBuf buf = channelHandlerContext.alloc().buffer(bytes.length);
            buf.writeBytes(bytes);
            DatagramPacket packet = new DatagramPacket(buf, remoteAddress);
            list.add(packet);
        }
    }
}