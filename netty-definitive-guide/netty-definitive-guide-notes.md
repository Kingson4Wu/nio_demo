——内容基本来自Netty权威指南一书
## 概念
+ file descriptor,socketfd，描述符就是一个数字，它指向内核中的一个结构体（文件路径，数据区等一些属性）
+ IO多路复用，Java NIO的核心类库多路复用器Selector就是基于epoll的多路复用技术实现。
+ IO复用的系统调用方式：select，pselect，poll，epoll（IO复用属于同步IO）
+ Java I/O 操作及优化建议:<http://www.ibm.com/developerworks/cn/java/j-lo-io-optimize/index.html>

## epoll
+ select单进程所打开的FD有限制，由FD_SETSIZE设置，默认1024，epoll没有限制，它所支持的FD上限是操作系统的最大句柄数。1 GB内存大约是10万个左右。`cat /proc/sys/fs/file-max`
+ IO效率不会随FD数目得增加而线性下降。
+ 使用mmap加速内核与用户空间得消息传递。
+ <http://www.cnblogs.com/venow/archive/2012/11/30/2790031.html>
select，poll实现需要自己不断轮询所有fd集合，直到设备就绪，期间可能要睡眠和唤醒多次交替。而epoll其实也需要调用epoll_wait不断轮询就绪链表，期间也可能多次睡眠和唤醒交替，但是它是设备就绪时，调用回调函数，把就绪fd放入就绪链表中，并唤醒在epoll_wait中进入睡眠的进程。虽然都要睡眠和交替，但是select和poll在“醒着”的时候要遍历整个fd集合，而epoll在“醒着”的时候只要判断一下就绪链表是否为空就行了，这节省了大量的CPU时间。这就是回调机制带来的性能提升。

---
select，poll每次调用都要把fd集合从用户态往内核态拷贝一次，并且要把current往设备等待队列中挂一次，而epoll只要一次拷贝，而且把current往等待队列上挂也只挂一次（在epoll_wait的开始，注意这里的等待队列并不是设备等待队列，只是一个epoll内部定义的等待队列）。这也能节省不少的开销。
<http://www.voidcn.com/blog/chen8238065/article/p-4511757.html>

## Java IO
 同步阻塞BIO，同步非阻塞NIO，异步非阻塞AIO
 同步IO和异步IO的区别就在于：数据拷贝的时候进程是否阻塞！
阻塞IO和非阻塞IO的区别就在于：应用程序的调用是否立即返回！

### BIO
 +  socket通信采用同步阻塞模式，这种一请求一应答的通信模型简化了上层的应用开发，但在性能和可靠性方面存在巨大的瓶颈。当并发访问量增大，响应延迟增大之后，采用Java BIO开发的服务只有通过硬件不断扩容来满足高并发和低时延。
 + 传统Client/Server模型，ServerSocket负责绑定IP地址，启动监听端口，Socket负责发起连接操作。连接成功之后，双方通过输入和输出流进行同步阻塞式通信。
 + 服务端线程个数和客户端并发访问数呈1：1的正比关系。线程是Java虚拟机宝贵的系统资源，访问量继续增大，系统会发生线程堆栈溢出，创建新线程失败等，最终导致进程宕机或者僵死。
#### BIO API
+ 输入流InputStream
`public int read(byte b[]) throws IOException`
This method blocks until input data is available ,end of file is detected ,or an exception is thrown.
1.  有数据可读
2.  可用数据已经读取完毕
3.  发生空指针或IO异常
这意味着当对方发送请求或者应答消息比较缓慢,或者网络传输较慢时,读取输入流的一方也会被长时间阻塞.阻塞期间,其他介入消息只能在队列中排队.
+ 输出流OutputStream
`public void write(byte b[]) throws IOException`
write 方法输出流时也会被阻塞,直到所有要发送的字节全部写入完毕,或者发生异常
TCP/IP 中,当消息接受方处理缓慢时,将会导致发送方TCP windows size不断减少,直至为0,双方处于Keep-Alive状态,write操作无限期阻塞.

读和写都是同步阻塞的,阻塞时间取决于对方I/O线程的处理速度和网络I/O的传输速度.

