package com.kxw.udp.otherThread;

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
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * 3.在其他线程中收发报文
 * 程序员也应善用EventLoopGroup来进行多线程代码的实现和管理。同时，本例子中，使用了ScheduledFuture 来优雅的结束了Channel。
 */
public class UdpSenderInOtherThread {
    private static int S_PORT = 1111; //Sender的端口
    private static int R_PORT = 2222; //Reciever的端口

    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者，也是线程池，线程池中初始化两个线程
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();
        //3.配置启动器
        bootstrap.group(group)                         //3.1指定group
            .channel(NioDatagramChannel.class)     //3.2指定channel
            .option(ChannelOption.SO_BROADCAST, true) //3.3指定为广播模式
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    //3.4在pipeline中加入编码器，和解码器（用来处理返回的消息）
                    nioDatagramChannel.pipeline().addLast(new MyUdpEncoder()).addLast(new DecoderInSender());
                }
            });
        try {
            //4.bind并返回一个channel
            final Channel channel = bootstrap.bind(S_PORT).sync().channel();
            System.out.println("main thread is "+Thread.currentThread());

            //从线程池中取一个线程，每隔两秒发送一个报文,group其实就是一个ServiceExecutor
            int times = 0;
            final ScheduledFuture future = group.scheduleAtFixedRate(new Runnable() {
                private int times = 0;
                @Override
                public void run() {
                    //5.发送数据
                    System.out.println(Thread.currentThread()+ " in scheduleAtFixedRate.");
                    channel.writeAndFlush("Send msg-" + times++);
                    System.out.println("Send msg-" + times);
                }
            }, 2, 2, TimeUnit.SECONDS);

            //从线程池中取一个线程，10秒后运行代码，结束定时发送报文，并关闭channel
            group.schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("cancel sending msg.");
                    future.cancel(true);
                    System.out.println("close channel.");
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
            //查看encode执行的线程，是group线程池中的某个线程
            System.out.println(Thread.currentThread()+ " in encode.");
        }
    }

    //解码器，用来处理返回的数据
    private static class DecoderInSender extends MessageToMessageDecoder<DatagramPacket> {
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
            ByteBuf buf = datagramPacket.content();
            String msg = buf.toString(CharsetUtil.UTF_8);
            System.out.println("Sender receive: " + msg);
        }
    }
}