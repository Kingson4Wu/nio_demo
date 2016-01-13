package com.kxw.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

/**
 * <a href='http://www.iteye.com/magazines/132-Java-NIO'>@link</a>
 */
public class FileReadTest {


    public static void main(String[] args) throws IOException {

        //文本行的流可以这样处理
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("email.txt"); // get the InputStream from the client socket
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String nameLine   = reader.readLine();
        String ageLine    = reader.readLine();
        String emailLine  = reader.readLine();
        String phoneLine  = reader.readLine();

        System.out.println(nameLine);
        System.out.println(ageLine);
        System.out.println(emailLine);
        System.out.println(phoneLine);

        /**
         * 请注意处理状态由程序执行多久决定。换句话说，一旦reader.readLine()方法返回，你就知道肯定文本行就已读完，
         * readline()阻塞直到整行读完，这就是原因。你也知道此行包含名称；同样，第二个readline()调用返回的时候，你知道这行包含年龄等。
         * 正如你可以看到，该处理程序仅在有新数据读入时运行，并知道每步的数据是什么。一旦正在运行的线程已处理过读入的某些数据，
         * 该线程不会再回退数据（大多如此）。
         */

        ByteBuffer buffer = ByteBuffer.allocate(48);
        RandomAccessFile fromFile = new RandomAccessFile("email.txt", "r");
        FileChannel inChannel = fromFile.getChannel();

        int bytesRead = inChannel.read(buffer);
       /* while(! bufferFull(bytesRead) ) {
            bytesRead = inChannel.read(buffer);
        }*/

        /**
         * bufferFull()方法必须跟踪有多少数据读入缓冲区，并返回真或假，这取决于缓冲区是否已满。换句话说，如果缓冲区准备好被处理，那么表示缓冲区满了。

         bufferFull()方法扫描缓冲区，但必须保持在bufferFull（）方法被调用之前状态相同。如果没有，下一个读入缓冲区的数据可能无法读到正确的位置。这是不可能的，但却是需要注意的又一问题。

         如果缓冲区已满，它可以被处理。如果它不满，并且在你的实际案例中有意义，你或许能处理其中的部分数据。但是许多情况下并非如此。
         */

    }
}
