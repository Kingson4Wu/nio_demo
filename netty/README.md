Netty的并发处理能力主要体现在两个方面：

利用Java语言自身的多线程机制实现消息的并行处理；
利用Java NIO类库的Selector实现多路复用，一个NIO线程可以同时并发处理成百上千个通信链路，实现海量客户端的并发接入和处理。


<https://mp.weixin.qq.com/s?__biz=MjM5MjAwODM4MA==&mid=209275660&idx=3&sn=efa049cf2b32a8d69214f77d80a79ae4#rd>

