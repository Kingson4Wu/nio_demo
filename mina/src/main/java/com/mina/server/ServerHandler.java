package com.mina.server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * 自定义的消息处理器，必须实现IoHandlerAdapter类 
 * @see org.apache.mina.core.service.IoHandlerAdapter：它定义的方法用于处理程序接收到的消息，并处理通信中的连结，断开，消息到达等事件
 * @see                   客户机和服务器端创建后，都有一个setHandler方法，就是要传入我们重写的该类的对象
 * @see                   其中各个方法在通信中会根据情况自动调用，类似于Swing事件中的调用机制
 */
public class ServerHandler extends IoHandlerAdapter {
	//这是IoHandlerAdapter类中最重要的一个方法。IoSession代表与对方机器的TCP/IP连接，Object代表接收到的数据
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String str = message.toString(); //我们已设定了服务器解析消息的规则是一行一行读取，这里就可转为String
		System.out.println("The message received from Client is [" + str + "]");
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception{
		System.out.println("InComing Client：" + session.getRemoteAddress());
	}
}