### NIO
+   与Socket类和ServerSocket类相对应，NIO也提供了SocketChannel和ServerSocketChannel，一般来说，低负载低并发的程序可以选择同步阻塞I/O以降低编程复杂度。反之选择非阻塞模式开发。
+ NIO 类库 
1. 缓冲区Buffer，最常用ByteBuffer。java.nio.ByteBuffer中flip、rewind、clear方法的区别:<http://www.blogjava.net/sdjxsgb/archive/2013/06/18/400703.html>
2. 通道Channel（全双工）,两类：用于网络读写的SelectableChannel和用于文件操作的FileChannel。SocketChannel和ServerSocketChannel都是SelectableChannel的子类。
3.  多路复用器Selector，一个Selector可以轮询多个Channel（epoll）

### AIO (NIO2.0)
+ NIO2.0提供了新的异步通道概念，并提供了异步文件通道和异步套接字通道的实现。异步通道提供两种方式获取操作结果：
1. 通过java.util.concurrent.Future类来表示异步操作的结果。
2. 在执行异步操作时传入一个java.nio.channels.CompletionHandler接口的实现类作为操作完成的回调。
+ NIO：事件驱动I/O (Reactor模式)，AIO：事件驱动I/O (Proactor模式)??,NIO和AIO区别：NIO在有通知时可以进行相关操作，AIO有通知时表示相关操作已经完成。
只有IOCP是asynchronous I/O，其他机制或多或少都会有一点阻塞。
select低效是因为每次它都需要轮询。但低效也是相对的，视情况而定，也可通过良好的设计改善
epoll, kqueue是Reacor模式，IOCP是Proactor模式。
+ NIO2.0的异步套接字通道是真正的异步非阻塞I/O，它对应UNIX网络编程中的事件驱动I/O（AIO），它不需要通过多路复用器（Selector）对注册的通道进行轮询操作即可实现异步读写，从而简化了NIO的编程模型。
+ AsynchronousServerSocketChannel
+ CompletionHandler 
+ Java7中异步IO的实现是建立在两件事情上：
1.  在异步通道上独立执行的线程（读、写、连接、关闭等）操作
2.  在操作初始化之后的控制机制，Java7 的异步 IO 操作有两种 form ：
	+ Pending Result: 这个结果返回的是 concurrent 包中的 Future 对象。
	+ Complete Result: 采用 CompleteHandler 的回调机制返回结果。
	
我们看异步通道所有的异步通道有需要遵循一个规则，在不阻塞应用去执行其他任务的情况下初始化IO操作和IO结束后的通知机制。在java7中有三个异步通道：AsynchronousFileChannel、AsynchronousSocketChannel和AsynchronousServerSocketChannel。另外还有一个比较重要的概念是group，要有组的概念是为了把资源共享，每一个异步的channel都会属于一个group，同一个group里的对象就可以共享一个线程池。这个group由AsynchronousChannelGroup,实现。
reference : <http://www.tuicool.com/articles/rYniQ3>

+ AsynchronousServerSocketChannel和CompletionHandler
1. file中的AsynchronousFileChannel.read()对应
socket中的AsynchronousServerSocketChannel.accept()
2. 其中参数attachment对应接口CompletionHandler`<V,A>` 中的第二个参数类型
V是read或accept的返回类型
3. AsynchronousServerSocketChannel.accept()返回AsynchronousSocketChannel
AsynchronousSocketChannel.read()返回Integer
AsynchronousFileChannel.read()返回Integer
+  异步SocketChannel是被动执行对象，我们不需要像NIO编程那样创建一个独立的I/O线程来处理读写操作。对于AsynchronousServerSocketChannel和AsynchronousSocketChannel，它们都由JDK底层的线程池负责回调并驱动读写操作。正因为如此，基于NIO2.0新的异步非阻塞Channel进行编程比NIO编程更为简单。
+ 1. java.nio.channels.AsynchronousChannel：标记一个 Channel 支持异步 IO 操作；
2. java.nio.channels.AsynchronousServerSocketChannel：ServerSocket 的 AIO 版本，创建 TCP 服务端，绑定地址，监听端口等；
3. java.nio.channels.AsynchronousSocketChannel：面向流的异步 Socket Channel，表示一个连接；
4. java.nio.channels.AsynchronousChannelGroup：异步 Channel 的分组管理，目的是为了资源共享。一个 AsynchronousChannelGroup 绑定一个线程池，这个线程池执行两个任务：处理 IO 事件和派发 CompletionHandler。AsynchronousServerSocketChannel 创建的时候可以传入一个 AsynchronousChannelGroup，那么通过 AsynchronousServerSocketChannel 创建的 AsynchronousSocketChannel 将同属于一个组，共享资源；
5. java.nio.channels.CompletionHandler：异步 IO 操作结果的回调接口，用于定义在 IO 操作完成后所作的回调工作。AIO 的 API 允许两种方式来处理异步操作的结果：返回的 Future 模式或者注册 CompletionHandler，推荐用 CompletionHandler 的方式，这些 handler 的调用是由 AsynchronousChannelGroup 的线程池派发的。这里线程池的大小是性能的关键因素。
reference:<http://www.ibm.com/developerworks/cn/java/j-lo-io-optimize/index.html>



