package com.kxw.nio2.file;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * 异步文件
 * <a href='http://www.tuicool.com/articles/rYniQ3'>@link</a>
 */
public class AIOFileChannal {
    public static void main(String[] args) {

        String prefix = Thread.currentThread().getContextClassLoader().getResource("").getPath();

        ByteBuffer buffer = ByteBuffer.allocate(100);
        String encoding = System.getProperty("file.encoding");
        Path path = Paths.get(prefix, "story.txt");
        try (AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel
                .open(path, StandardOpenOption.READ)) {
            Future<Integer> result = asynchronousFileChannel.read(buffer, 0);
            //Thread.sleep(100);
            while (!result.isDone()) {
                System.out.println("Do something else while reading ...");
            }
            System.out.println("Read done: " + result.isDone());
            System.out.println("Bytes read: " + result.get());
        } catch (Exception ex) {
            System.err.println(ex);
        }
        buffer.flip();
        System.out.print(Charset.forName(encoding).decode(buffer));
        buffer.clear();
    }
}