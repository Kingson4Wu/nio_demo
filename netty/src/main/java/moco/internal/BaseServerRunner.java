package moco.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public abstract class BaseServerRunner {
    protected abstract ChannelInitializer<? extends Channel> channelInitializer();

    private final MocoServer server = new MocoServer();

    public void start() {
        int port = this.server.start(8080, channelInitializer());
        System.out.println("port :" + port);
    }

    public void stop() {
        server.stop();
    }
}
