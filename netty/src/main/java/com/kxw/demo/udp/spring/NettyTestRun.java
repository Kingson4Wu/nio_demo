package com.kxw.demo.udp.spring;

import org.springframework.context.ApplicationContext;  
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * {<a href='http://blog.csdn.net/tanrenzong1986/article/details/6404256'>@link</a>}
 */
public class NettyTestRun   
{  
    public static void main( String[] args )  
    {  
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:nettyTest-context.xml");  
        IServer server=(IServer)context.getBean("serverNettyImpl");  
        server.start();  
    }  
}

/**
 *
 * -----------------

 netty有几个比较重要的概念，在此，仅做介绍，详细可以参考netty文档或源码。

 1). channelBuffer： 是 Netty 的一个基本数据结构，这个数据结构存储了一个字节序列。 类似于 NIO 的 ByteBuffer，但操作起来比ByteBuffer更为简单方便。

 2). ChannelFactory 是一个创建和管理 Channel 通道及其相关资源的工厂接口，它处理所有的 I/O 请求并产生相应的 I/O ChannelEvent 通道事件。

 3).ChannelHandler是所有I/O ChannelEvent事件的响应类，所有消息，包括netty通信异常事件，均在该响应类内处理。

 4).*** Bootstrap 是一个设置服务的帮助类。你甚至可以在这个服务中直接设置一个 Channel 通道。

 现在以实现一个UDP协议下的服务器应用程序为例，演示netty通过spring注解开发服务器端。（在此以maven工具管理项目开发）

 --------
 运行起来，可以看到服务器侦听在5000端口并接受客户端的信息并返回一个int随机数。

 后记：netty当然也可以应用在TCP和客户端的程序，具体的使用可以参考netty API文档。
 *
 */

/**
 echo "Hello World\!" | nc -4u  127.0.0.1 5000

 奇怪只能发一次。。。

 lsof -i:5000

 lsof -i:5000
 COMMAND   PID      USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
 nc      99608 kingsonwu    3u  IPv4 0xb826f29a2037b49d      0t0  UDP localhost:57394->localhost:commplex-main




 */