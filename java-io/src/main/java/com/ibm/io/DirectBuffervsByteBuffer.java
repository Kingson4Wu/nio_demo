package com.ibm.io;

import java.nio.ByteBuffer;

/**
 * <a href='http://www.ibm.com/developerworks/cn/java/j-lo-io-optimize/index.html'>@link</a>
 * NIO 的 Buffer 还提供了一个可以直接访问系统物理内存的类 DirectBuffer。
 * DirectBuffer 继承自 ByteBuffer，但和普通的 ByteBuffer 不同。
 * 普通的 ByteBuffer 仍然在 JVM 堆上分配空间，其最大内存受到最大堆的限制，而 DirectBuffer 直接分配在物理内存上，
 * 并不占用堆空间。在对普通的 ByteBuffer 访问时，系统总是会使用一个“内核缓冲区”进行间接的操作。
 * 而 DirectBuffer 所处的位置，相当于这个“内核缓冲区”。因此，使用 DirectBuffer 是一种更加接近系统底层的方法，
 * 所以，它的速度比普通的 ByteBuffer 更快。DirectBuffer 相对于 ByteBuffer 而言，读写访问速度快很多，
 * 但是创建和销毁 DirectBuffer 的花费却比 ByteBuffer 高。
 */
public class DirectBuffervsByteBuffer {
    public void DirectBufferPerform() {
        long start = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.allocateDirect(500);//分配 DirectBuffer
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 99; j++) {
                bb.putInt(j);
            }
            bb.flip();
            for (int j = 0; j < 99; j++) {
                bb.getInt(j);
            }
        }
        bb.clear();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        start = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            ByteBuffer b = ByteBuffer.allocateDirect(10000);//创建 DirectBuffer
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public void ByteBufferPerform() {
        long start = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.allocate(500);//分配 ByteBuffer
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 99; j++) {
                bb.putInt(j);
            }
            bb.flip();
            for (int j = 0; j < 99; j++) {
                bb.getInt(j);
            }
        }
        bb.clear();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        start = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            ByteBuffer b = ByteBuffer.allocate(10000);//创建 ByteBuffer
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public static void main(String[] args) {
        DirectBuffervsByteBuffer db = new DirectBuffervsByteBuffer();
        db.ByteBufferPerform();
        db.DirectBufferPerform();
    }
}

/**
 频繁创建和销毁 DirectBuffer 的代价远远大于在堆上分配内存空间。
 使用参数-XX:MaxDirectMemorySize=200M –Xmx200M 在 VM Arguments 里面配置最大 DirectBuffer 和最大堆空间，
 代码中分别请求了 200M 的空间，如果设置的堆空间过小，例如设置 1M，会抛出错误:
 Error occurred during initialization of VM
 Too small initial heap for new size specified
 */

/**
 * DirectBuffer 的信息不会打印在 GC 里面，因为 GC 只记录了堆空间的内存回收。
 * 可以看到，由于 ByteBuffer 在堆上分配空间，因此其 GC 数组相对非常频繁，在需要频繁创建 Buffer 的场合，
 * 由于创建和销毁 DirectBuffer 的代码比较高昂，不宜使用 DirectBuffer。但是如果能将 DirectBuffer 进行复用，
 * 可以大幅改善系统性能。
 */