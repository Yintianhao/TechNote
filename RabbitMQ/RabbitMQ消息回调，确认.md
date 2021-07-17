## 发送消息回调，消息接收确认，死信队列，延迟队列

### RabbitMQ发送消息回调

主要是实现两个接口，在实现之前需要加上两个比较重要的配置。

```
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
```

然后就是实现回调方法：

```
@Configuration
public class RmqConfig {

    @Bean
    public RabbitTemplate createRmqTemplate(ConnectionFactory connectionFactory){

        RabbitTemplate template = new RabbitTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setMandatory(true);


        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("确认回调-相关数据："+correlationData);
                System.out.println("确认回调-确认情况："+b);
                System.out.println("确认回调-原因："+s);
            }
        });

        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                System.out.println("返回回调-消息："+message);
                System.out.println("返回回调-回应码："+i);
                System.out.println("返回回调-回应信息："+s);
                System.out.println("返回回调-交换机："+s1);
                System.out.println("返回回调-路由键："+s2);
            }
        });
        return template;
    }

}

```



### RabbitMQ接收消息确认

消息确认在防止消息丢失的时候起到很重要的作用，RabbitMQ支持消息确认ACK。ACK机制是消费者从RabbitMQ收到消息并且处理完成之后，反馈给RabbitMQ，RabbitMQ收到反馈后再将消息从队列中删除。消费者如果没有正常接收消息，没有进行ACK反馈，那么RMQ就不会将这个消息删除，而是认为此消息没有被正常消费，将消息重新放到队列里面。当然，如果是集群模式下，这个消息会被推送到其他消费者，ACK机制在RMQ中是默认开启的。

#### 自动确认

这也是默认的消息确认情况，RabbitMQ成功将信息发出，即成功将消息写入到TCP连接中则认为这次消息投递已经被正确处理，而不在乎消费者端是否正确处理了。所以这种模式下消费者如果报异常了等情况没有正确地处理这个消息，那么RabbitMQ也不能知道，所以这条消息就丢失了。

```
spring.rabbitmq.listener.simple.acknowledge=auto
```

#### 不确认

```
spring.rabbitmq.listener.simple.acknowledge=none
```

#### 手动确认

```
spring.rabbitmq.listener.simple.acknowledge=manual
```

主要涉及的是三个API，也就是Channel里面的basicAck，baseNack和basicReject。三个方法调用都是代表消息被正确投递，但是只有basicAck表示消息被正确处理。值得一提的是basicReject，在实际生产中难免需要将消息重回队列的场景，那么这时候需要在第二个参数传入true，表示将消息重新回到队列。而设置为false则代表这条消息消费者端已经收到但是并不想下次还收到，所以这条消息不进入队列而是直接丢掉。然后是basicNack，这个API代表不消费某条消息。三个方法的参数列表和其意义为:

```
void basicAck(deliveryTag,multiple)

deliveryTag 当前消息的唯一ID			
multiple 是否批量，即是否一次性ack所有小于当前消息deliveryTag的消息		
```



```
void basicNack(deliveryTag,multiple,requeue)

deliveryTag 当前消息的唯一ID。		
multiple 是否批量，即一次性针对当前channel的消息的tagID小于当前这条消息的，都拒绝确认。		
requeue 是指是否重新入列，也就是指不确定的消息是否重新丢回到队列里去。
```



```
void basicReject(deliveryTag,requeue)

deliveryTag 当前消息的唯一ID。			
requeue 是指是否重新入列，也就是被拒绝的消息是否重新丢回到队列里去。
```

#### 代码实现

手动确认需要加入相关配置来配置对哪些队列进行手动确认，然后添加回调。

```
//比如这里对DirectQueue，fanout.A，fanout.B，fanout.C进行手动确认
@Configuration
public class AckConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private AckReceiver ackReceiver;

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){

        SimpleMessageListenerContainer smlc = new SimpleMessageListenerContainer(connectionFactory);
        smlc.setConcurrentConsumers(1);
        smlc.setMaxConcurrentConsumers(1);
        smlc.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        smlc.addQueues(new Queue("DirectQueue",true));
        smlc.addQueues(new Queue("fanout.A",true));
        smlc.addQueues(new Queue("fanout.B",true));
        smlc.addQueues(new Queue("fanout.C",true));
        smlc.setMessageListener(ackReceiver);
        return smlc;
    }
}


```

