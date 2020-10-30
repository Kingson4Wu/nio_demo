1. tio-utils
tio-utils是笔者在项目开发中积累的部分工具类
里面有少部分代码是在开源许可范围内摘自第三方开源项目代码的，还有部分代码是其它开源作者提供的，譬如hutool的作者路神就提供了许多类，在此也是表示感谢！笔者这么做，仅仅是因为广大用户强力要求tio减少第三方依赖！
当然笔者更愿意使第三方工具类，譬如hutool，毕竟和hutool的作者是基友
在tio-utils中目前鄙人用得最多的Cache
首先这个Cache是个门面——把市面上的各路Cache统一成了ICache，操作方法统一了，
其次它内置了一级cache，两级cache，并且性能极好、操作省心、稳定性也在大量项目中得到了考验
哦，这么说，其实就是想挑战J2cache，不过tio-utils也把J2cache门面化了^_^
2. tio-core
大家口中的t-io或tio指的就是tio-core，这个一定要记住，要不然会混掉
tio-core是依赖tio-utils的
tio-core是基于java aio的网络编程框架（很多人说t-io是基于netty，大家不要听信这样不负责任的言论）
如果你知道netty是啥，那理解tio-core就很容易了，因为tio-core是和netty类似的框架
关于社区的问答
问：市面上已经有netty这样优秀的框架，为何还要自己写一个tio-core？
答：每个人心中有杆秤，你认为优秀的，不代表所有人都这样认为
问：和netty比，tio-core有何优势和劣势？
答：很难回答这个问题，说几个事实：
在t-io没提供任何文档的情况下，许多用户仅依靠笔者提供的示范工程就掌握了tio，并用于生产项目中，这表明t-io极其容易上手
tio-mvc是基于tio-http-server的mvc框架，它在TFB上的性能排名不差（不得不严肃提醒各位：TFB上部分排在tio-mvc前面的框架，并不具备用于生产项目的能力，而只是DEMO级别的，譬如协议容错、协议防攻击、session支持、分布session支持，流控这些都没有，重要的是普通开发工程师难以入手）
t-io在TFB上最新一轮的排名
tio-mvc在TFB上的PK排名表明t-io性能很好
了解t-io的历史，t-io是从前线走出来的作品，而且久经考验！
性能达到一定高度的前提下，再对比性能，其已经意义不大，稳定性、易用性、坑多否才是后面主要的竞技场！
3. tio-http-common
一个给tio-http-server和tio-http-client共用的工程，大家可以略过
4. tio-http-server
基于tio-core（为啥不说是基于tio-http-common？怎么说都可以）实现的http服务器
内置了极易使用的MVC框架
内置了流控、拉黑、forward、拦截器等常用能力
性能优秀，前面已经有地方描述了它在TFB上的性能表现，在TFB上tio-mvc的性能远超使用人群最多的springmvc，当然这不是说springmvc不优秀，而是说性能到这份上了，再说性能没啥意义！
5. tio-websocket-common
一个给tio-websocket-server和tio-websocket-client共用的工程，大家可以略过
6. tio-websocket-server
基于tio-core（为啥不说是基于tio-websocket-common？怎么说都可以）实现的websocket服务器
请注意：tio-http-server和tio-websocket-server是分开的，不能在同一端口用tio-http-server和tio-websocket-server，这么样的原因很简单：为了提升性能。
“哦，那为什么其它框架是放在一起的？”
“我想说的是websocket协议是后来硬扯在http协议之上的，从设计上来讲：这俩货根本就不应该呆在一起，搞得笔者在实现websocket协议时，还得用http协议来完成握手”
tio-webpack-core
笔者在tio-http-server的基础之上依赖freemarker实现的类似nodejs webpack的功能，现在还没完全封装到位，就笔者一人在用
你现在正在浏览的网页就是基于tio-webpack-core的，不信你用右键点击查看源代码，全TM压缩或加密的^_^