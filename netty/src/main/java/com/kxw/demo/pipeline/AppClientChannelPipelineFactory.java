package com.kxw.demo.pipeline;

import org.jboss.netty.channel.*;

public class AppClientChannelPipelineFactory implements ChannelPipelineFactory {

    public ChannelPipeline getPipeline() throws Exception {

        ChannelPipeline pipeline = Channels.pipeline();

        //pipeline.addLast("encode", new StringEncoder());

        pipeline.addLast("handler", new AppStoreClientHandler());
        return pipeline;

    }

}
