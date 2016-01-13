package com.kxw.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by kingsonwu on 16/1/14.
 */
public class PipeTest {
    //Java NIO 管道是2个线程之间的单向数据连接。Pipe有一个source通道和一个sink通道。数据会被写到sink通道，从source通道读取。
    public static void main(String[] args) throws IOException {
        Pipe pipe = Pipe.open();
        //要向管道写数据，需要访问sink通道
        Pipe.SinkChannel sinkChannel = pipe.sink();

        //通过调用SinkChannel的write()方法，将数据写入SinkChannel,
        String newData = "New String to write to file..." + System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while(buf.hasRemaining()) {
            sinkChannel.write(buf);
        }
        //从管道读取数据
        //从读取管道的数据，需要访问source通道
        //String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer buf2 = ByteBuffer.allocate(48);
        //RandomAccessFile file = new RandomAccessFile(path + "pipe.txt", "rw");
        //FileChannel inChannel = file.getChannel();
        int bytesRead = sourceChannel.read(buf2);

        System.out.println(bytesRead);
       // System.out.println(getString(buf2));

        while (bytesRead != -1) {

            System.out.println("Read " + bytesRead);
            buf2.flip();

            while (buf2.hasRemaining()) {
                System.out.print((char) buf2.get());
            }
            buf2.clear();
            bytesRead = sourceChannel.read(buf2);
        }
    }

    /**
     * ByteBuffer 转换 String
     * @param buffer
     * @return
     * <a href='http://www.xuebuyuan.com/593860.html'>@link</a>
     */
    public static String getString(ByteBuffer buffer)
    {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try
        {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }
}
