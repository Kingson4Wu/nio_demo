package com.kxw.nio;

import java.nio.ByteBuffer;

/**
 * <a href = 'http://www.mytju.com/classcode/news_readNews.asp?newsID=300'>@link</a>
 */
public class ByteBufferTest {

    public static void main(String[] args) {

        //10个字节大小
        ByteBuffer buffer = ByteBuffer.allocate(10);

        //容量是10，EOF位置是10，初始位置也是0
        v(buffer.capacity());
        v(buffer.limit());

        //输出看看，输出是10个0
        printBuffer(buffer);

        //此时，指针指向位置10，已经是最大容量了。
        //把指针挪回位置1
        buffer.rewind();

        //写操作，指针会自动移动
        buffer.putChar('a');
        v(buffer.position()); //指针指向2
        buffer.putChar('啊');
        v(buffer.position()); //指针指向4

        //当前位置设置为EOF，指针挪回位置1
        //相当于下面两句：
        //buffer.limit(4);
        //buffer.position(0);
        buffer.flip();

        //输出前4个字节看看，输出是0 61 55 4a
        printBuffer(buffer);

        //指针挪到位置1，压缩一下
        //输出是61 55 4a 4a 0 0 0 0 0 0
        //compact方法会把EOF位置重置为最大容量，这里就是10
        buffer.position(1);
        buffer.compact();
        printBuffer(buffer);

        //注意当前指针指向3，继续写入数据的话，就会覆盖后面的数据了。
        v(buffer.position());

    }

    /**
     * 输出buffer内容.
     */
    public static void printBuffer(ByteBuffer buffer) {

        //记住当前位置
        int p = buffer.position();

        //指针挪到0
        buffer.position(0);

        //循环输出每个字节内容
        for (int i = 0; i < buffer.limit(); i++) {
            byte b = buffer.get(); //读操作，指针会自动移动
            v(Integer.toHexString(b));
        }

        //指针再挪回去
        buffer.position(p);

        //本想用mark()和reset()来实现。
        //但是，它们貌似只能正向使用。
        //如，位置6的时候，做一下Mark，
        //然后在位置10（位置要大于6）的时候，用reset就会跳回位置6.

        //而position(n)这个方法，如果之前做了Mark，但是Mark位置大于新位置，Mark会被清除。
        //也就是说，做了Mark后，只能向前跳，不能往回跳，否则Mark就丢失。
        //rewind()方法，更干脆，直接清除mark。
        //flip()方法，也清除mark
        //clear()方法，也清除mark
        //compact方法，也清除mark

        //所以，mark方法干脆不要用了，自己拿变量记一下就完了。
    }

    public static void v(Object o) {
        System.out.println(o);
    }

}

/**
 * java.nio.ByteBuffer
 -------------------------------
 Capacity 缓冲区最大容量
 Limit 当前最大使用量，或者说是有效数据的EOF位置。
 Position 指向当前位置的指针
 -----------------------------------
 假设一个缓冲区容量是10，开始指针指向0，即position=0。
 然后写入6个字节数据，写完后，下标0、1、2、3、4、5有数据，
 指针指向6，即当前position=6。
 此时，用limit(6)方法将当前位置设为EOF位置。
 那么，读数据的时候，读到EOF位置就结束了。
 下标超过的话，会报错java.nio.BufferUnderflowException
 -------------------------------------
 clear()，只是把指针移到位置0，并没有真正清空数据。
 flip()，当前位置设置为EOF，指针指向0.
 rewind，指针指向0.
 compact()，压缩数据。比如当前EOF是6，当前指针指向2
 （即0,1的数据已经写出了，没用了），
 那么compact方法将把2,3,4,5的数据挪到0,1,2,3的位置，
 然后指针指向4的位置。这样的意思是，从4的位置接着再写入数据。
 写完后，把指针挪到0，再写出，然后再compact()，如此反复……
 --------------------------------
 buf.clear();          // 清空一下，准备
 for (;;) {
 if (in.read(buf) < 0 && !buf.hasRemaining())
 break;        // 没有读入数据了，并且buffer里没有剩余数据了
 buf.flip(); //当前位置设置为EOF，指针挪到0
 out.write(buf); //写出数据，即读取buffer的数据
 buf.compact();    // write方法可能只写出了部分数据，buffer里还有剩余。
 //压缩一下，把后一段的数据挪到前面。指针也挪到有效数据的后一位。
 }
 --------------------------
 */

/**
 * java.nio.ByteBuffer中flip、rewind、clear方法的区别
 * <a href ='http://www.blogjava.net/sdjxsgb/archive/2013/06/18/400703.html'>@link</>
 *
 * 对缓冲区的读写操作首先要知道缓冲区的下限、上限和当前位置。下面这些变量的值对Buffer类中的某些操作有着至关重要的作用：
 limit：所有对Buffer读写操作都会以limit变量的值作为上限。
 position：代表对缓冲区进行读写时，当前游标的位置。
 capacity：代表缓冲区的最大容量（一般新建一个缓冲区的时候，limit的值和capacity的值默认是相等的）。
 flip、rewind、clear这三个方法便是用来设置这些值的。
 clear方法

 <code>
 public final Buffer clear()
 {
 position = 0; //重置当前读写位置
 limit = capacity;
 mark = -1;  //取消标记
 return this;
 }
 </code>

 clear方法将缓冲区清空，一般是在重新写缓冲区时调用。
 flip方法
 <code>
 public final Buffer flip() {
 limit = position;
 position = 0;
 mark = -1;
 return this;
 }
 </code>
 反转缓冲区。首先将限制设置为当前位置，然后将位置设置为 0。如果已定义了标记，则丢弃该标记。 常与compact方法一起使用。通常情况下，在准备从缓冲区中读取数据时调用flip方法。
 rewind方法

 <code>
 public final Buffer rewind() {
  position = 0;
  mark = -1;
  return this;
 }
 </code>

 以上三种方法均使用final修饰，java.nio.Buffer的所有子类均使用同一种flip、clear和rewind机制。
 */