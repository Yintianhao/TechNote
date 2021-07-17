### 分布式限流

上一篇博客中提到的单个应用的限流存在局限性，那就是只能对单个实例进行限流，而面对现在大型的应用，这种限流肯定是不够的，那么就要进行对整个分布式系统。对于分布式限流，我从个人的理解上有两个途径，第一种就是在应用之外，也就是比如用Nginx来做限流，第二种就是系统自己利用第三方的中间件来自己实现限流。这里了解一下第二种途径吧，利用Redis来做限流。

### 算法

之前有提过几个限流算法，分别是计数器，漏桶，滑动窗口，令牌桶。这里采用令牌桶算法来实现。具体怎么实现呢。

### 初始化令牌桶

令牌桶算法需要初始化桶的最大能够盛放的令牌个数，以及匀速放入令牌桶的令牌的放入速率。这里初始化令牌桶的最大盛放令牌个数为100个，放入速率为5，如下图，limitkey1是统一的key。

![avatar](D:\VsCodePro\TechNote\src\Image\初始化令牌桶.jpg)

### 时间的问题

之前的分布式锁的文章就有提过，多个系统请求的时候，他们之间的时间可能是不一致的，当然那篇文章中的时间和此处的时间并无关联。主要是这里需要进行取令牌和放令牌的操作，需要获取最后一次放入令牌的时间。所以这里统一采用redis中的time命令来获取统一的时间。lua脚本如下：

```
local times = redis.pcall("TIME")
return tonumber(times[1])*1000+tonumber(times[2])/1000
```

### 获取令牌

获取令牌可以结合令牌桶算法画一个简单流程图。

![avatar](D:\VsCodePro\TechNote\src\Image\令牌桶算法流程图.jpg)

对应的lua脚本如下

```
local local_key =  KEYS[1]
local permits = ARGV[1]
local curr_mill_second = ARGV[2]
if tonumber(redis.pcall("EXISTS", local_key)) < 1 then
   return 0
end

--- 令牌桶内数据：
---             last_mill_second  最后一次放入令牌时间
---             curr_permits  当前桶内令牌
---             max_permits   桶内令牌最大数量
---             rate  令牌放置速度
local rate_limit_info = redis.pcall("HMGET", local_key, "last_mill_second", "curr_permits", "max_permits", "rate")
local last_mill_second = rate_limit_info[1]
local curr_permits = tonumber(rate_limit_info[2])
local max_permits = tonumber(rate_limit_info[3])
local rate = rate_limit_info[4]
--- 标识没有配置令牌桶
if type(max_permits) == 'boolean' or max_permits == nil then
   return 0
end
--- 若令牌桶参数没有配置，则返回0
if type(rate) == 'boolean' or rate == nil then
   return 0
end

local local_curr_permits = max_permits;

--- 令牌桶刚刚创建，上一次获取令牌的毫秒数为空
--- 根据和上一次向桶里添加令牌的时间和当前时间差，触发式往桶里添加令牌，并且更新上一次向桶里添加令牌的时间
--- 如果向桶里添加的令牌数不足一个，则不更新上一次向桶里添加令牌的时间
--- ~=号在Lua脚本的含义就是不等于!=
if (type(last_mill_second) ~= 'boolean'  and last_mill_second ~= nil) then
    if(curr_mill_second - last_mill_second < 0) then
       return -1
    end

    --- 生成令牌操作
    local reverse_permits = math.floor(((curr_mill_second - last_mill_second) / 1000) * rate) --- 最关键代码：根据时间差计算令牌数量并匀速的放入令牌
    local expect_curr_permits = reverse_permits + curr_permits;
    local_curr_permits = math.min(expect_curr_permits, max_permits);  --- 如果期望令牌数大于桶容量，则设为桶容量
    --- 大于0表示这段时间产生令牌，则更新最新令牌放入时间
    if (reverse_permits > 0) then
        redis.pcall("HSET", local_key, "last_mill_second", curr_mill_second)
    end
else
    redis.pcall("HSET", local_key, "last_mill_second", curr_mill_second)
end
--- 取出令牌操作
    local result = -1
    if (local_curr_permits - permits >= 0) then
        result = 1
        redis.pcall("HSET", local_key, "curr_permits", local_curr_permits - permits)
    else
        redis.pcall("HSET", local_key, "curr_permits", local_curr_permits)
    end
return result

```

### 调用

- 创建注解，在接口对传入参数进行初始化

```
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FlowLimit {
    /**
     * 请求令牌数
     * */
    int permit() default 1;

    /**
     * 请求的key
     * */
    String key();
}

```

- 创建一个切面，进行取令牌操作

切面编程我也是第一次用，有些写的不太好的地方还请见谅。有了前面的脚本，事实上代码就不怎么复杂了，主要就是加载两次lua脚本，然后完成相应的逻辑就好了。

```
@Aspect
@Configuration
public class RedisLimitInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RedisLimitInterceptor.class);


    private final RedisTemplate<String,Serializable> redisTemplate;


    @Autowired
    public RedisLimitInterceptor(RedisTemplate<String,Serializable> template){
        this.redisTemplate = template;
    }

    @Around("execution(public * *(..))&&@annotation(cn.izzer.flow_limit_redis.anonation.FlowLimit)")
    public Object interceptor(ProceedingJoinPoint point){

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        FlowLimit annotation = method.getAnnotation(FlowLimit.class);
        int permit = annotation.permit();

        ImmutableList<String> keys = ImmutableList.of(annotation.key());
        logger.info(String.format("欲取出的令牌数:%d key:%s ",permit,keys.get(0)));

        logger.info("开始尝试取出令牌...");

        try {
            //先获取系统时间
            logger.info("尝试获取当前时间...");
            DefaultRedisScript<Number> redisScript = new DefaultRedisScript<Number>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/redis_currentTimeMillis")));
            redisScript.setResultType(Number.class);
            Number currentTimeMillies = redisTemplate.execute(redisScript,keys);
            logger.info(String.format("当前时间:%d",currentTimeMillies));

            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/redis_acquire")));
            //请求令牌
            Number reqResult = redisTemplate.execute(redisScript,keys,annotation.permit(),currentTimeMillies.intValue());

            logger.info(String.format("请求结果:%d",reqResult.intValue()));

            if(reqResult.intValue()==1){
                logger.info("请求成功");
                return point.proceed();
            }
            else{
                logger.info("限流了");
                throw new RuntimeException("超限限流");
            }

        }catch (Throwable e){
            if(e instanceof RuntimeException){
                e.printStackTrace();
                throw new RuntimeException(e.getLocalizedMessage());
            }
            throw new RuntimeException("服务器错误");
        }
    }

}

```

- Controller层进行调用

```
@RestController
public class RedisLimitController {

    private static final Logger logger = LoggerFactory.getLogger(RedisLimitController.class);

    @FlowLimit(permit = 2,key = "limitkey1")
    @GetMapping("/limit")
    public String limit(){
        return "ok";
    }

}
```

- 测试环节

老样子，用JMeter进行并发调用测试。用十个线程循环十次去请求接口，每次取出两个令牌。

![avatar](D:\VsCodePro\TechNote\src\Image\测试分布式限流线程组设置.jpg)

查看日志情况，可以看到一定的时候就会进行限流。
![avatar](https://media.izzer.cn/%E9%99%90%E6%B5%81%E7%BB%93%E6%9E%9C.jpg)

### 总结

这次的限流实际上是一个比较初级的版本，在生产中还是要结合业务进行设计，脚本有肯定要更加复杂，因为要面对更复杂的场景。