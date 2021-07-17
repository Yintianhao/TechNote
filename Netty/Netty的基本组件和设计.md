##  Netty的基本组件和设计
### Channel(Socket)
Channel接口实现基本的IO操作，bind(),connect(),read(),write()。          
### EventLoop(控制流，多线程处理，并发)
EventLoop顾名思义(事件循环)是用来处理事件的，说详细点是用来处理连接的生命周期中发生的事件。一个EventLoopGroup包含一个或者多个EventLoop，一个EventLoop在其生命周期内只和一个Thread绑定，所有由EventLoop处理的IO事件都将在其专有的Thread里进行处理，一个Channel在它的生命周期内只注册一个EventLoop，但一个EventLoop可能被分配一个或者多个Channel。
### ChannelFuture（异步通知）
由于Netty所有的操作都是异步处理的，所有一个操作不会立即返回，所以需要一个能够在之后的某个时间点能够得到前面操作的结果的方法，这就是ChannelFuture的由来，用其addListener方法可以注册一个监听器用来监听操作是否完成。
### ChannelHandler
ChannelHandler主要是处理业务逻辑的地方，对于我们开发人员来说就是处理出站和入站数据的地方。
### ChannelPipeline
ChannelPipeline是容纳ChannelHandler的地方，定义了用于该链上传播入站和出站事件流的API，当Channel被创建的时候，它会被自动分配到专属的ChannelPipeline。ChannelHandler安装到ChannelPipeline中的过程如下所示：
```
一个ChannelInitializer的实现被注册到ServerBootstrap中。然后当ChannelInitializer的initChannel方法被调用的时候，ChannelInitializer将在ChannelPipeline中安装一组自定义的ChannelHandler。ChannelInitializer将它自己从ChannelPipeline中移除。
```

ChannelHandler和ChannelPipeline之间的关系，ChannelHandler是专门为支持广泛的用途而设计的，可以将它看做是处理ChannelPipeline事件的任何代码的通用处理器，使得事件流经ChannelPipeline是ChannelHandler的工作，ChannelHandler是在程序初始化的时候就已经安装了的，它们的执行顺序是由他们被添加的顺序决定的，所以ChannelPipeline也就是ChannelHandler的编排顺序。

### 引导类

- Bootstrap 

客户端引导类，功能是连接远程主机和端口，需要绑定一个ChannelPipeline

- ServerBootstrap

服务器引导类，功能是监听某一个端口，但是需要两个ChannelPipeline，之所以需要两个是因为服务器需要两组Channel，一组是用来表示和客户端之间连接的套接字，另一组将包含所有已经创建的用来处理传入客户端连接的Channel。

