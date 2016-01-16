package com.kxw.nio2.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

/**
 * <a href='http://zjumty.iteye.com/blog/1896350'>@link</a>
 */
public class FileTest {


    /**
     * 这个类在java.nio.file，在NIO里对文件系统进行了进一步的抽象。
     * 是用来替换原来的java.io.File。其中 FileSystems, Files, Path, PathMatcher 成为一个体系。
     * 在java7里File和Path可以相互转换： File.toPath(), Path.toFile()
     * 原来的File里包含了文件引用和文件，在Path系统里， Path里是文件的引用，而文件操作都放到了Files的静态方法里。
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        String prefix = Thread.currentThread().getContextClassLoader().getResource("").getPath();

        /** Path ----------------------------------------------------------------- **/
        //获得Path实例的方式：  File.toPath(), Paths.get(), FileSystem.getPath()

        /** 不用特意指定路径的separator */
        Path path = Paths.get("C:", "folder1", "subfolder", "aa.txt");
        v(path.toString());

        /** 重复利用基本路径 */
        //define the fixed path
        Path base = Paths.get("C:/rafaelnadal/tournaments/2009");
        //resolve BNP.txt file
        Path path_1 = base.resolve("BNP.txt");
        //output: C:\rafaelnadal\tournaments\2009\BNP.txt
        System.out.println(path_1.toString());
        //resolve AEGON.txt file
        Path path_2 = base.resolve("AEGON.txt");
        //output: C:\rafaelnadal\tournaments\2009\AEGON.txt
        System.out.println(path_2.toString());

        /** 而resolveSibling方法更加好用：可以直接获取相同目录下的其他文件 */

        //define the fixed path
        base = Paths.get("C:/rafaelnadal/tournaments/2009/BNP.txt");
        //resolve sibling AEGON.txt file
        Path path_3 = base.resolveSibling("AEGON.txt");
        //output: C:\rafaelnadal\tournaments\2009\AEGON.txt
        System.out.println(path_3.toString());

        /** 取得相对路径 */
        // 假设BNP.txt和AEGON.txt在同一目录下
        Path path01 = Paths.get("BNP.txt");
        Path path02 = Paths.get("AEGON.txt");

        //output: ..\AEGON.txt
        Path path01_to_path02 = path01.relativize(path02);
        System.out.println(path01_to_path02);
        //output: ..\BNP.txt
        Path path02_to_path01 = path02.relativize(path01);
        System.out.println(path02_to_path01);

        //假设
        path01 = Paths.get("/tournaments/2009/BNP.txt");
        path02 = Paths.get("/tournaments/2011");

        //output: ..\..\2011
        path01_to_path02 = path01.relativize(path02);
        System.out.println(path01_to_path02);
        //output: ..\2009\BNP.txt
        path02_to_path01 = path02.relativize(path01);
        System.out.println(path02_to_path01);

        /** 遍历 */
        //Path实现了Iterable<Path>接口
        path = Paths.get("C:", "rafaelnadal/tournaments/2009", "BNP.txt");
        for (Path name : path) {
            System.out.println(name);
        }

        /** File Attributes -------------------------------------------- */
        /**
         * java.nio.file.attribute包下的类提供了获取文件属性的类，针对不同操作系统使用的类不太一样，当然也有所有操作系统通用的属性。

         属性分类有一些几种：
         BasicFileAttributeView
         DosFileAttributeView
         PosixFileAttributeView
         FileOwnerAttributeView
         AclFileAttributeView
         UserDefinedFileAttributeView
         */


        BasicFileAttributes attr = null;
        path = Paths.get(prefix, "story.txt");
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("File size: " + attr.size());
        System.out.println("File creation time: " + attr.creationTime());
        System.out.println("File was last accessed at: " + attr.lastAccessTime());
        System.out.println("File was last modified at: " + attr.lastModifiedTime());
        System.out.println("Is directory? " + attr.isDirectory());
        System.out.println("Is regular file? " + attr.isRegularFile());
        System.out.println("Is symbolic link? " + attr.isSymbolicLink());
        System.out.println("Is other? " + attr.isOther());

        //long size = (Long)Files.getAttribute(path, "basic:size", NOFOLLOW_LINKS);

        /** 设置属性 */
        path = Paths.get(prefix, "BNP.txt");
        long time = System.currentTimeMillis();
        FileTime fileTime = FileTime.fromMillis(time);
        try {
            Files.getFileAttributeView(path,
                    BasicFileAttributeView.class).setTimes(fileTime, fileTime, fileTime);
        } catch (IOException e) {
            System.err.println(e);
        }

