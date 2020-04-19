package com.kxw.udp.mulitcast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

public class UdpMulticastReceiver {
    private static int R_PORT = 2222; //Reciever的端口
    //本地ip和端口
    private static InetSocketAddress groupAddress = new InetSocketAddress("239.255.27.1", R_PORT);
    public static void main(String[] args) {
        //1.NioEventLoopGroup是执行者
        NioEventLoopGroup group = new NioEventLoopGroup();
        //2.启动器
        Bootstrap bootstrap = new Bootstrap();

        Enumeration<InetAddress> addresses = NetUtil.LOOPBACK_IF.getInetAddresses();
        InetAddress localAddress = null;
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address instanceof Inet4Address){
                localAddress = address;
            }
        }
        //3.配置启动器
        bootstrap.group(group)//3.1指定group
            .channelFactory(new ChannelFactory<NioDatagramChannel>() {

                public NioDatagramChannel newChannel() {
                    return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                }
            })//3.2指定channel
            .localAddress(localAddress, R_PORT)       //3.3指定本地地址
            .option(ChannelOption.IP_MULTICAST_IF, NetUtil.LOOPBACK_IF)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                    //3.4在pipeline中加入解码器，和编码器（用来发送UDP）
                    nioDatagramChannel.pipeline().addLast(new MyUdpDecoder());
                }
            });
        try {
            //4.bind到指定端口，并返回一个channel，该端口就是监听UDP报文的端口
            Channel channel = bootstrap.bind(R_PORT).sync().channel();

            ((NioDatagramChannel)channel).joinGroup(groupAddress, NetUtil.LOOPBACK_IF).sync();
            //5.等待channel的close
            channel.closeFuture().await();

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
            InetSocketAddress sender = datagramPacket.sender();
            InetSocketAddress recipient = datagramPacket.recipient();
            String msg = buf.toString(CharsetUtil.UTF_8);
            String msgInfo = msg +",sender = "+sender+", recipient = "+recipient;
            System.out.println("UdpReciever :"+msgInfo);
            //若接收到"close"，则关闭channel
            if (msg.equals("close")) {
                System.out.println("close channel.");
                channelHandlerContext.channel().close();
            }
        }
    }
}