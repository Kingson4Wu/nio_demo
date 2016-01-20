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
package com.phei.netty.protocol.http.fileServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class HttpFileServer {

	//http://localhost:8080/src/com/phei/netty/
    private static final String DEFAULT_URL = "/src/com/phei/netty/";

    public void run(final int port, final String url) throws Exception {
	EventLoopGroup bossGroup = new NioEventLoopGroup();
	EventLoopGroup workerGroup = new NioEventLoopGroup();
	try {
	    ServerBootstrap b = new ServerBootstrap();
	    b.group(bossGroup, workerGroup)
		    .channel(NioServerSocketChannel.class)
		    .childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch)
				throws Exception {
			    ch.pipeline().addLast("http-decoder",
				    new HttpRequestDecoder());//Http请求消息解码器
			    ch.pipeline().addLast("http-aggregator",
				    new HttpObjectAggregator(65536));
				//HttpObjectAggregator解码器,将多个消息转换为单一的FullHttpRequest或者FullHttpResponse,原因时HTTP解码器在每个http消息中会生成多个消息对象
				//(1)HttpRequest/HttpResponse (2)HttpContent (3)LastHttpContent
			    ch.pipeline().addLast("http-encoder",
				    new HttpResponseEncoder());
				//HTTP响应解码器,对HTTP响应消息进行编码
			    ch.pipeline().addLast("http-chunked",
				    new ChunkedWriteHandler());
				//支持异步发送大的码流(例如大文件传输),但不占用过多的内存,防止发生Java内存溢出错误
			    ch.pipeline().addLast("fileServerHandler",
				    new HttpFileServerHandler(url));
				//文件服务器业务逻辑处理
			}
		    });
	    ChannelFuture future = b.bind("127.0.0.1", port).sync();
	    System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://192.168.1.102:"
		    + port + url);
	    future.channel().closeFuture().sync();
	} finally {
	    bossGroup.shutdownGracefully();
	    workerGroup.shutdownGracefully();
	}
    }

    public static void main(String[] args) throws Exception {
	int port = 8080;
	if (args.length > 0) {
	    try {
		port = Integer.parseInt(args[0]);
	    } catch (NumberFormatException e) {
		e.printStackTrace();
	    }
	}
	String url = DEFAULT_URL;
	if (args.length > 1)
	    url = args[1];
	new HttpFileServer().run(port, url);
    }
}
