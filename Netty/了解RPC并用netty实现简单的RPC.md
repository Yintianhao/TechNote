## 使用netty实现简单的RPC

### 前言

最近在做一个自己的小项目，这个小项目分为客户端，路由和服务端，服务端和客户端之间通过Netty来通信，而路由主要用来做负载均衡，用户上线下线的操作，客户端和路由之间本来是采用HTTP来通信，后来一想既然都用到netty了，那就干脆基于netty做一个RPC来实现客户端和路由的通信吧。

### RPC的概念

RPC，全称是Remote Procedure Call，从字面意思上也容易理解，通常写代码，比如本地写一个类A，里面一个方法B，我们要调用A类的B方法就很容易，这叫本地方法调用。而如今，现在很多服务都是在不同的服务器上，如果还是这样的场景，我们要调用另外一台服务器上的A类的B方法，这时候就可以用RPC，这使得我们调用A类的B方法就如同调用本地方法一样。



### RPC的实现

可以先简单罗列一下，实现上面的想法需要哪些东西	

- 网络通信 netty

这必然是需要的，因为这里服务在两台服务器上，这里使用netty来作为两端之间的通信框架。

- 解码编码 Jackson

为了方便通信，使用Json来作为统一的编码格式，使用spring默认的Jackson来实现json和对象之间的转化。

- 服务注册与发现 Nacos

服务端是多实例部署，那自然需要一个注册中心，客户端选择的时候可以通过一定的负载均衡算法来选择一个服务实例进行通信。这里使用阿里的nacos。

- 心跳机制

客户端和服务端建立连接之后每隔一定的时间上报心跳，这里使用netty自带的心跳机制来实现。

- 反射和动态代理

客户端是没有调用的接口的具体实现的，所以在服务端需要从请求报文里得到请求的接口，参数，方法名等信息，通过反射来得到具体的类和方法，再通过动态代理来实现具体的接口方法的调用。

- 项目脚手架 Springboot
- ......

当然，这里如果要做细致还有很多东西需要考虑，包括熔断，限流，缓存，这些之后再考虑，这里先完成我们最初的需求。接下来一个个把这些都实现。为此花了一个简单的图来方便理解这里面有啥需要实现的模块。

