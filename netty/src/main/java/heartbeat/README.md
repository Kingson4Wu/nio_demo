netty4 如何建立心跳机制
<http://www.oschina.net/question/139577_146101>

<pre>
#心跳包机制
#   跳包之所以叫心跳包是因为：它像心跳一样每隔固定时间发一次，以此来告诉服务器，这个客户端还活着。事实上这是为了保持长连接，至于这个包的内容，是没有什么特别规定的，不过一般都是很小的包，或者只包含包头的一个空包。
#    在TCP的机制里面，本身是存在有心跳包的机制的，也就是TCP的选项：SO_KEEPALIVE。系统默认是设置的2小时的心跳频率。但是它检查不到机器断电、网线拔出、防火墙这些断线。而且逻辑层处理断线可能也不是那么好处理。一般，如果只是用于保活还是可以的。
#    心跳包一般来说都是在逻辑层发送空的echo包来实现的。下一个定时器，在一定时间间隔下发送一个空包给客户端，然后客户端反馈一个同样的空包回来，服务器如果在一定时间内收不到客户端发送过来的反馈包，那就只有认定说掉线了。
#    其实，要判定掉线，只需要send或者recv一下，如果结果为零，则为掉线。但是，在长连接下，有可能很长一段时间都没有数据往来。理论上说，这个连接是一直保持连接的，但是实际情况中，如果中间节点出现什么故障是难以知道的。更要命的是，有的节点（防火墙）会自动把一定时间之内没有数据交互的连接给断掉。在这个时候，就需要我们的心跳包了，用于维持长连接，保活。
#    在获知了断线之后，服务器逻辑可能需要做一些事情，比如断线后的数据清理呀，重新连接呀……当然，这个自然是要由逻辑层根据需求去做了。
#    总的来说，心跳包主要也就是用于长连接的保活和断线处理。一般的应用下，判定时间在30-40秒比较不错。如果实在要求高，那就在6-9秒。

</pre>

---
<https://www.yidianzixun.com/home?page=article&id=0Axm6TVs&up=256>
<pre>
Netty协议通信双方链路建立成功之后，双方可以进行全双工通信，无论客户端还是服务端。
都可以主动发送请求消息给对方，通信方式可以使TWOWAY或者ONE WAY。双方之间的心跳采用Ping-Pong机制，
当链路处于空闲状态时，客户端主动发送Ping消息给服务端，服务端接受到Ping消息后发送应答消息Pong给客户端，
如果客户端连线发送N条Ping 消息都没有接受到服务端返回的Pong消息，说明链路已经挂死或者对方处于异常状态，
客户端主动关闭连接，间隔周期T后发起重新连接，直到连接成功。
</pre>

Netty系列之Netty百万级推送服务设计要点
<http://www.infoq.com/cn/articles/netty-million-level-push-service-design-points>

---
+ Netty 的超时类型 IdleState 主要分为：

ALL_IDLE : 一段时间内没有数据接收或者发送
READER_IDLE ： 一段时间内没有数据接收
WRITER_IDLE ： 一段时间内没有数据发送
在 Netty 的 timeout 包下，主要类有：

IdleStateEvent ： 超时的事件
IdleStateHandler ： 超时状态处理
ReadTimeoutHandler ： 读超时状态处理
WriteTimeoutHandler ： 写超时状态处理

+ Netty 超时机制及心跳程序实现 : <https://mp.weixin.qq.com/s/srTZqWebbQo2eLmiuF_Tkg>

---

Netty 4.0 实现心跳检测和断线重连
<pre>
一 实现心跳检测
原理：当服务端每隔一段时间就会向客户端发送心跳包，客户端收到心跳包后同样也会回一个心跳包给服务端
一般情况下，客户端与服务端在指定时间内没有任何读写请求，就会认为连接是idle（空闲的）的。此时，客户端需要向服务端发送心跳消息，来维持服务端与客户端的链接。那么怎么判断客户端在指定时间里没有任何读写请求呢？netty中为我们提供一个特别好用的IdleStateHandler来干这个苦差事！

在服务端工作线程中添加：
Java代码  收藏代码
arg0.pipeline().addLast("ping", new IdleStateHandler(25, 15, 10,TimeUnit.SECONDS));


