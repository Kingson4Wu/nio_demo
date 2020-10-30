package redis;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class ErrorOutput implements IRedisOutput {

    private String type;
    private String reason;

    public ErrorOutput(String type, String reason) {
        this.type = type;
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte('-');
        // reason不允许多行字符串
        buf.writeBytes(String.format("%s %s", type, headOf(reason)).getBytes(Charsets.UTF_8));
        buf.writeByte('\r');
        buf.writeByte('\n');
    }

    private String headOf(String reason) {
        int idx = reason.indexOf("\n");
        if (idx < 0) {
            return reason;
        }
        return reason.substring(0, idx).trim();
    }

    // 通用错误
    public static ErrorOutput errorOf(String reason) {
        return new ErrorOutput("ERR", reason);
    }

    // 语法错误
    public static ErrorOutput syntaxOf(String reason) {
        return new ErrorOutput("SYNTAX", reason);
    }

    // 协议错误
    public static ErrorOutput protoOf(String reason) {
        return new ErrorOutput("PROTO", reason);
    }

    // 参数无效
    public static ErrorOutput paramOf(String reason) {
        return new ErrorOutput("PARAM", reason);
    }

    // 服务器内部错误
    public static ErrorOutput serverOf(String reason) {
        return new ErrorOutput("SERVER", reason);
    }

}
