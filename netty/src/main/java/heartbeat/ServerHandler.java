package heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Arrays;
import java.util.Map;



public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void handlerAdded(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelActive(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub

        System.out.println("channelActive" + arg0.channel().remoteAddress());
        System.out.println(arg0.channel().hashCode());
        NettyChannelMap.add(arg0.channel().remoteAddress().toString(), arg0.channel());

        Map<String, Channel> map = NettyChannelMap.getAllMap();

        for (Map.Entry<String, Channel> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("channelInactive");

        arg0.close();
    }

    public static String[] bytesToHexStrings(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        String[] str = new String[src.length];

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str[i] = "0";
            }
            str[i] = hv;
        }
        return str;
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object arg1)
            throws Exception {
        ByteBuf buf = (ByteBuf) arg1;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        System.out.println("channelRead:" + Arrays.toString(bytesToHexStrings(req)));
        byte[] responseByteArray = hexStringToByte("6832003200684B15120200010C6100000200E416");
        NettyChannelMap.get("10.1.5.197").writeAndFlush(responseByteArray);
        System.out.println("send OK");

        // TODO Auto-generated method stub
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext arg0)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelRegistered(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext arg0)
            throws Exception {
        System.out.println("channelUnregistered");

        NettyChannelMap.remove(arg0.channel());

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext arg0)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        // TODO Auto-generated method stub
        //  System.out.println(NettyTCPServer.getMap().keySet());

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                /*读超时*/
                System.out.println("READER_IDLE 读超时");
                NettyChannelMap.remove(ctx.channel());
                System.out.println(ctx.channel() + "will be removed from map");
                System.out.println("print now map");
                Map<String, Channel> map = NettyChannelMap.getAllMap();

                for (Map.Entry<String, Channel> entry : map.entrySet()) {
                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }


                ctx.disconnect();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                /*写超时*/
                System.out.println("WRITER_IDLE 写超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.ALL_IDLE) {
                /*总超时*/
                System.out.println("ALL_IDLE 总超时");
                ctx.disconnect();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // TODO Auto-generated method stub
        System.out.println("channelRead0: " + (String) msg);
    }
}