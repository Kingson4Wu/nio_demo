/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Administrator
 * @date 2014年2月16日
 * @version 1.0
 */
public class TimeClientHandle implements Runnable {

    private String host;
    private int port;

    private Selector selector;
    private SocketChannel socketChannel;

    private volatile boolean stop;

    public TimeClientHandle(String host, int port) {
	this.host = host == null ? "127.0.0.1" : host;
	this.port = port;
	try {
	    selector = Selector.open();
	    socketChannel = SocketChannel.open();
	    socketChannel.configureBlocking(false);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
	try {
	    doConnect();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	while (!stop) {
	    try {
		selector.select(1000);
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> it = selectedKeys.iterator();
		SelectionKey key = null;
		while (it.hasNext()) {
		    key = it.next();
		    it.remove();
		    try {
			handleInput(key);
		    } catch (Exception e) {
			if (key != null) {
			    key.cancel();
			    if (key.channel() != null)
				key.channel().close();
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}

	// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
	if (selector != null)
	    try {
		selector.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

    }

    private void handleInput(SelectionKey key) throws IOException {

	if (key.isValid()) {
	    // 判断是否连接成功
	    SocketChannel sc = (SocketChannel) key.channel();
	    if (key.isConnectable()) {
		if (sc.finishConnect()) {
		    sc.register(selector, SelectionKey.OP_READ);
		    doWrite(sc);
		} else
		    System.exit(1);// 连接失败，进程退出
	    }
	    if (key.isReadable()) {
		ByteBuffer readBuffer = ByteBuffer.allocate(1024);
		int readBytes = sc.read(readBuffer);
		if (readBytes > 0) {
		    readBuffer.flip();
		    byte[] bytes = new byte[readBuffer.remaining()];
		    readBuffer.get(bytes);
		    String body = new String(bytes, "UTF-8");
		    System.out.println("Now is : " + body);
		    this.stop = true;
		} else if (readBytes < 0) {
		    // 对端链路关闭
		    key.cancel();
		    sc.close();
		} else
		    ; // 读到0字节，忽略
	    }
	}

    }

    private void doConnect() throws IOException {
	// 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
	if (socketChannel.connect(new InetSocketAddress(host, port))) {
		//判断是否连接成功,若成功则直接注册读状态到selector,否则(异步连接,返回false,说明客户端已经发送sync包,服务端没有返回ack包,物理链路还没有建立)
	    socketChannel.register(selector, SelectionKey.OP_READ);
	    doWrite(socketChannel);
	} else
	    socketChannel.register(selector, SelectionKey.OP_CONNECT);//监听服务端ACK应答
    }

	/**
	 * 客户端发起的读写操作是异步的,可以通过在selector注册OP_CONNECT等待后续结果,不需要像之前的客户端那样同步阻塞
	 */



    private void doWrite(SocketChannel sc) throws IOException {
	byte[] req = "QUERY TIME ORDER".getBytes();
	ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
	writeBuffer.put(req);
	writeBuffer.flip();
	sc.write(writeBuffer);
	if (!writeBuffer.hasRemaining())
	    System.out.println("Send order 2 server succeed.");
    }

}
