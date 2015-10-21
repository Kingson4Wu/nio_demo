package com.kxw.demo.udp.spring;

public interface IServer {
	 /** 
     * 启动服务器 
     */  
    public void start();  
    /** 
     * 重启程序 
     */  
    public void restart();  
      
    /** 
     * 停止程序运行 
     */  
    public void stop(); 
}
