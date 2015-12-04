package moco.internal;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;


public class MocoHttpServer extends BaseServerRunner {

    @Override
    protected ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("codec", new HttpServerCodec(4096, 8192, 8192, false));
                pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                pipeline.addLast("handler", new MocoHandler());
            }
        };
    }

}
