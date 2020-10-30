package redis;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class ArrayOutput implements IRedisOutput {

    private List<IRedisOutput> outputs = new ArrayList<>();

    public static ArrayOutput newArray() {
        return new ArrayOutput();
    }

    public ArrayOutput append(IRedisOutput output) {
        outputs.add(output);
        return this;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte('*');
        buf.writeBytes(String.valueOf(outputs.size()).getBytes(Charsets.UTF_8));
        buf.writeByte('\r');
        buf.writeByte('\n');
        for (IRedisOutput output : outputs) {
            output.encode(buf);
        }
    }

}
