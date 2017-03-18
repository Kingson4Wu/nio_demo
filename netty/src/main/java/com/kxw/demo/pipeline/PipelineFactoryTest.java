package com.kxw.demo.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class PipelineFactoryTest implements ChannelPipelineFactory {

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("1", new UpstreamHandlerA());
        pipeline.addLast("2", new UpstreamHandlerB());
        pipeline.addLast("3", new DownstreamHandlerA());
        pipeline.addLast("4", new DownstreamHandlerB());
        pipeline.addLast("timeout", new ReadTimeoutHandler(new HashedWheelTimer(), 10));
        pipeline.addLast("6", new KingHandler());
        pipeline.addLast("5", new UpstreamHandlerX());
        return pipeline;
    }
}
