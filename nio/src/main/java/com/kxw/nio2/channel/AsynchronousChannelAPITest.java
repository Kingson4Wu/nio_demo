package com.kxw.nio2.channel;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <a href='http://zjumty.iteye.com/'>@link</a>
 * 这个是NIO2的较大的变化，有原来的Selecor方法变成方法回调模式。使用上更加方便。并且文件的读写也可以异步的方式实现了。
 */
public class AsynchronousChannelAPITest {
    public static void main(String[] args) {

        String prefix = Thread.currentThread().getContextClassLoader().getResource("").getPath();

        //异步读取文件
        ByteBuffer buffer = ByteBuffer.allocate(100);

        Path path = Paths.get(prefix, "story.txt");
        try (AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            final Thread current = Thread.currentThread();
            asynchronousFileChannel.read(buffer, 0, "Read operation status ...", new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    System.out.println(attachment);
                    System.out.print("Read bytes: " + result);
                    current.interrupt();
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.println(attachment);
                    System.out.println("Error:" + exc);
                    current.interrupt();
                }
            });
            System.out.println("\nWaiting for reading operation to end ...\n");
            try {
                current.join();
                //等待该线程终止,即等待main(本线程)终止,则会导致无限等待,所以在另一个异步线程使用interrupt终止,
                // 这么做的目的其实就是为了等待另一个线程有读取文件完成,可用下面线程休眠的方式代替
            } catch (InterruptedException e) {
               //System.out.println(e);
            }
            //Thread.sleep(1000L);

            //now the buffer contains the read bytes
            System.out.println("\n\nClose everything and leave! Bye, bye ...");
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }
}
/** Thread.currentThread.interrupt() 只对阻塞线程起作用，
 当线程阻塞时，调用interrupt方法后，该线程会得到一个interrupt异常，可以通过对该异常的处理而退出线程
 对于正在运行的线程，没有任何作用
 */