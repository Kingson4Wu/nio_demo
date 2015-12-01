package com.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * 简单Mina Server示例
 * @see 非阻塞I/O是JDK5.0提供的API，意思是服务器不用像以前那样调用accept()方法，阻塞等待了
 * @see 开发一个Mina应用，简单的说：就是创建连结、设定过滤规则、编写自己的消息处理器这三步
 * @see Mina执行流程：进入IoService-->IoProcessor-->IoFilter-->IoHandler-->IoFilter-->IoProcessor-->IoService
 */
public class MyServer {
	public static void main(String[] args) throws IOException {
		//指定服务器端所绑定的端口
		int bindPort = 9876;
		
		//初始化服务端的TCP/IP的基于NIO的套接字
		//即创建非阻塞服务器端，类似于Java中的ServerSocket
		IoAcceptor acceptor = new NioSocketAcceptor();
		
		//调用IoSessionConfig设置读取数据的缓冲区大小、读写通道均在10秒内无任何操作就进入空闲状态
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		
		/**
		 * 定义拦截器：可以包括日志输出、黑名单过滤、数据的编码(write方向)与解码(read方向)等功能
		 *           其中数据的encode与decode是最为重要的，也是在使用Mina时最主要关注的地方
		 */
		
		//启用Mina的日志跟踪
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		
		//这段代码要在acceptor.bind()方法之前前执行，因为绑定套接字之后，就不能再做这些准备工作了
		//这里所要传输的是以换行符为标识的数据，所以使用了Mina自带的换行符编解码器工厂
		//若不清楚操作系统或Telnet软件的换行符是什么，可以删掉new TextLineCodecFactory(*,*,*)的后两个参数
		//即new TextLineCodecFactory(Charset.forName("UTF-8"))，此时使用的就是TextLineCodec内部的自动识别机制
		//acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		acceptor.getFilterChain().addLast("codec", 
			new ProtocolCodecFilter(new TextLineCodecFactory(
				Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
		
		/**
		 * 指定服务器端的消息处理器。它负责编写业务逻辑，即接收、发送数据的地方
		 */
		
		//把编写好的IoHandler注册到IoService。它也要在acceptor.bind()方法之前前执行
		acceptor.setHandler(new ServerHandler());
		
		//绑定端口，启动服务器
		//该接口中的void bind()方法用于监听端口、void unbind()方法用于解除对套接字的监听
		//这里与传统的Java中的ServerSocket不同的是：IoAcceptor可以多次调用bind()方法同时监听多个端口
		//或者在一个方法中传入多个SocketAddress参数，来监听多个端口
		acceptor.bind(new InetSocketAddress(bindPort));
		
		System.out.println("MinaServer is startup, and it`s listing on := " + bindPort);
	}
}