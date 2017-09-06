
由于 NIO 使用起来较为困难，所以许多公司推出了自己封装 JDK NIO 的框架，
例如 Apache 的 Mina，JBoss 的 Netty，Sun 的 Grizzly 等等，这些框架都直接封装了传输层的 TCP 或 UDP 协议.


---

+ Selector 实现原理:<http://www.jianshu.com/p/2b71ea919d49>
    - epoll的两种工作模式：
        + LT：level-trigger，水平触发模式，只要某个socket处于readable/writable状态，无论什么时候进行epoll_wait都会返回该socket。
        + ET：edge-trigger，边缘触发模式，只有某个socket从unreadable变为readable或从unwritable变为writable时，epoll_wait才会返回该socket。
    - 在Linux系统中JDK NIO使用的是 LT ，而Netty epoll使用的是 ET。    
    
    
+ JAVA NIO 一步步构建I/O多路复用的请求模型:<https://github.com/jasonGeng88/blog/blob/master/201708/java-nio.md>
+ t-io: 不仅仅是百万级即时通讯框架: <http://git.oschina.net/tywo45/t-io>        