```
//实现ChannelAwareMessageListener
@Component
public class AckReceiver implements ChannelAwareMessageListener {


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try{
            String text = message.toString();
            String[] msgArr = text.split("'");
            Map<String, String> msgMap = mapStringToMap(msgArr[1].trim(),3);
            String messageId=msgMap.get("msgId");
            String messageData=msgMap.get("msgData");
            String createTime=msgMap.get("createTime");
            System.out.println("MessageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
            System.out.println("Message from ："+message.getMessageProperties().getConsumerQueue());
            //手动确认
            channel.basicAck(deliveryTag, true);

        }catch (Exception e){
            channel.basicReject(deliveryTag,false);
            e.printStackTrace();
        }
    }

    private Map<String, String> mapStringToMap(String str,int entryNum ) {
        str = str.substring(1, str.length() - 1);
        String[] strs = str.split(",",entryNum);
        Map<String, String> map = new HashMap<String, String>();
        for (String string : strs) {
            String key = string.split("=")[0].trim();
            String value = string.split("=")[1];
            map.put(key, value);
        }
        return map;
    }

}
```

这里我采用的postman来调用生产者的发送信息的接口，代码就没啥好说的了，上一篇有讲。

然后怎么样才能看出有没有确认的效果呢？不要忘了，RMQ有自带的管理页面的插件，上面可以浏览队列的相关信息。首先将上面的配置暂时注释，然后调用生产者发送信息，打开管理页面的Queue选项。可以看到：

