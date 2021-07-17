## Redis实现分布式锁

### 分布式锁

为了防止分布式系统中的多个线程之间相互干扰，需要一种分布式协调技术来对这些进程进行调度，这个技术的核心就是分布式锁。比如在如下场景中，就需要用到分布式锁，现有某个服务有ABC三个实例，部署在三台服务器上，成员变量var在三个实例中都存在，此时三个请求经过nginx同时对var操作，显然结果不是对的，而倘若不同时对A进行操作，而A是不共享的，也不具有可见性，所以处理的结果也是不对的。这时候就需要分布式锁了。

### 分布式锁的条件

- 分布式环境下，一个方法同一时间只能有一个机器的线程执行。
- 高可用地获取和释放
- 高性能地获取和释放
- 可重入
- 具有锁失效机制，防止死锁
- 具有非阻塞锁特性，即没有获取到锁将直接返回获取锁失败。

### 用Redis实现分布式锁

- 加锁

加锁过程简单拆分为两步，第一步是检查key是否存在，也就是检查目前有没有别的客户端已经上锁了，如果有人上锁了，那就直接返回失败。第二步就是如果没有上锁，也就是key不存在，那么就设置key，那么就设置一个过期时间，之所以设置过期时间，是因为万一客户端发生意外没有来解锁，redis也可以自己来解，代码比较简单。

 ```
   /**
     * 尝试获取分布式锁
     * @param lockKey 键
     * @param reqId 值，此处设置为requestID可以在解锁的时候有依据知道是哪个请求加的锁
     * @param expire 过期时间
     * */
    public static boolean tryGetLock(Jedis jedis,String lockKey,String reqId,int expire){
        //SET_IF_NOT_EXIST 不存在的时候设置，存在的时候不操作
        //SET_WITH_EXPIRE_TIME 设置过期时间
        String result = jedis.set(lockKey,
                reqId,
                SET_IF_NOT_EXIST,
                SET_WITH_EXPIRE_TIME,
                expire);
        if(LOCK_SUCCESS.equals(result)){
            return true;
        }
        return false;
    }
 ```

那么问题来了，如果将这两个步骤分开可以不呢，例如像下面这样：

```
    public static boolean tryGetLock(Jedis jedis,String lockKey,String reqId,int expire){
        long result = jedis.setnx(lockKey,reqId);
        if(result==1){
        	jedis.expire(lockKey,expire);
        }
    }
```

自然是不行的，因为这段代码不能保证原子性，倘若某一个客户端执行完setnx之后就因为某些原因没有继续往下面执行了，那么这个key就一直设置在这里而不能自动过期，这个时候就没有别的客户端能够获取到这个锁了。

​			

第二个问题是我在网上看到的，我初一看还没想明白为什么是错误的加锁代码：			

