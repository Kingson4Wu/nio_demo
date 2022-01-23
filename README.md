
由于 NIO 使用起来较为困难，所以许多公司推出了自己封装 JDK NIO 的框架，
例如 Apache 的 Mina，JBoss 的 Netty，Sun 的 Grizzly 等等，这些框架都直接封装了传输层的 TCP 或 UDP 协议.


---

+ Selector 实现原理:<http://www.jianshu.com/p/2b71ea919d49>
    - epoll的两种工作模式：
        + LT：level-trigger，水平触发模式，只要某个socket处于readable/writable状态，无论什么时候进行epoll_wait都会返回该socket。
        当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序可以不立即处理该事件。下次调用epoll_wait时，会再次响应应用程序并通知此事件。
        + ET：edge-trigger，边缘触发模式，只有某个socket从unreadable变为readable或从unwritable变为writable时，epoll_wait才会返回该socket。
        当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。
        ET模式在很大程度上减少了epoll事件被重复触发的次数，因此效率要比LT模式高。epoll工作在ET模式的时候，必须使用非阻塞套接口，以避免由于一个文件句柄的阻塞读/阻塞写操作把处理多个文件描述符的任务饿死。
    - 在Linux系统中JDK NIO使用的是 LT ，而Netty epoll使用的是 ET。    
    
    
+ JAVA NIO 一步步构建I/O多路复用的请求模型:<https://github.com/jasonGeng88/blog/blob/master/201708/java-nio.md>
+ t-io: 不仅仅是百万级即时通讯框架: <http://git.oschina.net/tywo45/t-io>      


----

/Klay11/os-im/README.md


