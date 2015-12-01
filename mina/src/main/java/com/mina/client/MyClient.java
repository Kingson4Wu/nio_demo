package com.mina.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * 简单的TCPClient
 * @see Mina中的Server端和Client端的执行流程是一样的，唯一不同的是IoService的Client端实现是IoConnector
 * @see 这里我们实现Mina中的TCPClient。运行MyClient时，会发现MyServer控制台输入如下语句
 * @see The message received from Client is [岂曰无衣..]
 * @see The message received from Client is [月照沟渠....]
 * @see 说明服务器端收到的是两条消息，因为我们所用的编解码器是以换行符判断数据是否读取完毕的
 */
public class MyClient {
	public static void main(String[] args) {
		//Create TCP/IP connector
		//NioSocketConnector功能类似于JDK中的Socket类，它也是非阻塞的读取数据
		IoConnector connector = new NioSocketConnector();

		connector.setConnectTimeoutMillis(3000);
		
		connector.getFilterChain().addLast("codec", 
			new ProtocolCodecFilter(new TextLineCodecFactory(
				Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
		
		//注册IoHandler，即指定客户器端的消息处理器
		connector.setHandler(new ClientHandler("岂曰无衣..\r\n月照沟渠...."));
		
		//连接到服务器
		//ConnectFuture connect(SocketAddress arg0,SocketAddress arg1)
		//该方法用于与Server端建立连接，第二个参数若不传递则使用本地的一个随机端口访问Server端
		//该方法是异步执行的，且可以同时连接多个服务端
		connector.connect(new InetSocketAddress("127.0.0.1", 9876));
		
		System.out.println("Mina Client is startup");
	}
}