### 伪异步I/O
（官方并没有wei伪异步I/O的说法）
利用线程池和消息队列避免每次请求都创建一个线程，将消息或task放到线程池，但底层仍是BIO
当队列积满时,后续队列操作将被阻塞，前端只有一个accept线程接收客户端接入,它被阻塞在线程池的同步阻塞队列之后,新的客户端请求消息将被拒绝,客户端会发生大量的连接超时。

### summary
+ NIO(包括AIO)相比BIO开发难度大，不易维护
+ 由于NIO采用了异步非阻塞编程模型，而且是一个I/O线程处理多条链路，它的调试和跟踪非常麻烦，特别是生产环境中的问题，我们无法进行有效的调试和跟踪，往往只能靠一些日志来辅助分析，定位难度很大。


## Netty

Netty 没用JDK1.7的AIO
为什么Netty不用AIO而用NIO?
According to the book the main reasons were:
1. Not faster than NIO (epoll) on unix systems (which is true)
There is no daragram suppport
2. Unnecessary threading model (too much abstraction without usage)
3. I agree that AIO will not easily replace NIO, but it is useful for windows developers nonetheless.
<https://github.com/netty/netty/issues/2515>
We obviously did not consider Windows as a serious platform so far, and that's why we were neglecting NIO.2 AIO API which was implemented using IOCP on Windows. (On Linux, it wasn't any faster because it was using the same OS facility - epoll.)

#### IOCP
IOCP：完成端口，windows平台上的一种高性能网络模型。
微软在 Winsocket2 中引入了 IOCP（Input/Output Completion Port）模型。IOCP 是 Input/Output Completion Port（I/O 完成端口）的简称。简单的说，IOCP 是一种高性能的 I/O 模型，是一种应用程序使用线程池处理异步 I/O 请求的机制。Java7 中对 IOCP 有了很好的封装，程序员可以非常方便的时候经过封装的 channel 类来读写和传输数据。<http://blog.csdn.net/wenbingoon/article/details/9237695>

IOCP （ReferenceLink|ConcreteRealization）
I/O完成端口,是Windows环境下的异步IO处理模型。IOCP采用了线程池+队列+重叠结构的内核机制完成任务。需要说明的是IOCP其实不仅可以接受套接字对象句柄，还可以接受文件对象句柄等。 IOCP本质是一种线程池的模型，当然这个线程池的核心工作就是去调用IO操作完成时的回调函数。是WINDOWS系统的一个内核对象。通过此对象，应用程序可以获得异步IO的完成通知。
<http://www.voidcn.com/blog/chen8238065/article/p-4511757.html>

nio非阻塞io jdk7实现了nio2框架 在window上通过iocp实现 在linux上通过epoll实现. netty基于nio的实现的高并发低延迟网络服务通信框架.

#### Epoll和IOCP的比较
 1. Epoll 用于 Linux 系统；而 IOCP 则是用于 Windows；
 2. Epoll 是当事件资源满足时发出可处理通知消息；而 IOCP 则是当事件完成时发出完成通知消息。
 3. 从应用程序的角度来看， Epoll 本质上来讲是同步非阻塞的，而 IOCP 本质上来讲则是异步操作；这是才二者最大的不同。
<http://www.kryptosx.info/archives/854.html>
<http://www.programgo.com/article/78532460201/>

+ ChannelHandlerAdapter

