package redis;

import io.netty.buffer.ByteBuf;

public interface IRedisOutput {

    public void encode(ByteBuf buf);

}