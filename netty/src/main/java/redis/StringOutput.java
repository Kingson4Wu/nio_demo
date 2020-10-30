package redis;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class StringOutput implements IRedisOutput {

    private String content;

    public StringOutput(String content) {
        this.content = content;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte('$');
        if (content == null) {
            // $-1\r\n
            buf.writeByte('-');
            buf.writeByte('1');
            buf.writeByte('\r');
            buf.writeByte('\n');
            return;
        }
        byte[] bytes = content.getBytes(Charsets.UTF_8);
        buf.writeBytes(String.valueOf(bytes.length).getBytes(Charsets.UTF_8));
        buf.writeByte('\r');
        buf.writeByte('\n');
        if (content.length() > 0) {
            buf.writeBytes(bytes);
        }
        buf.writeByte('\r');
        buf.writeByte('\n');
    }

    public static StringOutput of(String content) {
        return new StringOutput(content);
    }

    public static StringOutput of(long value) {
        return new StringOutput(String.valueOf(value));
    }

    public final static StringOutput NULL = new StringOutput(null);

}