### TCP粘包和拆包
+ LineBasedFrameDecoder + StringDecoder解决TCP粘包导致的读半包或多包问题。
### 分隔符和定长解码器
+ TCP以流的方式进行数据传输，上层的应用协议为了对消息进行区分，一般采用以下4种方式：
1. 消息长度固定
2. 将回车换行符作为消息结束符
3. 将特殊的分隔符作为消息的结束标志
4. 通过在消息头中定义长度字段来标识消息的总长度
netty对应四种解码器解决对应问题，有了这些解码器，用户不需要对读取的报文进行人工解码，也不需要考虑TCP的粘包和拆包。
+  DelimiterBasedFrameDecoder(以分隔符做结束标志) FixedLengthFrameDecoder（定长消息）

---
### 编解码技术
+ 基于Java提供的对象输入/输出流ObjectInputStream和ObjectOutputStream，可以直接把Java对象作为可存储的字节数组写入文件，也可以传输到网络上。对程序员来说，基于JDK默认的序列化机制可以避免操作底层的字节数组，从而提升开发效率。
+ Java序列化的目的：
1. 网络传输
2. 对象持久化
+ 网络传输中，当进行远程跨进程服务调用时，需要把被传输的Java对象编码为字节数组或者ByteBuffer对象。而当远程服务读取到ByteBuffer对象或者字节数组时，需要将其解码为发送时的Java对象。（Java对象的编解码技术）
+ Java序列化仅仅是Java对象的编解码技术的一种，由于它的种种缺陷，衍生出了多种编解码技术和框架。
+ 在远程服务调用（RPC）时，很少直接使用Java序列化进行消息的编解码和传输。原因是（1）无法跨语言（2）序列化后的码流太大［TestUserInfo］（3）序列化性能太低
+ 业界主流的编解码框架：Protobuf（Google Protocol Buffers）,Facebook Thrift,JBoss Marshalling 
+  Java序列化： Netty的ObjectDecoder和ObjectEncoder
+ Protobuf支持数据结构化一次，可以到处使用
ProtobufEncoder 
ProtobufDecoder仅仅负责解码，它不支持读半包，因此在ProtobufDecoder前面，一定要有能够处理半包的解码器：
1. 使用Netty提供的ProtobufVarint32FrameDecoder
2. 继承Netty提供的通用半包解码器LengthFieldBasedFrameDecoder
3. 继承ByteToMessageDecoder类，自己处理半包消息。

### Netty 多协议开发
#### Http
+ HttpFileServer
+ HTTP +XML 轻量级httpserver ，XML绑定框架JiBx
#### WebSocket
讲得很好!，待会回来看，先跳过
#### UDP
未看
#### File Transfer
+ 在NIO类库提供之前，Java所有文件操作分为两类：
1.  基于字节流的InputStream和OutputStream
2.  基于字符流的Reader和Writer
+ 通过NIO新提供的FileChannel类库可以方便地以管道方式对文件进行各种I/O操作。
+ Netty - DefaultFileRegion 
+ 为了解决大文件传输过程中可能发生的内存溢出问题，Netty提供了ChunkedWriteHandler

#### 私有协议栈
+ 通信协议从广义上区分，可以分为公有协议和私有协议。公司或内部组织可能会使用私有协议，升级方便，灵活性好。绝大多数私有协议传输层都基于TCP/IP,利用Netty的NIO TCP协议栈可以方便地进行私有协议的定制和开发。
+ 在传统的Java应用中，通常使用以下4种方式进行跨节点调用：
1. 通过RMI进行远程服务调用
2. 通过Java的Socket＋Java序列化的方式进行跨节点调用
3. 利用一些开源的RPC框架进行远程服务调用，例如facebook Thrift，Apache Avro等
4. 利用标准的公有协议进行跨节点服务调用，例如HTTP＋XML，RESTful＋JSON或WebService
+ Netty协议栈 （用于内部各模块之间的通信）？ 看得不是很仔细（登录验证，心跳机制，断开重连）

---
### Netty 功能介绍与源码分析
####ByteBuf和相关辅助类
####Channel和Unsafe
####ChannelPipeline和ChannelHandler
####EventLoop和EventLoopGroup
####Future和Promise
+ Netty - ChannelFuture ,ChannelPromise

---
### 架构和行业应用
＋ Avro ，Dubbo

<http://www.iteye.com/topic/472333>