+ ChannelPipeline接口的两个重要的方法：sendUpstream(ChannelEvent e)和sendDownstream(ChannelEvent e)。
所有事件的发起都是基于这两个方法进行的。Channels类有一系列fireChannelBound之类的fireXXXX方法，
其实都是对这两个方法的facade包装。
<http://rockeyhoo.github.io/2015/08/01/netty-learn.html>