![avatar](https://media.izzer.cn/RPC%E7%AE%80%E5%8D%95%E9%A1%B9%E7%9B%AE%E5%9B%BE.jpg)

#### 公共模块

- 实体类

这里介绍两个主要的实体，一个是请求类

```
public class RpcRequest {
    /**
     * 请求id，唯一，有雪花算法生成
     * */
    @Getter
    @Setter
    private Long id;

    /**
     * 请求类型：正常请求0，心跳：1
     * */
    @Getter
    @Setter
    private RequestTypeEnum type;

    /**
     * 类名称
     * */
    @Getter
    @Setter
    private String className;

    /**
     * 指定运行的方法名称
     * */
    @Getter
    @Setter
    private String methodName;

    /**
     * 参数类型
     * */
    @Getter
    @Setter
    private Class<?>[] paramTypes;

    /**
     * 参数值
     * */
    @Getter
    @Setter
    private Object[] params;

}

```

另一个是回复类

```
public class RpcResponse {

    /**
     * 请求的ID
     * */
    @Getter
    @Setter
    private Long reqId;

    /**
     * 返回码
     * */
    @Getter
    @Setter
    private RespCodeEnum code;

    /**
     * 发生错误时的错误信息
     * */
    @Getter
    @Setter
    private String errorMsg;

    /**
     * 正常返回下返回信息
     * */
    @Getter
    @Setter
    private Object data;

}
```

- 工具类

工具类包括对对象和Json字符串/字节数组进行相互转化的JsonUtil，以及一个用来生成请求ID的算法，算法采用的是Twitter开源的雪花算法，代码是从网上借鉴来的。Json是用的Jackson做的解析。

```
public class JsonUtil{

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转Json字符串
     * */
    public static String parseToJsonStr(Object object) {
        String result = null;
        try {
            result = mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 字节数组转Json字符串
     * */
    public static byte[] parseToJsonBytes(Object object) {
        byte[] result = null;
        try {
            result = mapper.writeValueAsBytes(object);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Json字符串转为对象
     * */
    public static <T> T parseToObject(String json,Class<T> clazz){
        T t = null;
        try {
            t = mapper.readValue(json,clazz);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return t;
    }


    /**
     * 字节数组转为对象
     * */
    public static <T> T parseToObject(byte[] jsonBytes,Class<T> clazz){
        T t = null;
        try {
            t = mapper.readValue(jsonBytes,clazz);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 获取ObjectMapper
     * */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}

```

#### 服务注册和发现

Nacos是阿里的开源的一款中间件，既有服务注册发现，也有配置中心的作用，在java工程里这两个依赖也是独立的。使用方面，跟着官网文档走就可以了。

- 服务注册

先需要把Nacos的地址，服务名称，namespace这些配置好。

```
spring:
  application:
    name: rpc_producer
server:
  port: 7001

rpc:
  heartbeat: 600

app:
  ip: 127.0.0.1
  port: 8001

nacos:
  discovery:
    server-addr: 127.0.0.1:8848
    namespace: 35375ce2-f421-431f-bd2e-89677440dc9f
    register:
      group-name: netty_rpc_provider
```

接下来就是注册了，注册一个实例需要服务名，服务所在的IP和端口。

```

@EnableDiscoveryClient
@SpringBootApplication
@Slf4j
public class RpcProducerApplication implements CommandLineRunner {

    @NacosInjected
    private NamingService namingService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.port}")
    private Integer serverPort;

    @Value("${app.ip}")
    private String serverIp;

    public static void main(String[] args) {
        SpringApplication.run(RpcProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(String.format("Register service name:%s,port:%s",appName,serverPort));
        namingService.registerInstance(appName,serverIp,serverPort);
    }
}
```



- 服务发现

  服务发现相比于服务注册多了一些步骤，因为服务端是多实例的，所以需要获取当前服务的所有实例，然后采用一定的负载均衡算法来选择其中一个实例。这里本项目暂且使用轮询算法来做负载均衡。		

  - 获取Nacos中的所有实例，然后将Channel的列表交给专门的连接管理器来维护。ConnectionManager后续在负载均衡的时候用到。

  ```
  @Component
  public class RpcServiceDiscovery {
  
      private static final Logger logger = LoggerFactory.getLogger(RpcServiceDiscovery.class);
  
      @NacosInjected
      private NamingService namingService;
  
      @Value("${rpc.provider}")
      private String providerRegisterName;
  
      @Autowired
      private ConnectionManager connectionManager;

      private volatile List<ServerAddress> serverList = new ArrayList<>();

      @PostConstruct
    public void init(){
          List<Instance> instanceList;
          try {
              instanceList = namingService.getAllInstances(providerRegisterName);
              for (Instance ins : instanceList){
                  ServerAddress addr = new ServerAddress();
                  addr.setIp(ins.getIp());
                  addr.setPort(ins.getPort());
                  serverList.add(addr);
              }
              updateConnection();
          } catch (NacosException e) {
              logger.info(String.format("Nacos 获取全部实例异常 %s",e));
          }
      }
  
      private void updateConnection(){
          connectionManager.updateConnection(serverList);
      }
  }
  
  ```
  
  ServerAddress是自定义类，结构比较简单，就是IP和端口。
  
  ```
  public class ServerAddress {
  
      @Getter
      @Setter
      private String ip;
  
      @Getter
      @Setter
      private Integer port;
      
  }
  
  ```
  
  - 因为要实现简单的负载均衡，所以在客户端发送请求的时候需要从所有的Channel中选出一个来进行通信。下面的ConnectionManager的chooseOneAvailableChannnel就是这个作用。
  
```
@Component
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private AtomicInteger channelIndex = new AtomicInteger(0);

    private CopyOnWriteArrayList<Channel> channelList = new CopyOnWriteArrayList<>();

    private Map<SocketChannel,Channel> channelMap = new ConcurrentHashMap<>();

    @Autowired
    private NettyClient client;

    /**
     * 选择一个可用Channel
     * */
    public Channel chooseOneVariableChannel(){
        if(channelList.size()>0){
            int size = channelList.size();
            //轮询算法
            int index = (channelIndex.getAndAdd(1)+size)%size;
            return channelList.get(index);
        }else{
            return null;
        }
    }

    /**
     * 更新连接列表
     * */
    public synchronized void updateConnection(List<ServerAddress> serverList)
    {
        if (serverList==null||serverList.size()==0){
            logger.info("没有可用的服务");
            for (Channel ch : channelList){
                SocketAddress remoteServerAddr = ch.remoteAddress();
                Channel channel = channelMap.get(remoteServerAddr);
                channel.close();
            }
            channelMap.clear();
            channelList.clear();
            return;
        }
        //去重
        HashSet<SocketAddress> serverNodeList = new HashSet<>();
        for(ServerAddress sa : serverList){
            serverNodeList.add(new InetSocketAddress(sa.getIp(),sa.getPort()));
        }

        for (SocketAddress addr : serverNodeList){
            Channel channel = channelMap.get(addr);
            if(channel!=null&&channel.isOpen()){
                logger.info("服务{}已经存在，不需要重新连接",addr);
            }
            //Channel没打开的情况下，重新连接
            connectToServer(addr);
        }
        //移除无效节点
        for (Channel ch : channelList){
            SocketAddress addr = ch.remoteAddress();
            if(!serverNodeList.contains(addr)){
                logger.info("服务{}无效，自动移除",addr);
                Channel channel = channelMap.get(addr);
                if (channel!=null){
                    channel.close();
                }
                channelList.remove(channel);
                channelMap.remove(addr);
            }
        }
    }
    /**
     * 连接到服务器
     * */
    private void connectToServer(SocketAddress addr){
        try {
            Channel channel = client.connect(addr);
            channelList.add(channel);
            logger.info("成功连接到服务器{}",addr);
        }catch (Exception e){
            logger.info("未能连接到服务器{}",addr);
        }
    }
    /**
     * 移除连接
     * */
    public void removeConnection(Channel channel){
        logger.info("Channel:{}已经被移除",channel.remoteAddress());
        SocketAddress address = channel.remoteAddress();
        channelList.remove(channel);
        channelMap.remove(address);
    }
}

```

#### 解码编码

  这里采用的自定义的消息格式是json类型，客户端发送一个请求到服务端，也就是将RpcRequest对象转为json对象，然后经netty到达客户端，服务端做出反应之后，将RpcResponse对象以同样的方式发送到客户端。这里用的是Springboot自带的Jackson。但这里值得注意的是，这两个端的编码解码器并不是相同的实现。		

- 客户端解码和编码

```
//解码
public class JsonDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonDecoder.class);

    public JsonDecoder(){
        super(65535,0,4,0,4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int data_len = decode.readableBytes();
        byte[] bytes = new byte[data_len];
        decode.readBytes(bytes);
        logger.info("JsonDecoder 解码:{}",new String(bytes));
        Object ret = JsonUtil.parseToObject(bytes,RpcResponse.class);
        return ret;
    }
}
//编码
public class JsonEncoder extends MessageToMessageEncoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer();
        byte[] bytes = JsonUtil.parseToJsonBytes(o);
        logger.info("JsonEncoder 编码:{}",new String(bytes));
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        list.add(buf);
    }
}

```



- 服务端解码和编码

```
//解码
public class JsonDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonDecoder.class);

    public JsonDecoder(){
        super(65535,0,4,0,4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int data_len = decode.readableBytes();
        byte[] bytes = new byte[data_len];
        decode.readBytes(bytes);
        Object ret = JsonUtil.parseToObject(bytes,RpcRequest.class);
        logger.info("JsonDecoder 解码 : {}",new String(bytes));
        return ret;
    }
}
//编码
public class JsonEncoder extends MessageToMessageEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer();
        byte[] bytes = JsonUtil.parseToJsonBytes(o);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        list.add(buf);
    }
}
```

#### 心跳机制

 服务端需要对客户端进行心跳检测，约定好一定的心跳上报时间，如果客户端在这个时间内没有上报心跳，那么服务端将与此客户端之间的Channel关闭。这里因为是用的netty做的通信，所以心跳检测起来并不麻烦。只要在ChannelPipeline里面加入一个IdleStateHandler就行了。		

- 添加IdleStateHandler进行心跳配置

```
@Component
@Slf4j
public class NettyClient {

    private EventLoopGroup eventExecutors = new NioEventLoopGroup();

    private Bootstrap bootstrap = new Bootstrap();

    @Value("${rpc.heartbeat}")
    private int heartBeatTime;

    @Autowired
    private NettyClientHandler handler;

    @Autowired
    private ConnectionManager manager;

    public NettyClient(){
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,0,heartBeatTime))
                                .addLast(new JsonEncoder())
                                .addLast(new JsonDecoder())
                                .addLast("handler",handler);
                    }
                });

    }
	.....
}
```

同时还有一个需要注意的地方就是实际上这个项目中的请求，也就是RpcRequest是分为两种类别的，一种是心跳，一种是Rpc请求，所以上报心跳的时候需要设置RpcRequest对象的种类。本项目中使用一个枚举类来对这两种请求进行分类。

```
public enum RequestTypeEnum {
    /**
     * 正常请求
     * */
    NORMAL,
    /**
     * 心跳请求
     * */
    HEART_BEAT
}
```

- 上报心跳

  Netty中连接关闭，开启，读写这些都是基于事件驱动的，所以这里上报心跳需要实现ChannelInboundHandlerAdapter，至于这里头的netty的出站入站细节就不多说了，心跳的上报只需要重写userEventTriggered就行。

```
    /**
     * 心跳上报
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object event){

        try {
            if (event instanceof IdleStateEvent){
                IdleStateEvent evt = (IdleStateEvent)event;
                if (evt.state()==IdleState.ALL_IDLE){
                    RpcRequest request = new RpcRequest();
                    request.setId(IDUtil.getRpcRequestId());
                    request.setType(RequestTypeEnum.HEART_BEAT);
                    request.setMethodName("heartBeat");
                    ctx.channel().writeAndFlush(request);
                    logger.info("客户端{} s发送心跳",heartBeatTime);
                }
            }else{
                super.userEventTriggered(ctx,event);
            }
        }catch (Exception e){
            logger.error("心跳异常 %s ",e);
        }
    }
```

- 接收心跳

```
    /**
     * 检查心跳
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object event) throws Exception{
        if(event instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent)event;
            if(idleStateEvent.state()==IdleState.ALL_IDLE){
                logger.info("客户端{}心跳未上报,连接关闭",ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        }else{
            super.userEventTriggered(ctx,event);
        }
    }
```

#### 反射，动态代理

为啥要用到反射呢，因为服务端从客户端发来的请求中可以得到请求的接口，方法，方法的参数类型和参数值，那么问题来了，这些东西知道了，怎么才可以调用呢，其实用笨办法，穷举不就完事了，写上若干个case，但是这样终究是不优雅的，而且一旦项目大了，接口多了，穷举自然就不行了。那么这个时候反射就派上用场了。服务端在接收到请求的时候，我们可以通过反射拿到请求对应的服务端接口的类，方法，这样我们就可以利用反射拿到一个Method对象，通过Method对象的invoke方法就可以实现对这个接口的这个方法的调用呢。

```
    /**
     * 处理请求
     * */
    private Object handleRequest(RpcRequest req) throws Exception{
        String className = req.getClassName();
        Object serverInstance = serviceDictionary.get(className);

        if(serverInstance!=null){
            Class<?> serviceClass = serverInstance.getClass();
            String methodName = req.getMethodName();
            Class<?>[] paramTypes = req.getParamTypes();
            Object[] params = req.getParams();
            //获取方法
            Method method = serviceClass.getMethod(methodName,paramTypes);
            method.setAccessible(true);
            return method.invoke(serverInstance,getParamValues(paramTypes,params));
        }else{
            logger.info("没有找到实例:{},方法名:{}",className,req.getMethodName());
            throw new Exception("没有找到合适的RPC实例");
        }
    }
        /**
     * 获取方法参数的值
     * */
    public Object[] getParamValues(Class<?>[] paramTypes,Object[] params){
        if(params==null||paramTypes==null){
            return params;
        }
        Object[] retValues = new Object[params.length];

        for(int i = 0;i < params.length;i++){
            retValues[i] = JsonUtil.parseToObject(String.valueOf(params[i]),paramTypes[i]);
        }
        return retValues;
    }
```

那客户端为啥又要用动态代理呢，首先需要明确一点，客户端没有我们请求的接口的实现，也就是说，我们客户端只申明了一个接口罢了，真正的实现在客户端，比如这里我有一个UserService的接口，里面有一个getUserById的方法。

```
public interface UserService {
    User getUserById(Integer userId);
}
```

这里我们用一般写web的方式来进行调用，也就是需要在Controller层来调用。比如：

```
@Controller
public class RpcController {

    private static final Logger logger = LoggerFactory.getLogger(RpcController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/getUserById")
    @ResponseBody
    public String getUserById(@RequestParam("id")Integer id){
        User user = userService.getUserById(id);
        String jsonStr = JsonUtil.parseToJsonStr(user);
        logger.info("getUserById {}",jsonStr);
        return jsonStr;
    }
}
```

我们知道，如果这个UserService没有实现类，这个地方肯定是跑不起来的，但是这里我们又必须这样子写啊，因为需要做到RPC中的“就像调用本地方法一样”啊，那怎么办呢，不要忘了这里有Spring，Spring里面不止只有@Autowired这种来初始化一个bean，不然人家MyBatis为啥只要定义一个接口加一个@Mapper的注解，也不用写代码就能实现对数据库增删改查呢，我们这里就借鉴Mybatis这种思想来对客户端的接口进行实例化，但注意不是进行实现，实现怎么搞呢，当然就是用动态代理啦，在动态代理内部发送我们的请求，然后拿到返回。不就是相当于，我们在客户端，像调用本地方法一样，调用了服务端的方法么。废话不多说，看代码吧。		

- 包扫描

Spring要去初始化bean，需要扫描接口路径下的接口，对某一个路径下的类进行扫描，Spring也提供了一些接口。

```
public class RpcScanner extends ClassPathBeanDefinitionScanner {

    private RpcFactoryBean<?> rpcFactoryBean = new RpcFactoryBean<>();

    private static final Logger logger = LoggerFactory.getLogger(RpcScanner.class);
    /**
     * 注解类
     * */
    @Setter
    private Class<? extends Annotation> annotationClass;

    public RpcScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... packages){
        Set<BeanDefinitionHolder> beanDefineHolders = super.doScan(packages);

        if(beanDefineHolders.isEmpty()){
            logger.warn("No proper Rpc mapper found in such paths : {}",Arrays.asList(packages));
        }else{
            postProcessBeanDefinitions(beanDefineHolders);
        }
        return beanDefineHolders;
    }

    /**
     * 注册过滤器
     * */
    public void registerFilters(){
        boolean acceptAllInterfaces = true;
        //如果事先设置了注解类，那么就只对这个注解类不设置过滤
        if(this.annotationClass!=null){
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }
        //没有设置注解类，那么默认将basePackage下的类都进行扫描
        if(acceptAllInterfaces){
            addIncludeFilter(new TypeFilter() {
                @Override
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                    return true;
                }
            });
        }
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    /**
     * 配置自定义BeanDefinition的属性
     */
    private void postProcessBeanDefinitions(Set<BeanDefinitionHolder> holders){
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : holders){
            definition = (GenericBeanDefinition)holder.getBeanDefinition();
            //添加FactoryBean带参构造函数的参数值
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
            definition.setBeanClass(this.rpcFactoryBean.getClass());
            //设置注入模式
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            logger.info("BeanDefinitionHolder:{}",holder);
        }

    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition definition){
        return definition.getMetadata().isInterface()&&definition.getMetadata().isIndependent();
    }
}

```

这里的doScan的方法，就是在扫描某个路径的时候调用的，在扫描之后，需要对BeanDefinition进行一下设置，设置构造方法传入的参数，这里需要在对应的FactoryBean里添加对应的构造方法，设置BeanClass和注入模式，这里要讲还可以讲一大堆，建议看不懂的可以了解一下FactoryBean的原理和用法。		

```
public class RpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    private static final Logger logger = LoggerFactory.getLogger(RpcFactoryBean.class);

    @Autowired
    private RpcFactory<T> factory;

    public RpcFactoryBean(){
    }

    public RpcFactoryBean(Class<T> rpcInterface){
        this.rpcInterface = rpcInterface;
    }

    /**
     * 返回对象实例
     * */
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(),new Class[]{rpcInterface},factory);
    }

    /**
     * Bean的类型
     * */
    @Override
    public Class<?> getObjectType() {
        return this.rpcInterface;
    }

    /**
     * 是否是单例的
     * */
    @Override
    public boolean isSingleton(){
        return true;
    }
 }
```

最后就是客户端调用服务端的本质所在了，了解过动态代理的人肯定知道InvocationHandler这个接口。

```
@Component
public class RpcFactory<T> implements InvocationHandler {

    @Autowired
    private NettyClient client;

    private static final Logger logger = LoggerFactory.getLogger(RpcFactory.class);
    /**
     * 发送请求的地方
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setType(RequestTypeEnum.NORMAL);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setParamTypes(method.getParameterTypes());
        request.setId(IDUtil.getRpcRequestId());

        //向服务端发送请求
        Object result = client.send(request);

        Class<?> returnType = method.getReturnType();

        ObjectMapper mapper = new ObjectMapper();

        RpcResponse response = mapper.readValue(String.valueOf(result),RpcResponse.class);

        if(response.getCode()==RespCodeEnum.ERROR){
            throw new Exception(response.getErrorMsg());
        }
        String respData = mapper.writeValueAsString(response.getData());
        if(returnType.isPrimitive()||String.class.isAssignableFrom(returnType)){
            return respData;
        }else if(Collection.class.isAssignableFrom(returnType)){
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(Collection.class,Object.class);
            return mapper.readValue(respData,collectionType);
        }else if (Map.class.isAssignableFrom(returnType)){
            MapType mapType = mapper.getTypeFactory().constructMapType(Map.class,Object.class,Object.class);
            return mapper.readValue(respData,mapType);
        }else{
            Object data = response.getData();
            return mapper.readValue(respData,returnType);
        }
    }
}
```

这里应该要对Response的返回值做不同类型的校验和处理，篇幅有限，我之后再加上吧。另外上面还有一个问题，就是RpcFactoryBean这个类中的@Autowired注解，究竟有没有作用，这段代码我是参考了网上的，具体请看：

[RPC基本原理以及如何用Netty来实现RPC](https://www.jianshu.com/p/8876c9f3cd7f)

后来我经过一番查阅和自己debug，这个是起到了作用的，虽然这个类里没有@Component的注解，并且初始化是new出来的，不会交给spring来管理，那么这个类是从哪里初始化的呢，答案其实还是加载顺序的问题，RpcFactory这个类由于加了@Component注解，所以这个bean在Spring容器中是存在的，而并且是**单例**的，RpcFactoryBean是在什么时候加载的呢，是在Controller层中的对UserService进行注入的时候，这个时候RpcFactory已经有实例了，并且由于是单例，那自然用@Autowired获取到的是同一个实例。				

试验一下看效果，还是以Web请求来触发RPC请求，请求比较简单，就是根据一个用户id获取用户信息。（http://localhost:6001/getUserById?id=1）		

![avatar](https://media.izzer.cn/RPC请求.jpg)

RPC客户端的请求日志

![avatar](https://media.izzer.cn/RPC%E5%AE%A2%E6%88%B7%E7%AB%AF%E8%AF%B7%E6%B1%82.jpg)

RPC服务端的日志

![avatar](https://media.izzer.cn/RPC服务端处理.jpg)

### 总结

虽然这个RPC功能很简陋，但是实现花了我挺多时间，主要还是在查资料上，因为Spring涉及到源码层面和FactoryBean这些没了解过。但是还好磕磕绊绊也实现了出来。但说到底还是有很多缺陷，比如负载均衡算法还可以扩展几个，比如限流操作，熔断操作，还有功能上，这里我默认服务端和客户端两个的包名是在一样的，但是现实中是可能不一样的，这样就会出现找不到实现类的出错的情况，不过这也让我有动力去重写这个demo了，最后我把我的代码实现传到了我的[GitHub](https://github.com/Yintianhao/SimpleRpc)，工作之余我会尽力去完善的。