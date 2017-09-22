package com.kxw.frame.server;


import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * <a href='http://blog.csdn.net/linsongbin1/article/details/77915686>@link</a>'
 */
public class SelfDefineEncodeHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bufferIn, List<Object> out) throws Exception {
        if (bufferIn.readableBytes() < 4) {
            //消息的长度,通常用4个字节保存
            //如果接收到的字节还不到4个字节,也即是连消息长度字段中的内容都不完整的,直接return。
            return;
        }

        //获取buffer的读取开始下标
        int beginIndex = bufferIn.readerIndex();
        //读取消息的长度之后,下标被移动
        int length = bufferIn.readInt();

        if (bufferIn.readableBytes() < length) {
            //消息不完整,重置下标为开始的下标
            bufferIn.readerIndex(beginIndex);
            return;
        }

        //设置到下一个包的开始下标
        bufferIn.readerIndex(beginIndex + 4 + length);
        //ByteToMessageDecoder类的底层会根据bufferIn.isReadable()方法来判断是否读取完毕。只有将readerIndex设置为最大,bufferIn.isReadable()方法才会返回false。

        //slice操作,目的是从大消息中截取出一条有效的业务消息。粘包情况
        ByteBuf otherByteBufRef = bufferIn.slice(beginIndex, 4 + length);

        otherByteBufRef.retain();

        out.add(otherByteBufRef);

        /**
         * 当decode方法执行完后,会释放bufferIn这个缓冲区,如果将执行完释放操作的bufferIn传递给下个处理器的话,一旦下个处理器调用bufferIn的读或者写的方法时,会立刻报出IllegalReferenceCountException异常的。

         因此slice操作后,必须加上一个retain操作,让bufferIn的引用计数器加1,这样ByteToMessageDecoder会刀下留人,先不释放bufferIn。
         */
    }
}