这个处理器，它的作用就是用来检测客户端的读取超时的，该类的第一个参数是指定读操作空闲秒数，第二个参数是指定写操作的空闲秒数，第三个参数是指定读写空闲秒数，当有操作操作超出指定空闲秒数时，便会触发UserEventTriggered事件。所以我们只需要在自己的handler中截获该事件，然后发起相应的操作即可（比如说发起心跳操作）。以下是我们自定义的handler中的代码：
Java代码  收藏代码
/**
     * 一段时间未进行读写操作 回调
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        // TODO Auto-generated method stub
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {

            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state().equals(IdleState.READER_IDLE)) {
                //未进行读操作
                System.out.println("READER_IDLE");
                // 超时关闭channel
                 ctx.close();

            } else if (event.state().equals(IdleState.WRITER_IDLE)) {


            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                //未进行读写
                System.out.println("ALL_IDLE");
                // 发送心跳消息
                MsgHandleService.getInstance().sendMsgUtil.sendHeartMessage(ctx);

            }

        }
    }


也就是说 服务端在10s内未进行读写操作，就会向客户端发送心跳包，客户端收到心跳包后立即回复心跳包给服务端，此时服务端就进行了读操作，也就不会触发IdleState.READER_IDLE（未读操作状态），若客户端异常掉线了，并不能响应服务端发来的心跳包，在25s后就会触发IdleState.READER_IDLE（未读操作状态），此时服务器就会将通道关闭

客户端代码略


二 客户端实现断线重连
原理当客户端连接服务器时
Java代码  收藏代码
bootstrap.connect(new InetSocketAddress(
                    serverIP, port));

会返回一个ChannelFuture的对象，我们对这个对象进行监听
代码如下：
Java代码  收藏代码
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.ld.qmwj.Config;
import com.ld.qmwj.MyApplication;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by zsg on 2015/11/21.
 */
public class MyClient implements Config {
    private static Bootstrap bootstrap;
    private static ChannelFutureListener channelFutureListener = null;

    public MyClient() {

    }


    // 初始化客户端
    public static void initClient() {

        NioEventLoopGroup group = new NioEventLoopGroup();

        // Client服务启动器 3.x的ClientBootstrap
        // 改为Bootstrap，且构造函数变化很大，这里用无参构造。
        bootstrap = new Bootstrap();
        // 指定EventLoopGroup
        bootstrap.group(group);
        // 指定channel类型
        bootstrap.channel(NioSocketChannel.class);
        // 指定Handler
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 创建分隔符缓冲对象
                ByteBuf delimiter = Unpooled.copiedBuffer("#"
                        .getBytes());
                // 当达到最大长度仍没找到分隔符 就抛出异常
                ch.pipeline().addLast(
                        new DelimiterBasedFrameDecoder(10000, true, false, delimiter));
                // 将消息转化成字符串对象 下面的到的消息就不用转化了
                //解码
                ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                ch.pipeline().addLast(new StringDecoder(Charset.forName("GBK")));
                ch.pipeline().addLast(new MyClientHandler());
            }
        });
        //设置TCP协议的属性
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);

        channelFutureListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                //  Log.d(Config.TAG, "isDone:" + f.isDone() + "     isSuccess:" + f.isSuccess() +
                //          "     cause" + f.cause() + "        isCancelled" + f.isCancelled());

                if (f.isSuccess()) {
                    Log.d(Config.TAG, "重新连接服务器成功");

                } else {
                    Log.d(Config.TAG, "重新连接服务器失败");
                    //  3秒后重新连接
                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 3, TimeUnit.SECONDS);
                }
            }
        };

    }

    //  连接到服务端
    public static void doConnect() {
        Log.d(TAG, "doConnect");
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(
                    serverIP, port));
            future.addListener(channelFutureListener);

        } catch (Exception e) {
            e.printStackTrace();
            //future.addListener(channelFutureListener);
            Log.d(TAG, "关闭连接");
        }

    }

}


监听到连接服务器失败时，会在3秒后重新连接（执行doConnect方法）

这还不够，当客户端掉线时要进行重新连接
在我们自己定义逻辑处理的Handler中
Java代码  收藏代码
@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(Config.TAG, "与服务器断开连接服务器");
        super.channelInactive(ctx);
        MsgHandle.getInstance().channel = null;

        //重新连接服务器
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                MyClient.doConnect();
            }
        }, 2, TimeUnit.SECONDS);
        ctx.close();
    }

</pre>

