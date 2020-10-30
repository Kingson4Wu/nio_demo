package redis;

import java.util.List;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

@Sharable
public class RedisOutputEncoder extends MessageToMessageEncoder<IRedisOutput> {

    @Override
    protected void encode(ChannelHandlerContext ctx, IRedisOutput msg, List<Object> out) throws Exception {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
        msg.encode(buf);
        out.add(buf);
    }

}