![avatar](http://media.izzer.cn/RMQ%E7%AE%A1%E7%90%86%E9%A1%B5%E9%9D%A2%E6%9F%A5%E7%9C%8Bnack.jpg)

这四个队列都有一条没有ack的消息，然后将之前的注释去掉使得代码生效。再查看控制台输出和管理页面的情况。

![avatar](http://media.izzer.cn/控制台打印确认信息.jpg)

![avatar](http://media.izzer.cn/RMQ管理页面acked信息.jpg)

综上就可以得出这些消息的的确确被确认收到了。

### 死信队列

#### 概念

死信即DeadLetter，是RMQ中的一种消息机制，死信消息会被RMQ特殊处理，如果配置了死信队列，那么死信消息会被扔进死信队列，如果没有，这条消息会被丢弃。

#### 死信出现条件

```
1，消息被否定确认，即上面的basicNack,basicReject，并且requeue属性设置为false，即不重新进入队列。
2，消息在队列中的存活时间超过设置TTL。
3，消息队列的消息数量已经超过了最大队列长度。
```

#### 配置

业务队列与死信交换机的绑定是在构建业务队列时，通过参数（x-dead-letter-exchange和x-dead-letter-routing-key）的形式进行指定。具体过程分为三步：

```
1，配置业务队列并绑定在业务交换机上。
2，为业务队列配置死信交换机和routingkey。
3，为死信交换机配置死信队列。
```

#### demo

死信队列一般是在业务队列的基础上进行配置的。这里设置两个业务队列(A,B)和两个死信队列(A,B)，绑定各自的交换机。同时为了显现出死信队列的作用，两个业务队列其中一个抛出异常，从而让没有被正常消费的信息进入死信队列。然后死信队列的消费者进行消费。			

以下是队列申明和配置。

```
@Configuration
public class DLConfig {

    public static final String BUSINESS_EXCHANGE_NAME = "business.exchange";
    public static final String BUSINESS_QUEUE_A_NAME = "business.queue_a";
    public static final String BUSINESS_QUEUE_B_NAME = "business.queue_b";
    public static final String DEAD_LETTER_EXCHANGE = "deadletter.exchange";
    public static final String DEAD_LETTER_QUEUEA_ROUTING_KEY = "dl_queue_a.routingkey";
    public static final String DEAD_LETTER_QUEUEB_ROUTING_KEY = "dl_queue_b.routingkey";
    public static final String DEAD_LETTER_QUEUE_A_NAME = "queue_a";
    public static final String DEAD_LETTER_QUEUE_B_NAME = "queue_b";

    // 声明业务Exchange
    @Bean("businessExchange")
    public FanoutExchange businessExchange(){
        return new FanoutExchange(BUSINESS_EXCHANGE_NAME);
    }

    // 声明死信Exchange
    @Bean("deadLetterExchange")
    public DirectExchange deadLetterExchange(){
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // 声明业务队列A
    @Bean("businessQueueA")
    public Queue businessQueueA(){
        Map<String, Object> args = new HashMap<>(2);
        //声明当前队列绑定的“死信交换机”
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信“路由key”
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEA_ROUTING_KEY);
        return QueueBuilder.durable(BUSINESS_QUEUE_A_NAME).withArguments(args).build();
    }

    // 声明业务队列B
    @Bean("businessQueueB")
    public Queue businessQueueB(){
        Map<String, Object> args = new HashMap<>(2);
        //声明当前队列绑定的“死信交换机”
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        //声明当前队列的“死信路由key”
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEB_ROUTING_KEY);
        return QueueBuilder.durable(BUSINESS_QUEUE_B_NAME).withArguments(args).build();
    }

    // 声明死信队列A
    @Bean("deadLetterQueueA")
    public Queue deadLetterQueueA(){
        return new Queue(DEAD_LETTER_QUEUE_A_NAME);
    }

    // 声明死信队列B
    @Bean("deadLetterQueueB")
    public Queue deadLetterQueueB(){
        return new Queue(DEAD_LETTER_QUEUE_B_NAME);
    }

    // 声明业务队列A绑定关系
    @Bean
    public Binding businessBindingA(@Qualifier("businessQueueA") Queue queue,
                                    @Qualifier("businessExchange") FanoutExchange exchange){
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 声明业务队列B绑定关系
    @Bean
    public Binding businessBindingB(@Qualifier("businessQueueB") Queue queue,
                                    @Qualifier("businessExchange") FanoutExchange exchange){
        return BindingBuilder.bind(queue).to(exchange);
    }

    // 声明死信队列A绑定关系
    @Bean
    public Binding deadLetterBindingA(@Qualifier("deadLetterQueueA") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEA_ROUTING_KEY);
    }

    // 声明死信队列B绑定关系
    @Bean
    public Binding deadLetterBindingB(@Qualifier("deadLetterQueueB") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEA_ROUTING_KEY);
    }

}

```

然后对业务队列中的消息进行消费。

```
@Slf4j
@Component
public class BusinessMsgReceiver {
    // 监听器处理方法（监听业务队列A——BUSINESS_QUEUE_A_NAME）
    @RabbitListener(queues = BUSINESS_QUEUE_A_NAME)
    public void receiveA(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("收到业务消息A：{}", msg);
        boolean ack = true;
        Exception exception = null;
        try {
            // 如果消息中包含“deadletter”, 则抛出异常，对接收到的消息返回“Nack”确认——让消息进入死信队列中
            if (msg.contains("deadletter")){
                throw new RuntimeException("dead letter exception");
            }
        } catch (Exception e){
            ack = false;
            exception = e;
        }
        if (!ack){
            log.error("消息消费发生异常，error msg:{}", exception.getMessage(), exception);
            // 进行非正常的“nack”确认
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } else {
            // 进行正常的“ack”确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    // 监听器处理方法（监听业务队列B——BUSINESS_QUEUE_B_NAME）
    @RabbitListener(queues = BUSINESS_QUEUE_B_NAME)
    public void receiveB(Message message, Channel channel) throws IOException {
        System.out.println("收到业务消息B：" + new String(message.getBody()));
        // 进行正常的“ack”确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

```

然后对A,B两个死信队列设置消费者。

```
@Component
public class DLMsgReceiver {

    // 监听器处理方法（监听死信队列A——DEAD_LETTER_QUEUE_A_NAME）
    @RabbitListener(queues = DEAD_LETTER_QUEUE_A_NAME)
    public void receiveA(Message message, Channel channel) throws IOException {
        System.out.println("收到死信消息A：" + new String(message.getBody()));
        // 进行正常的“ack”确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // 监听器处理方法（监听死信队列B——DEAD_LETTER_QUEUE_B_NAME）
    @RabbitListener(queues = DEAD_LETTER_QUEUE_B_NAME)
    public void receiveB(Message message, Channel channel) throws IOException {
        System.out.println("收到死信消息B：" + new String(message.getBody()));
        // 进行正常的“ack”确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

```

生产消息。

```
    @GetMapping("/sendBusinessMsg")
    public String sendDirectMessage4(){
   //消息包含deadletter字样从而产生异常     rabbitTemplate.convertSendAndReceive(BUSINESS_EXCHANGE_NAME, "", "deadletter");
        return "send ok";
    }
```

这里在业务队列A中抛出了异常，所以A中的消息会进入死信交换机，这里提一嘴死信队列在RMQ中的模式图：

![avatar](http://media.izzer.cn/死信队列模式图.jpg)

A中的消息在进入死信交换机后，被路由到相应的死信队列，上面两个死信队列是绑定在一个死信交换机上的，所以两个死信队列里都有消息，所以这两个队列的消费者也会有相应的输出。

![avatar](http://media.izzer.cn/死信队列demo控制台输出.jpg)

### 延迟队列

物如其名，延迟队列从字面意思来理解就是消息延迟某个时间再处理，一般用RMQ来实现延迟队列可以使用上面所提到的死信队列，那么延迟怎么处理呢，那自然是消息的TTL。过期的消息被放到死信队列里，然后再由专门的消费者消费死信队列中的消息，就可以实现延迟队列了。说干就干，且不说这个方案有啥方案吧，先来按照上面思路实现以下延迟队列。			

1，申明队列，交换机等信息。一共有AB两个队列，为了比较出效果，两个队列的TTL是不同的。

````
@Configuration
public class DelayQueueConfig {

    public static final String DELAY_QUEUE_EXCHANGE_NAME = "Delay.queue.business.exchange";

    public static final String DELAY_QUEUE_NAMEA = "Delay.queue.business.queue.A";

    public static final String DELAY_QUEUE_NAMEB = "Delay.queue.business.queue.B";

    public static final String DELAY_QUEUE_KEY_A = "Delay.queue.business.queue.a.key";

    public static final String DELAY_QUEUE_KEY_B = "Delay.queue.business.queue.b.key";

    public static final String DEAD_LETTER_EXCHANGE = "Delay.queue.deadletter.exchange";

    public static final String DEAD_LETTER_QUEUEA_KEY = "Delay.queue.deadletter.a.key";

    public static final String DEAD_LETTER_QUEUEB_KEY = "Delay.queue.deadletter.b.key";

    public static final String DEAD_LETTER_QUEUEA_NAME = "Delay.queue.deadletter.queue.A";

    public static final String DEAD_LETTER_QUEUEB_NAME = "Delay.queue.deadletter.queue.B";

    // 声明延时Exchange
    @Bean("delayExchange")
    public DirectExchange delayExchange(){
        return new DirectExchange(DELAY_QUEUE_EXCHANGE_NAME);
    }

    // 声明死信Exchange
    @Bean("deadLetterExchange")
    public DirectExchange deadLetterExchange(){
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // 声明延时队列A 延时10s
    // 并绑定到对应的死信交换机
    @Bean("delayQueueA")
    public Queue delayQueueA(){
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEA_KEY);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 6000);
        return QueueBuilder.durable(DELAY_QUEUE_NAMEA).withArguments(args).build();
    }

    // 声明延时队列B 延时 60s
    // 并绑定到对应的死信交换机
    @Bean("delayQueueB")
    public Queue delayQueueB(){
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEB_KEY);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 60000);
        return QueueBuilder.durable(DELAY_QUEUE_NAMEB).withArguments(args).build();
    }

    // 声明死信队列A 用于接收延时10s处理的消息
    @Bean("deadLetterQueueA")
    public Queue deadLetterQueueA(){
        return new Queue(DEAD_LETTER_QUEUEA_NAME);
    }

    // 声明死信队列B 用于接收延时60s处理的消息
    @Bean("deadLetterQueueB")
    public Queue deadLetterQueueB(){
        return new Queue(DEAD_LETTER_QUEUEB_NAME);
    }

    // 声明延时队列A绑定关系
    @Bean
    public Binding delayBindingA(@Qualifier("delayQueueA") Queue queue,
                                 @Qualifier("delayExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_QUEUE_KEY_A);
    }

    // 声明业务队列B绑定关系
    @Bean
    public Binding delayBindingB(@Qualifier("delayQueueB") Queue queue,
                                 @Qualifier("delayExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_QUEUE_KEY_B);
    }

    // 声明死信队列A绑定关系
    @Bean
    public Binding deadLetterBindingA(@Qualifier("deadLetterQueueA") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEA_KEY);
    }

    // 声明死信队列B绑定关系
    @Bean
    public Binding deadLetterBindingB(@Qualifier("deadLetterQueueB") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEB_KEY);
    }
}

````

2，申明消费者，这没什么太多要说的，解析出消息打印然后手动ACK。

```
    @RabbitListener(queues = DEAD_LETTER_QUEUEA_NAME)
    public void receiveA(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("当前时间：{},死信队列A收到消息：{}", new Date().toString(), msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = DEAD_LETTER_QUEUEB_NAME)
    public void receiveB(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("当前时间：{},死信队列B收到消息：{}", new Date().toString(), msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
```

3，生产者生产消息，代码没啥好说的就不贴了。发两条消息分别到AB两个TTL不同的队列中，同时查看控制台收消息情况：

![avatar](http://media.izzer.cn/延迟队列收到信息.jpg)

4,虽然很愉快地实现了，但是仔细一想这个方案还是会有问题，那就是每增加一种新的TTL不同的业务场景，就需要新增一个队列，对于TTL很多的情况，我们就需要创建很多很多队列，这肯定是不行的。

### 延迟队列 V2.0

既然问题处在队列上，那么是否可以将思路转换到消息本身上呢，答案自然是可以，不要忘记消息本身也有TTL属性，先不考虑这个方案的好坏，先试着实现一下，我们增加一个不带有TTL属性的队列。这个队列用来存放各种不同TTL的消息，这样不就可以达到我们的要求了么。

1，增加队列C。

```

    public static final String DELAY_QUEUEC_NAME = "Delay.queue.business.queuec";
    public static final String DELAY_QUEUEC_KEY = "Delay.queue.business.queuec.routingkey";

    public static final String DEAD_LETTER_QUEUEC__KEY = "Delay.queue.deadletter.c.key";
    public static final String DEAD_LETTER_QUEUEC_NAME = "Delay.queue.deadletter.queue.C";

// 声明延时队列C 不设置TTL
    // 并绑定到对应的死信交换机
    @Bean("delayQueueC")
    public Queue delayQueueC(){
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEC__KEY);
        return QueueBuilder.durable(DELAY_QUEUEC_NAME).withArguments(args).build();
    }

    // 声明死信队列C 用于接收延时任意时长处理的消息
    @Bean("deadLetterQueueC")
    public Queue deadLetterQueueC(){
        return new Queue(DEAD_LETTER_QUEUEC_NAME);
    }

    // 声明延时列C绑定关系
    @Bean
    public Binding delayBindingC(@Qualifier("delayQueueC") Queue queue,
                                 @Qualifier("delayExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_QUEUEC_KEY);
    }

    // 声明死信队列C绑定关系
    @Bean
    public Binding deadLetterBindingC(@Qualifier("deadLetterQueueC") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEC__KEY);
    }
```

2，生产者需要在发送的时候来对消息的属性进行设置，先后发送两条信息，一条过期时间为6S，一条为60S，然后观察死信消费者的输出情况。			

设置消息的过期时间。

```
    @GetMapping("/sendDelayMsg")
    public String sendDelayMsg(){
        String msgA = "hello world,the expire is 6";
        String msgB = "hello world,the expire is 60";
        log.info("当前时间：{},发送，msg:{}", new Date(), msgA);
        //TTL队列 (queue的扩展参数)
        rabbitTemplate.convertAndSend(DelayQueueConfig.DELAY_QUEUE_EXCHANGE_NAME,DelayQueueConfig.DELAY_QUEUEC_KEY,msgA,msg->{
            msg.getMessageProperties().setExpiration(String.valueOf(6000));
            return msg;
        });
        rabbitTemplate.convertAndSend(DelayQueueConfig.DELAY_QUEUE_EXCHANGE_NAME,DelayQueueConfig.DELAY_QUEUEC_KEY,msgB,msg->{
            msg.getMessageProperties().setExpiration(String.valueOf(60000));
            return msg;
        });
        return "ok";
    }
```



![avatar](http://media.izzer.cn/死信消费者输出1.jpg)

3，那么上面就算完成了？并不是，再试一下将过期时间长的放在前面，过期时间短的放在后面。再观察消费者这边的情况。

![avatar](http://media.izzer.cn/死信消费者输出2.jpg)

可以看到尽管后面的消息过期时间比前面的消息短，但是仍然没有及时地被处理，而是等到前面的消息过期才被处理。这是因为消息队列检查消息过期的时候不是对整个队列中的消息进行检查，而是对队首的消息进行检查，所以在第二种情况下，后面的消息尽管已经过期，但还是需要等到前面过期时间长的消息被处理之后才能得到处理。所以使用这种方案实现延迟队列也有一定的弊端。

### 延迟队列V3.0 

在比较前面两种方案之后现在来尝试第三种方案，那就是用RabbitMQ自己的延迟队列插件。

1，还是继续申明相关的队列信息。

```
@Configuration
public class RmqDelayQueueConfig {
    public static final String DELAY_QUEUE_NAME = "Delay.queue.name.demo";

    public static final String DELAY_EXCHANGE_NAME = "Delay.queue.exchange.demo";

    public static final String DELAY_QUEUE_KEY = "Delay.queue.key.demo";

    @Bean
    public Queue delayQueue(){
        return new Queue(DELAY_QUEUE_NAME);
    }

    @Bean
    public CustomExchange customExchange(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-delayed-type","direct");
        return new CustomExchange(DELAY_EXCHANGE_NAME,"x-delayed-message",true,false,args);
    }

    @Bean
    public Binding bindingNotify(@Qualifier("delayQueue")Queue queue,
                                 @Qualifier("customExchange")CustomExchange customExchange){
        return BindingBuilder.bind(delayQueue()).to(customExchange()).with(DELAY_QUEUE_KEY).noargs();
    }
}

```

2,发送消息，这里需要设置MessageProperties中的属性，注意区分setDelay()和setExpiration()。

```
@Component
public class DelayMsgSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    public void sendMsg(String msg,int expire){
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE_NAME, DELAY_QUEUE_KEY, msg, item ->{
            item.getMessageProperties().setExpiration(String.valueOf(expire));
            return item;
        });
    }
}

```

3，消费者还是和之前一样，但是这里只测试V2.0中的第二种情况，看看过期时间短的信息放在后面是否能被队列提前发现并处理。		

写一个简单的http接口来发送消息。

```
@Slf4j
@RestController
public class RmqDelayController {

    @Autowired
    private DelayMsgSender sender;
    @GetMapping("/sendDelayMsg/{msg}/{time}")
    public void send(@PathVariable("msg") String msg, @PathVariable("time")Integer delayedTime){
        log.info("当前时间：{},收到请求，msg:{},delayTime:{}", new Date(), msg, delayedTime);
        sender.sendMsg(msg, delayedTime);
    }
}
```

用postman测试一下，发送两个这样的请求。

```
http://localhost:9001/sendDelayMsg/helloworld,the expire is 20s/20000
http://localhost:9001/sendDelayMsg/helloworld,the expire is 2s/2000
```

![avatar](http://media.izzer.cn/rmq自带延迟队列输出.jpg)

这时候就可以看见这是我们需要的结果。

#### 延迟队列的一点小总结

延迟队列在需要延迟处理的场景中是非常有用的，使用rmq的延迟队列也有很好的rmq的支持，并且由于rmq支持集群模式，可以很好的实现高可用。但是rmq的延迟队列不是实现延迟处理的唯一解决办法，使用Java的延迟队列，甚至是Redis都可以实现延迟队列，各有各的利弊，对于方案的选用也需要综合业务来进行考虑，希望在日后我能对这个有更多的了解吧。