[代码来源](https://www.cnblogs.com/williamjie/p/9395659.html)

```
public static boolean wrongGetLock2(Jedis jedis, String lockKey, int expireTime) {

    long expires = System.currentTimeMillis() + expireTime;
    String expiresStr = String.valueOf(expires);
    // 如果当前锁不存在，返回加锁成功
    if (jedis.setnx(lockKey, expiresStr) == 1) {
        return true;
    }

    // 如果锁存在，获取锁的过期时间
    String currentValueStr = jedis.get(lockKey);
    if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
        // 锁已过期，获取上一个锁的过期时间，并设置现在锁的过期时间
        String oldValueStr = jedis.getSet(lockKey, expiresStr);
        if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
            // 考虑多线程并发的情况，只有一个线程的设置值和当前值相同，它才有权利加锁
            return true;
        }
    }
      
    // 其他情况，一律返回加锁失败
    return false;
}

```

这段问题在于，1，使用System.currentTimeMillis（）这个系统函数，那么就可能存在多个客户端上时间并不一致的问题。2，多线程的情况下，设置过期时间还是不线程安全。3，俗话说，解铃还须系铃人，这段代码缺少一个能表示客户端的值来用作key的值。所以无论是哪个客户端都能来解锁。

- 释放锁(lua脚本)

看过不少网上博客的会发现在使用Redis实现分布式锁的时候使用了lua脚本，那么为什么需要lua脚本呢，难道就不能手动通过Redis的原生API实现么，如果用了会有什么问题呢，首先看lua脚本是怎么写的。

```
if redis.call('get', KEYS[1]) == ARGV[1] 
	then return redis.call('del', KEYS[1])
else return 0 end
```

这段代码作用挺好理解，就是获取锁对应的值，如果和传来的ARGV[1]即RequestID相等，那么就删除，这是解锁中用到的，现在抛开这个lua脚本不谈，用jedis的api来解锁。		

第一种：					

```
public static void releaseLock(Jedis jedis,String key){
	jedis.del(key);
}
```

第二种：	

```
public static void releaseLock(Jedis jedis,String key,String reqId){
	if(jedis.get(key).equals(reqId)){
		jedis.del(key);
	}
}
```

第一种的问题在于，当一个线程到达解锁方法的时候，没有判别是否这个线程就是给这个key上锁的线程，也就是锁不认主人了，谁都能解开。当然在某些场景下是允许这样的，但是在分布式锁中这样自然不行。而第二个问题看似滴水不漏实际上这是两条命令，而两个命令无法保证原子性，也就是说，判断if之后执行del之前，中间的是有可能其他线程再上锁的，但是这个时候被当前线程解锁了。说到这里，lua脚本的作用自然就出来了，请出官方对于jedis,eval()的解释：

[Redis.eval()](https://redis.io/commands/eval/)

然后中间有一段这个：

```
Atomicity of scripts
Redis uses the same Lua interpreter to run all the commands. Also Redis guarantees that a script is executed in an atomic way: no other script or Redis command will be executed while a script is being executed. This semantic is similar to the one of MULTI / EXEC. From the point of view of all the other clients the effects of a script are either still not visible or already completed.
......
```

**Atomicity**，也就是说，使用lua脚本，能够保证脚本的执行具有原子性，不会因为多个进程竞争而被细分为更小的过程。

- 实测

写一个简单的Controller，然后用JMeter来模拟多个客户端进行请求的情景。

```
@Slf4j
@RestController
public class RedisController {
    

    @Autowired
    private JedisPool jedisPool;

    @GetMapping("/lock")
    public String lock(){

        String reqId = UUID.randomUUID().toString();

        boolean locked = false;

        Jedis jedis = jedisPool.getResource();

        try {
            locked = RedisUtil.tryGetLock(jedis,"lock1",reqId,2000);
            if (locked){
                log.info(reqId+"获取成功\n");
            }else{

                log.info(reqId+"获取失败\n");
            }
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
            //回收jedis实例，不回收jedis实例会pool中的jedis资源越来越少，从而导致获取不到可以用的jedis实例，报异常。
            if(jedis != null ) {
                jedisPool.returnResource(jedis);
            }
        }finally {
            if(locked) {
                boolean released = RedisUtil.releaseLock(jedis, "lock1",reqId);
                if(released){
                    log.info(reqId+"释放成功\n");
                }else{
                    log.info(reqId+"释放失败\n");
                }
            }
            if(jedis != null ) {
                jedisPool.returnResource(jedis);
            }
        }
        return "ok";

    }

}

```

逻辑很简单，设置key，这里设置过期时间为两秒，实际上这里也可以通过请求传入过期时间，这样每个客户端都可以设置自己的时间，然后用sleep(1000)来模拟业务处理。最后释放锁。			

- JMeter测试

用JMeter开启一个线程组，设置五个线程，循环五次，然后用http请求去访问这个接口。

![avatar](D:\VsCodePro\TechNote\src\Image\JMeter限流测试线程组.jpg)

![avatar](D:\VsCodePro\TechNote\src\Image\JMeter限流测试HTTP请求测试.jpg)

- 测试结果

上面五次中，每一个都应该是一个线程获取成功，其他四个获取失败。下面查看一下控制台，截取两次的日志进行查看，可以发现符合预期。

```
2020-10-25 13:32:39.274  INFO 13012 --- [nio-9001-exec-2] c.i.r.controller.RedisController         : a230e2cb-c87c-4abf-a17e-19ea8d3affc5获取成功

2020-10-25 13:32:39.274  INFO 13012 --- [nio-9001-exec-1] c.i.r.controller.RedisController         : 77ce1001-9621-42bb-bd2a-15bce0f83e25获取失败

2020-10-25 13:32:39.684  INFO 13012 --- [nio-9001-exec-3] c.i.r.controller.RedisController         : d9a3d30b-8aa1-4481-b82e-d1a3b347267e获取失败

2020-10-25 13:32:39.684  INFO 13012 --- [nio-9001-exec-4] c.i.r.controller.RedisController         : 48709ff6-5b84-42ca-8b00-83ee64ef756a获取失败

2020-10-25 13:32:39.783  INFO 13012 --- [nio-9001-exec-5] c.i.r.controller.RedisController         : e54a38a1-3c3f-4e3b-99a9-6766b04bafa5获取失败

2020-10-25 13:32:40.296  INFO 13012 --- [nio-9001-exec-2] c.i.r.controller.RedisController         : a230e2cb-c87c-4abf-a17e-19ea8d3affc5释放成功

2020-10-25 13:32:40.393  INFO 13012 --- [nio-9001-exec-2] c.i.r.controller.RedisController         : 740adf9e-bb28-445f-82e4-cf7e8f1bda99获取成功

2020-10-25 13:32:40.394  INFO 13012 --- [nio-9001-exec-1] c.i.r.controller.RedisController         : f5bbe368-e016-4fed-ba05-32700e908404获取失败

2020-10-25 13:32:40.698  INFO 13012 --- [nio-9001-exec-4] c.i.r.controller.RedisController         : cf393325-868f-4137-806e-c52f6d5e2968获取失败

2020-10-25 13:32:40.698  INFO 13012 --- [nio-9001-exec-3] c.i.r.controller.RedisController         : d914d620-2ce6-46df-a002-c92b546978c1获取失败

2020-10-25 13:32:40.792  INFO 13012 --- [nio-9001-exec-6] c.i.r.controller.RedisController         : 7356a83c-30a8-492f-b7a0-179d4ecd27a2获取失败

2020-10-25 13:32:41.396  INFO 13012 --- [nio-9001-exec-2] c.i.r.controller.RedisController         : 740adf9e-bb28-445f-82e4-cf7e8f1bda99释放成功
```

