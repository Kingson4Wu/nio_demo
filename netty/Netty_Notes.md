+ ChannelPipeline接口的两个重要的方法：sendUpstream(ChannelEvent e)和sendDownstream(ChannelEvent e)。
所有事件的发起都是基于这两个方法进行的。Channels类有一系列fireChannelBound之类的fireXXXX方法，
其实都是对这两个方法的facade包装。
<http://rockeyhoo.github.io/2015/08/01/netty-learn.html>

+ 分析Netty工作流程:<http://www.cnblogs.com/NanguoCoffee/archive/2010/12/10/1902491.html>

#### Netty系列之Netty线程模型
+ <http://www.infoq.com/cn/articles/netty-threading-model>
+ Reactor模型
+ 主从多线程模型
+ 主从Reactor线程模型的特点是：服务端用于接收客户端连接的不再是个1个单独的NIO线程，而是一个独立的NIO线程池。Acceptor接收到客户端TCP连接请求处理完成后（可能包含接入认证等），将新创建的SocketChannel注册到IO线程池（sub reactor线程池）的某个IO线程上，由它负责SocketChannel的读写和编解码工作。Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端subReactor线程池的IO线程上，由IO线程负责后续的IO操作。
+ EventLoopGroup实际就是一个EventLoop线程组，负责管理EventLoop的申请和释放。
+ EventLoopGroup管理的线程数可以通过构造函数设置，如果没有设置，默认取-Dio.netty.eventLoopThreads，如果该系统参数也没有指定，则为可用的CPU内核数 × 2。
+ bossGroup线程组实际就是Acceptor线程池，负责处理客户端的TCP连接请求，如果系统只有一个服务端端口需要监听，则建议bossGroup线程组线程数设置为1。
+ workerGroup是真正负责I/O读写操作的线程组，通过ServerBootstrap的group方法进行设置，用于后续的Channel绑定。
+ NioEventLoop是Netty的Reactor线程
+ NioEventLoop继承SingleThreadEventExecutor，这就意味着它实际上是一个线程个数为1的线程池
+ 串行化设计避免线程竞争:Netty采用了串行化设计理念，从消息的读取、编码以及后续Handler的执行，始终都由IO线程NioEventLoop负责，这就意外着整个流程不会进行线程上下文的切换，数据也不会面临被并发修改的风险，对于用户而言，甚至不需要了解Netty的线程细节
+ 一个NioEventLoop聚合了一个多路复用器Selector，因此可以处理成百上千的客户端连接，Netty的处理策略是每当有一个新的客户端接入，则从NioEventLoop线程组中顺序获取一个可用的NioEventLoop，当到达数组上限之后，重新返回到0，通过这种方式，可以基本保证各个NioEventLoop的负载均衡。一个客户端连接只注册到一个NioEventLoop上，这样就避免了多个IO线程去并发操作它。
+ Netty通过串行化设计理念降低了用户的开发难度，提升了处理性能。利用线程组实现了多个串行化线程水平并行执行，线程之间并没有交集，这样既可以充分利用多核提升并行处理能力，同时避免了线程上下文的切换和并发保护带来的额外性能损耗。
+ Netty线程开发最佳实践
    - 2.4.1. 时间可控的简单业务直接在IO线程上处理
    如果业务非常简单，执行时间非常短，不需要与外部网元交互、访问数据库和磁盘，不需要等待其它资源，则建议直接在业务ChannelHandler中执行，不需要再启业务的线程或者线程池。避免线程上下文切换，也不存在线程并发问题。
    - 2.4.2. 复杂和时间不可控业务建议投递到后端业务线程池统一处理
    对于此类业务，不建议直接在业务ChannelHandler中启动线程或者线程池处理，建议将不同的业务统一封装成Task，统一投递到后端的业务线程池中进行处理。
    过多的业务ChannelHandler会带来开发效率和可维护性问题，不要把Netty当作业务容器，对于大多数复杂的业务产品，仍然需要集成或者开发自己的业务容器，做好和Netty的架构分层。
    - 2.4.3. 业务线程避免直接操作ChannelHandler
    对于ChannelHandler，IO线程和业务线程都可能会操作，因为业务通常是多线程模型，这样就会存在多线程操作ChannelHandler。为了尽量避免多线程并发问题，建议按照Netty自身的做法，通过将操作封装成独立的Task由NioEventLoop统一执行，而不是业务线程直接操作
    `ctx.executor().execute(new Runnable(){})`
    如果你确认并发访问的数据或者并发操作是安全的，则无需多此一举，这个需要根据具体的业务场景进行判断，灵活处理。

##### Netty系列之Netty高性能之道
+ <http://www.infoq.com/cn/articles/netty-high-performance>    
##### Reactor线程模型    
+ 常用的Reactor线程模型有三种，分别如下：
    - 1) Reactor单线程模型；
    - 2) Reactor多线程模型；
    - 3) 主从Reactor多线程模型    
    - Netty的线程模型并非固定不变，通过在启动辅助类中创建不同的EventLoopGroup实例并通过适当的参数配置，就可以支持上述三种Reactor线程模型。    
##### 零拷贝
+ Netty的“零拷贝”主要体现在如下三个方面：
    - Netty的接收和发送ByteBuffer采用DIRECT BUFFERS，使用堆外直接内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。
    - Netty提供了组合Buffer对象，可以聚合多个ByteBuffer对象，用户可以像操作一个Buffer那样方便的对组合Buffer进行操作，避免了传统通过内存拷贝的方式将几个小Buffer合并成一个大的Buffer。
    - Netty的文件传输采用了transferTo方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免了传统通过循环write方式导致的内存拷贝问题。
##### 无锁化的串行设计理念
##### 高性能的序列化框架
+ Netty默认提供了对Google Protobuf的支持，通过扩展Netty的编解码接口，用户可以实现其它的高性能序列化框架，例如Thrift的压缩二进制编解码框架。
##### 灵活的TCP参数配置能力
+ SO_RCVBUF和SO_SNDBUF：通常建议值为128K或者256K；
+ SO_TCPNODELAY：NAGLE算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率。但是对于时延敏感的应用场景需要关闭该优化算法；
+ 软中断：如果Linux内核版本支持RPS（2.6.35以上版本），开启RPS后可以实现软中断，提升网络吞吐量。RPS根据数据包的源地址，目的地址以及目的和源端口，计算出一个hash值，然后根据这个hash值来选择软中断运行的cpu，从上层来看，也就是说将每个连接和cpu绑定，并通过这个hash值，来均衡软中断在多个cpu上，提升网络并行处理性能。
 
 

    
    