        //或
        try {
            Files.setLastModifiedTime(path, fileTime);
        } catch (IOException e) {
            System.err.println(e);
        }

        /** Symbolic和Hard Links --------------------------------------------------------------- */
        //相同于用Java程序实现linux下的 ln命令。

        Path link = FileSystems.getDefault().getPath("rafael.nadal.1");
        Path target = FileSystems.getDefault().getPath("C:/rafaelnadal/photos", "rafa_winner.jpg");
        Files.createSymbolicLink(link, target);

        /** DirectoryStream -------------------------------------------------------------------- */
        //这也是一个比较有用的类：用来遍历路径下的子路径或文件，而且支持通配符过滤。
        path = Paths.get(prefix, "story.txt");
        //glob pattern applied
        System.out.println("\nGlob pattern applied:");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, "*.{png,jpg,bmp}")) {
            for (Path file : ds) {
                System.out.println(file.getFileName());
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        /** FileVisitor和Files.walkFileTree ---------------------------------------------------- */
        /**
         * FileVisitor是一个接口，Files.walkFileTree是一个方法。
         * 通过两者的配合可以遍历整个某个路径下的所有子路径和文件。没有这个之前我们用递归方法也能实现，
         * 有了这个不能说是现实更加容易， 只能说是现实更加规范， 如果大家都用这个，代码的可维护性会更好。我觉得仅此而已。
         */

        //FileVisitor有四个方法
        /**
         * FileVisitResult postVisitDirectory(T dir, IOException exc)
         * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
         * FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
         * Invoked for a directory before entries in the directory are visited.
         * FileVisitResult visitFile(T file, BasicFileAttributes attrs)
         * Invoked for a file in a directory.
         * FileVisitResult visitFileFailed(T file, IOException exc)
         * Invoked for a file that could not be visited.
         * */

        /** FileVisitResult枚举类型：
         * CONTINUE，SKIP_SIBLINGS，SKIP_SUBTREE，TERMINATE
         */

        /** Watch Service API  ----------------------------------------- */
        /**
         * 这是NIO2里比较重要的一个新增功能， 以前直接用java监视文件系统的变化是不可能的，
         * 只能通过jni的方式调用操作系统的api来对文件系统进行监视。在java7里这部分被加到了标准库里，
         * 这样我们就不能在去寻找jni的结果方案了。但是事实上为了保持java的扩平台特性，监控的功能范围被定为各个操作系统的交集，
         * 所以没有特殊的情况还是需要直接调用操作系统的api来实现。
         */

        //watchRNDir(path);

        /** Random Access Files ---------------------------------------- */
        //主要是提供了一个SeekableByteChannel接口，配合ByteBuffer使随机访问文件更加方便。

        path = Paths.get("C:/rafaelnadal/tournaments/2009", "MovistarOpen.txt");
        ByteBuffer buffer = ByteBuffer.allocate(1);
        String encoding = System.getProperty("file.encoding");
        try (SeekableByteChannel seekableByteChannel = (Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ)))) {
            //the initial position should be 0 anyway
            seekableByteChannel.position(0);
            System.out.println("Reading one character from position: " + seekableByteChannel.position());
            seekableByteChannel.read(buffer);
            buffer.flip();
            System.out.print(Charset.forName(encoding).decode(buffer));
            buffer.rewind();
            //get into the middle
            seekableByteChannel.position(seekableByteChannel.size() / 2);
            System.out.println("\nReading one character from position: " + seekableByteChannel.position());
            seekableByteChannel.read(buffer);
            buffer.flip();
            System.out.print(Charset.forName(encoding).decode(buffer));
            buffer.rewind();
            //get to the end
            seekableByteChannel.position(seekableByteChannel.size() - 1);
            System.out.println("\nReading one character from position: " + seekableByteChannel.position());
            seekableByteChannel.read(buffer);
            buffer.flip();
            System.out.print(Charset.forName(encoding).decode(buffer));
            buffer.clear();
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    private static void v(String str) {
        System.out.println(str);
    }

    public static void watchRNDir(Path path) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            //start an infinite loop
            while (true) {
                //retrieve and remove the next watch key
                final WatchKey key = watchService.take();
                //get list of pending events for the watch key
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    //get the kind of event (create, modify, delete)
                    final WatchEvent.Kind<?> kind = watchEvent.kind();
                    //handle OVERFLOW event
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    //get the filename for the event
                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    //print it out
                    System.out.println(kind + " -> " + filename);
                }
                //reset the key
                boolean valid = key.reset();
                //exit loop if the key is not valid (if the directory was deleted, for example)
                if (!valid) {
                    break;
                }
            }
        }
    }
}
