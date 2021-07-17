## Replication
在kafka0.8以前的版本中，是没有
replication的，这就导致一旦一个Broker宕机，那么其他所有的partition数据都不可用被消费，同时生产者也不能将消息存入到这些partition中。         
如果生产者使用同步模式则生产者会尝试重新发送message.send.max.retries次后抛出异常，用户可以选择停止发送后续数据也可以继续发送数据，停止发送会造成数据阻塞，继续发送会导致整个Broker的数据丢失。      
如果生产者使用异步模式，那么生产者会尝试重新发送message.send.max.retries后记录该异常并且继续发送数据，这样会造成数据丢失并且用户只能通过日志才能发现问题，因为kafka的生产者并没有对异步模式提高回调。           
所以没有replication的情况下，一旦机器宕机或者某个Broker停止工作，会造成整个系统的可用性降低。           
## 选举Leader
有了replication之后，同一个partition可能会有多个replication，这时候就需要在这些replication中选举出leader，消费者生产者只与leader交互，其他replication作为follower从leader当中复制数据。这样做的目的是因为，如果不设置一个leader，那么为了确保多个replication中能够同步数据，就必须需要保证多个replication能够互相同步数据，假设replication有N个，那么就有N*N个通路，数据一致性很难保证，而有了leader之后，只需要从leader中同步数据即可，相当于N条通路，简单高效。           
## Kafka 高可用设计
### 将所有replication均匀分布到集群
分配算法：      
1，将所有broker和待分配的partition排序      
2，将第i个partition分配到第(i%n)个broker上      
3，将第i个partition的第j个replication分配到第(i+j)%n个broker上。
### 副本策略
也就是上面replication策略。
#### 消息传递同步策略
生产者在发布消息到某个partition中的时候，先通过Zookeeper找到这个partition的leader，将该信息发送给这个leader，leader会将信息写入日志。每个follower都从leader拉数据，这种方式下，follower存储的数据和leader是一致的。follower在收到信息也写入日志，并且向leader发送ACK消息，一旦leader把所有的replication的ACK都收到了，说明消息已经被commit了。随之leader会向生产者发送ACK信息。     
为了性能，上面的描述中，其实每个follower在接收到数据之后会立马发送ACK到leader，而不是等待写入日志，对于已经commit的消息，kafka只能保证它被存到多个replication的内存中，而不保证持久化，当然，也不能保证异常发生后这个消息一定会被消费者消费。但是只有commit的消息才会暴露给消费者。
#### ACK前的备份
判断一个Broker是否存活，一是broker必须维护其和Zookeeper的session，说到代码上就是心跳机制。二是follower必须能够及时地将leader的消息复制过来。            
leader会跟踪它的follower列表，如果其中一个宕机或者落后太多，那么会将它从列表中移除，kafka的复制机制并不是完全的同步复制和异步复制，因为完全同步会影响吞吐率，而异步的情况下，数据只要被写入日志就会被认为已经commit，这种情况下，如果follower都复制完都落后于leader，那么如果leader突然挂了，就会造成数据丢失。采用列表的方式就可以很好的均衡了确保数据不丢失以及吞吐率，follower可以批量的从leader复制数据，提高复制性能，减少leader和follower的差距。
### leader的选举算法
选举机制具体来讲是一个分布式锁，有两种方式实现基于Zookeeper的分布式锁：         
```
节点名称的唯一性：即多个客户端创建一个节点，只有成功创建节点的客户端才能获得锁。
临时顺序节点：所有客户端在某个目录下创建自己的临时顺序节点，只有序号小的才能获得锁。
```