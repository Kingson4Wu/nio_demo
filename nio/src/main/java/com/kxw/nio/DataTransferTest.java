package com.kxw.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Created by kingsonwu on 16/1/14.
 */
public class DataTransferTest {


    //通道之间的数据传输
    public static void main(String[] args) throws IOException {


        //FileChannel的transferFrom()方法可以将数据从源通道传输到FileChannel中（译者注：这个方法在JDK文档中的解释为将字节从给定的可读取字节通道传输到此通道的文件中）

        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println(path);
        RandomAccessFile fromFile = new RandomAccessFile(path + "fromFile.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile(path + "toFile.txt", "rw");
        FileChannel toChannel = toFile.getChannel();

        long position = 0;
        long count = fromChannel.size();

        toChannel.transferFrom(fromChannel, position, count);

        /**
         * 方法的输入参数position表示从position处开始向目标文件写入数据，count表示最多传输的字节数。
         * 如果源通道的剩余空间小于 count 个字节，则所传输的字节数要小于请求的字节数。
         * 此外要注意，在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据（可能不足count字节）。
         * 因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。
         */

        //transferTo()
        //transferTo()方法将数据从FileChannel传输到其他的channel中。


      /*  RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
        FileChannel      fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
        FileChannel      toChannel = toFile.getChannel();

        long position = 0;
        long count = fromChannel.size();

        fromChannel.transferTo(position, count, toChannel);*/
        //是不是发现这个例子和前面那个例子特别相似？除了调用方法的FileChannel对象不一样外，其他的都一样。
        // 上面所说的关于SocketChannel的问题在transferTo()方法中同样存在。SocketChannel会一直传输数据直到目标buffer被填满。
    }
}
