<http://www.ibm.com/developerworks/cn/java/j-lo-io-optimize/index.html>
本文首先对 I/O 与 NIO 进行了对比，然后通过若干实例介绍了 I/O 的操作方式，包括传统 I/O、基于缓存的 I/O 等，最后又介绍了 JDK7 推出的 AIO。通过这篇文章，让读者对 Java I/O 处理方式有一个大概的了解，后续会对 I/O 方式的源代码进行解释

Java I/O
I/O，即 Input/Output(输入/输出) 的简称。就 I/O 而言，概念上有 5 种模型：blocking I/O，nonblocking I/O，I/O multiplexing (select and poll)，signal driven I/O (SIGIO)，asynchronous I/O (the POSIX aio_functions)。不同的操作系统对上述模型支持不同，UNIX 支持 IO 多路复用。不同系统叫法不同，freebsd 里面叫 kqueue，Linux 叫 epoll。而 Windows2000 的时候就诞生了 IOCP 用以支持 asynchronous I/O。
Java 是一种跨平台语言，为了支持异步 I/O，诞生了 NIO，Java1.4 引入的 NIO1.0 是基于 I/O 复用的，它在各个平台上会选择不同的复用方式。Linux 用的 epoll，BSD 上用 kqueue，Windows 上是重叠 I/O。
Java I/O 的相关方法如下所述：
同步并阻塞 (I/O 方法)：服务器实现模式为一个连接启动一个线程，每个线程亲自处理 I/O 并且一直等待 I/O 直到完成，即客户端有连接请求时服务器端就需要启动一个线程进行处理。但是如果这个连接不做任何事情就会造成不必要的线程开销，当然可以通过线程池机制改善这个缺点。I/O 的局限是它是面向流的、阻塞式的、串行的一个过程。对每一个客户端的 Socket 连接 I/O 都需要一个线程来处理，而且在此期间，这个线程一直被占用，直到 Socket 关闭。在这期间，TCP 的连接、数据的读取、数据的返回都是被阻塞的。也就是说这期间大量浪费了 CPU 的时间片和线程占用的内存资源。此外，每建立一个 Socket 连接时，同时创建一个新线程对该 Socket 进行单独通信 (采用阻塞的方式通信)。这种方式具有很快的响应速度，并且控制起来也很简单。在连接数较少的时候非常有效，但是如果对每一个连接都产生一个线程无疑是对系统资源的一种浪费，如果连接数较多将会出现资源不足的情况；
同步非阻塞 (NIO 方法)：服务器实现模式为一个请求启动一个线程，每个线程亲自处理 I/O，但是另外的线程轮询检查是否 I/O 准备完毕，不必等待 I/O 完成，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有 I/O 请求时才启动一个线程进行处理。NIO 则是面向缓冲区，非阻塞式的，基于选择器的，用一个线程来轮询监控多个数据传输通道，哪个通道准备好了 (即有一组可以处理的数据) 就处理哪个通道。服务器端保存一个 Socket 连接列表，然后对这个列表进行轮询，如果发现某个 Socket 端口上有数据可读时，则调用该 Socket 连接的相应读操作；如果发现某个 Socket 端口上有数据可写时，则调用该 Socket 连接的相应写操作；如果某个端口的 Socket 连接已经中断，则调用相应的析构方法关闭该端口。这样能充分利用服务器资源，效率得到大幅度提高；
异步非阻塞 (AIO 方法，JDK7 发布)：服务器实现模式为一个有效请求启动一个线程，客户端的 I/O 请求都是由操作系统先完成了再通知服务器应用去启动线程进行处理，每个线程不必亲自处理 I/O，而是委派操作系统来处理，并且也不需要等待 I/O 完成，如果完成了操作系统会另行通知的。该模式采用了 Linux 的 epoll 模型。
在连接数不多的情况下，传统 I/O 模式编写较为容易，使用上也较为简单。但是随着连接数的不断增多，传统 I/O 处理每个连接都需要消耗一个线程，而程序的效率，当线程数不多时是随着线程数的增加而增加，但是到一定的数量之后，是随着线程数的增加而减少的。所以传统阻塞式 I/O 的瓶颈在于不能处理过多的连接。非阻塞式 I/O 出现的目的就是为了解决这个瓶颈。非阻塞 IO 处理连接的线程数和连接数没有联系，例如系统处理 10000 个连接，非阻塞 I/O 不需要启动 10000 个线程，你可以用 1000 个，也可以用 2000 个线程来处理。因为非阻塞 IO 处理连接是异步的，当某个连接发送请求到服务器，服务器把这个连接请求当作一个请求“事件”，并把这个“事件”分配给相应的函数处理。我们可以把这个处理函数放到线程中去执行，执行完就把线程归还，这样一个线程就可以异步的处理多个事件。而阻塞式 I/O 的线程的大部分时间都被浪费在等待请求上了。

Java NIO

I/O	   NIO
面向流  面向缓冲
阻塞IO  非阻塞IO
无      选择器



AIO 相关的类和接口：
java.nio.channels.AsynchronousChannel：标记一个 Channel 支持异步 IO 操作；
java.nio.channels.AsynchronousServerSocketChannel：ServerSocket 的 AIO 版本，创建 TCP 服务端，绑定地址，监听端口等；
java.nio.channels.AsynchronousSocketChannel：面向流的异步 Socket Channel，表示一个连接；
java.nio.channels.AsynchronousChannelGroup：异步 Channel 的分组管理，目的是为了资源共享。一个 AsynchronousChannelGroup 绑定一个线程池，这个线程池执行两个任务：处理 IO 事件和派发 CompletionHandler。AsynchronousServerSocketChannel 创建的时候可以传入一个 AsynchronousChannelGroup，那么通过 AsynchronousServerSocketChannel 创建的 AsynchronousSocketChannel 将同属于一个组，共享资源；
java.nio.channels.CompletionHandler：异步 IO 操作结果的回调接口，用于定义在 IO 操作完成后所作的回调工作。AIO 的 API 允许两种方式来处理异步操作的结果：返回的 Future 模式或者注册 CompletionHandler，推荐用 CompletionHandler 的方式，这些 handler 的调用是由 AsynchronousChannelGroup 的线程池派发的。这里线程池的大小是性能的关键因素。


---

结束语
I/O 与 NIO 一个比较重要的区别是我们使用 I/O 的时候往往会引入多线程，每个连接使用一个单独的线程，而 NIO 则是使用单线程或者只使用少量的多线程，每个连接共用一个线程。而由于 NIO 的非阻塞需要一直轮询，比较消耗系统资源，所以异步非阻塞模式 AIO 就诞生了。本文对 I/O、NIO、AIO 等三种输入输出操作方式进行一一介绍，力求通过简单的描述和实例让读者能够掌握基本的操作、优化方法。