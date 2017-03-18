package com.kxw.watch;

import java.nio.file.*;
import java.util.List;

/**
 * <a href='http://www.importnew.com/2000.html'>@link</a>
 */
public class Watcher {
    public static void main(String[] args) {
        Path this_dir = Paths.get(".");
        System.out.println("Now watching the current directory ...");
        System.out.println(this_dir.toAbsolutePath());

        try {
            WatchService watcher = this_dir.getFileSystem().newWatchService();
            this_dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey watckKey = watcher.take();

            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                System.out.println("Someone just created the file '" + event.context().toString() + "'.");

            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    /**
     * 在相同的目录下，创建一个新的文件，例如运行touch example或者copy Watcher.class example命令。你会看到下面的变更通知消息：

     Someone just create the fiel ‘example1′.


     这个简单的示例展示了怎么开始使用Java NIO的功能。同时，它也介绍了NIO.2的Watcher类，它相比较原始的I/O中的轮询方案而言，显得更加直接和易用。


     */
}