package com.kxw.nio2.channel.thread;

/**
 * <a href='http://www.open-open.com/lib/view/open1371741636171.html'>@link</a>
 */
public class ThreadJoinTest {

    /**
     * 一、使用方式。
     join是Thread类的一个方法，启动线程后直接调用，例如：
     Thread t = new AThread(); t.start(); t.join();
     二、为什么要用join()方法
     在很多情况下，主线程生成并起动了子线程，如果子线程里要进行大量的耗时的运算，主线程往往将于子线程之前结束，但是如果主线程处理完其他的事务后，需要用到子线程的处理结果，也就是主线程需要等待子线程执行完成之后再结束，这个时候就要用到join()方法了。
     三、join方法的作用
     在JDk的API里对于join()方法是：
     join
     public final void join() throws InterruptedException Waits for this thread to die.
     Throws: InterruptedException  - if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
     即join()的作用是：“等待该线程终止”，这里需要理解的就是该线程是指的主线程等待子线程的终止。也就是在子线程调用了join()方法后面的代码，只有等到子线程结束了才能执行。
     */






}
