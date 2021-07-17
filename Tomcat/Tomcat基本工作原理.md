## Tomcat

### Tomcat8 的主要目录结构

- bin 存放可执行文件
- conf 存放配置文件
- lib 存放Tomcat运行需要的包
- logs 存放运行的日志
- webapps 存放默认的web应用部署目录
- work 存放web应用代码生成和编译文件的临时目录

### 组件结构

- connector，负责接收和反馈外部请求的连接器
- container，负责处理请求的容器
- service，由connecter和container一起构成，Tomcat可以管理多个service，每个service之间相互独立

### 连接器

- 核心功能

1，监听网络端口，接收和相应网络请求。

2，网络字节流处理，将收到的网络字节流转换成TomcatRequest再转成标准的ServletRequest给容器。

同时将容器传回来的ServletResponse转为TomcatResponse再转成网络字节流。

-  模块设计

1，EndPointer，端点，用来处理Socket接收和发送的逻辑，内部由Acceptor监听请求，Handler处理数据，AsyncTimeout检查请求超时，具体实现有NioEndPoint，AprEndPointer。

2，Processor，处理器，负责构建TomcatRequest和TomcatResponse对象，具体的实现是Http11Processor，StreamProcessor等等。

3，Adapter，适配器，实现TomcatRequest和ServletRequest的互相转换，采用了经典的适配器模式。

4，ProtocolHandler，协议处理器，将不同协议和通讯方式组合封装成对应的协议处理器。

![avatar](https://media.izzer.cn/tomcat连接器结构.jpg)

### 容器

- 容器结构

每个Service会包含一个容器，容易有一个引擎，可以管理多个虚拟主机，每个虚拟主机可以管理多个web应用，每个web应用会有多个Servlet包装器，

1，Engine，引擎，可以管理多个虚拟主机

2，Host，虚拟主机，负责多个应用的部署

3，Context，web应用，包含多个Servlet封装器

4，Wrapper，封装器，容器的最底层，对Servlet进行封装，负责实例的创建，执行和销毁。

这四个按顺序为父子关系。



### 容器请求处理

容器的请求处理过程就是上面所提到的四个容器之间的层层调用，最后在Servlet中执行相应的业务逻辑，各个容器都会有一个通道pipeline，每个通道上都会有一个BasicValue，类似于一个闸门，用来处理Request和Response。

![avatar](https://media.izzer.cn/Tomcat容器请求处理流程.png)

### 映射器

映射器，将请求url的地址匹配由哪个容器来处理，其中每个容器都有自己实现自己的映射器。

### 浅析Tomcat处理一个http请求的流程

以http://localhost:8080/docs/api为例

1，连接器监听的端口是8080，连接器接收该请求。

2，引擎的默认虚拟主机是localhost，并且虚拟主机的目录是webapps，所以请求找到tomcat/webapps目录

3，解析docs是web程序的应用名，也就是context上下文，此时请求继续从webapps目录下找到docs目录。

4，解析的api是具体的业务逻辑地址，此时需要从docs/WEB-INF/web.xml中找到映射关系，最后调用具体的函数。

![avatar](https://media.izzer.cn/Tomcat处理一个http请求.jpg)

### Springboot启动内嵌Tomcat的过程

说实话，一直在用Springboot，确实还没了解过内嵌Tomcat的使用方法。中小型项目虽然可以直接内嵌Tomcat启动，但是遇到大型项目，需要用到Tomcat集群，并且需要调优，那可能就不能满足了。

从SpringBoot main函数开始看

```
SpringAppliation.run();
```

![avatar](https://media.izzer.cn/Springboot启动方法.jpg)

```
public void refresh() throws BeansException, IllegalStateException {
        Object var1 = this.startupShutdownMonitor;
        synchronized(this.startupShutdownMonitor) {
            this.prepareRefresh();
            ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
            this.prepareBeanFactory(beanFactory);

            try {
                this.postProcessBeanFactory(beanFactory);
                this.invokeBeanFactoryPostProcessors(beanFactory);
                this.registerBeanPostProcessors(beanFactory);
                this.initMessageSource();
                this.initApplicationEventMulticaster();
                this.onRefresh();
                this.registerListeners();
                this.finishBeanFactoryInitialization(beanFactory);
                this.finishRefresh();
            } catch (BeansException var9) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
                }

                this.destroyBeans();
                this.cancelRefresh(var9);
                throw var9;
            } finally {
                this.resetCommonCaches();
            }

        }
    }
```

继续往里面看就是上面这个refresh方法，然后在看到这里的onRefresh方法，熟悉的肯定这个名字就是典型的模板方法，那继续看下去。在ServletWebServerApplicationContxt再看onRefresh()

```
    protected void onRefresh() {
        super.onRefresh();

        try {
            this.createWebServer();
        } catch (Throwable var2) {
            throw new ApplicationContextException("Unable to start web server", var2);
        }
    }
```

这里就会创建WebServer了，再看这里头做了什么。

```
    private void createWebServer() {
        WebServer webServer = this.webServer;
        ServletContext servletContext = this.getServletContext();
        if (webServer == null && servletContext == null) {
            ServletWebServerFactory factory = this.getWebServerFactory();
            this.webServer = factory.getWebServer(new ServletContextInitializer[]{this.getSelfInitializer()});
            this.getBeanFactory().registerSingleton("webServerGracefulShutdown", new WebServerGracefulShutdownLifecycle(this.webServer));
            this.getBeanFactory().registerSingleton("webServerStartStop", new WebServerStartStopLifecycle(this, this.webServer));
        } else if (servletContext != null) {
            try {
                this.getSelfInitializer().onStartup(servletContext);
            } catch (ServletException var4) {
                throw new ApplicationContextException("Cannot initialize servlet context", var4);
            }
        }

        this.initPropertySources();
    }
```

看到getWebServerFactory，去找对应的实现方法。在TomcatServletWebServerFactory里找到对应的方法。

```
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        if (this.disableMBeanRegistry) {
            Registry.disableRegistry();
        }

        Tomcat tomcat = new Tomcat();
        File baseDir = this.baseDirectory != null ? this.baseDirectory : this.createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        Connector connector = new Connector(this.protocol);
        connector.setThrowOnFailure(true);
        tomcat.getService().addConnector(connector);
        this.customizeConnector(connector);
        tomcat.setConnector(connector);
        tomcat.getHost().setAutoDeploy(false);
        this.configureEngine(tomcat.getEngine());
        Iterator var5 = this.additionalTomcatConnectors.iterator();

        while(var5.hasNext()) {
            Connector additionalConnector = (Connector)var5.next();
            tomcat.getService().addConnector(additionalConnector);
        }

        this.prepareContext(tomcat.getHost(), initializers);
        return this.getTomcatWebServer(tomcat);
    }
```

这里终于可以看到和之前整理的Tomcat结构相关的东西了，在这里配置了基本的连接器，引擎和虚拟站点。



