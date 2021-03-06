## 使用场景 
### 计数器
可以对String进行自增自减运算，从而实现计数器功能。
Redis的读写性能很高，适合存储频繁读写的计数量。
### 缓存
将热点数据放在内存中，设置内存的最大使用量和淘汰策略来保证缓存的命中率。
### 查找表
例如存放DNS记录，token等。
### 消息队列
list是一个双向链表，可以通过lpush和rpop来吸入和读取消息
### 分布式锁
在分布式场景下，无法使用单机环境下的锁来对多个结点上的进程进行同步，可以使用redis中的setnx命令丝线分布式锁，除此之外还可以通过官方提供的RedLock来实现。
## Redis和Memcached
### 数据类型
前缀支持五种不同类型，后者仅支持字符串
### 数据持久化
前者采用RDB快照和AOF日志，后者不支持持久化
### 分布式
前者有RedisCluster来提供分布式得支持，后者不支持分布式，只能通过客户端的一致性哈希来实现分布式存储，这种方式在存储和查询时都需要先在客户端计算一次数据所在的结点。
### 内存管理机制
前者并不是把所有数据一直存储在内存中，可以将一些很久没用的值存储在磁盘，而memcached则一直存在内存。
memcached将内存分割成特定的长度的来存储数据，以完全解决内存碎片的问题，但是这种方式的内存利用率不高。

## 数据淘汰策略
可以设置内存最大使用量，超出时实行数据淘汰策略。
### 六种淘汰策略
volatile-lru 从已设置过期时间的数据集中挑选出最近最少使用的数据淘汰                              
volatile-ttl 从已设置过期时间的数据集中挑选出过期的数据淘汰              
volatile-random 从已设置过期时间的数据集中挑选出任意的数据淘汰           
allkeys-lru 从所有数据集中挑选出最近最少使用的数据淘汰                 
allkeys-random 从所有数据集中随机选出数据淘汰                             
noeviction 禁止驱逐数据                       
**做缓存时可以设置内存最大使用量是热点数据的占用内存量，然后启用allkeys-lru策略**
## 持久化
### RDB
将某个时间点的所有数据都保存在磁盘，可以将快照复制到其他服务器从而创建相同数据的服务器副本，
如果系统发生问题，将会丢失最后一次创建快照之后的数据，如果数据量很大，保存快照的时间会很长
### AOF 
将写命令添加到AOF文件的末尾         
使用AOF持久化需要设置同步选项，从而确保写命令同步到磁盘文件的时机，这是因为对文件进行写入并不会马上将内容同步到磁盘去，而是先存储到缓冲区，
然后由操作系统决定什么时候同步                     
          
always:每个写命令都同步         
everysec:每秒同步一次         
no：让操作系统决定        
        
```
always会严重降低服务器性能
everysec 可以保证系统崩溃时只丢失 一秒左右的数据
no 并不能给服务器性能带来多大提升，而且还会丢失比everysec更多的数据量。
```

## 事件(接redis.md后面)
### Redis的两种事件
```
文件事件:redis通过套接字与客户端进行通信，而文件事件就是服务器套接字的抽象，服务器与客户端的通信通常会发生相应的文件事件，而服务器通过监听
并处理这些事件来完成一系列操作。Redis基于Reactor模式开发了自己的网络事件模型，使用I/O复用程序来同时监听多个套接字，并将到达的事件传送给文件事件分派器，分派器根据套接字产生的时间类型调用响应的时间处理器。
时间事件：Redis服务器的一系列操作需要在给定的时间点执行，而时间事件就是服务器对这类定时操作的抽象。(定时事件和周期性时间)
```
## 复制
### 连接过程
主服务器创建快照文件，发送给从服务器，并在发送期间使用缓冲区记录执行的命令，快照文件发送完毕后，开始向从服务器发送缓冲区存储的写命令。
从服务器丢弃所有数据，载入主服务器发送的快照文件，之后从服务器开始接受主服务器发来的写命令。主服务器每执行一次写命令，就向从服务器发送相同的写命令。
### 主从链
随着负载的上升，主服务器可能无法很快地更新从服务器，或者重新连接和重新同步从服务器导致系统超载，这时候可以创建一个中间层来分担主服务器的复制工作，中间层服务器是最上层服务器的从服务器，又是最下层的服务器的主服务器。

## 哨兵
可以监听集群中的服务器，并在主服务器进入下线状态时，自动从服务器中选举出新的主服务器。
