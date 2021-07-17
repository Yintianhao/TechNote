## Storm

### 简介

人称实时版Hadoop，是一套由Twitter开源的分布式实时大数据处理框架。

### 应用场景

推荐，金融，预警，网站统计等各种系统。

### 特性

- 适用场景广泛
- 可收缩性高
- 保证无数据丢失
- 健壮性高
- 容错性高
- 语言无关性

### 集群结构

|       | 主节点 | 工作节点   | 作业       |
| ----- | ------ | ---------- | ---------- |
| storm | Nimbus | Supervisor | Topologies |

主节点和工作节点之间的协调通过Zookeeper集群来完成，所以这两者之间无法直接连接，并且是无状态的，所有的状态都维持在ZK或者本地磁盘中。

### 主要概念

- Topologies

  实时应用程序的逻辑被封装在Storm topologies中，storm topology类似于MapReduce作业，两者之间的关键的区别是MapReduce作业最终会完成，而Topology的任务永远会运行，除非kill掉，一个Topo是Spout和Bolt通过StreamGrouping连接起来的有向无环图。

- Streams

  Stram是Storm中的核心概念，一个Stream是一个无界的，以分布式方式并行创建和处理的Tuple序列，Stream以一个Schema来定义，这个Schema用来命名Stream tuple（元祖）中的字段，默认情况下Tuplele可以包含integers，longs，shorts，bytes，strings，doubles，floats，booleans，byte arrays等数据类型，当然也可以实现自己的序列化和反序列化来自定义自己的类型。

- Spouts

  Spout是一个Topo的源头，通常Spout会从外部数据源读取Tuple，然后把他们发送到Topo中，Spout可以是可靠的也可以是不可靠的，可靠的Spout在Storm处理失败的时候能够重放Tuple，比可靠的一般发送Tuple之后就不再管了。

  Spout可以发送多个流，可以使用OutputFieldDeclarer的declareStream来定义多个流，在SpoutOutputCollector对象的emit方法中制定要发送到的Stream。

  Spout中最主要的方法就是nextTuple，这个方法顾名思义是向拓扑中发送一个新的Tuple，要是在没有tuple发送的情况下就会直接返回，对于任何spout的实现，nextTuple的方法都必须是非阻塞的，因为storm在一个线程中调用所有的spout方法。除此之外，spout还有ack和fail方法，这些方法检测到发出去的tuple被成功处理还是处理失败的时候会被调用，ack和fail只会在可靠的spout中被调用。

- Bolts

  最主要的方法是execute方法，当有一个新tuple输入的时候会进入这个方法，Bolt使用OutputCollector对象发送新的tuple，bolt必须在每一个tuple处理完调用OutputCollector中的ack方法，storm就会知道tuple在什么时候完成，最终确定调用原spout tuple是没问题的，当处理一个输入的tuple，会基于这个tuple产生零个或者多个tuple发送出去，当所有的tuple完成的时候，会调用acking，storm提供的IBasicBolt接口会自动执行acking。

- Stream Groupings

  拓扑定义中有一部分是为每一个bolt制定输入的streams，stream grouping定义了stream如何在bolts task之间分区。storm中内置了八个Stream Grouping。分别是

  - Shuffle grouping，tuple随机分发到Bolt task，每个bolt获取到等量的tuple。
  - Fields grouping，streams通过grouping制定字段来分区，例如通过流的id字段来分区，具有相同的id的tuple会发送到同一个task，不同id 的tuple可能会流入到不同的tasks。
  - Partial Key grouping，stream通过grouping指定的field来分组，与上面的类似，但对于下游的bolt来说是负载均衡的，可以在输入不平均的情况下提供更好的优化。
  - All grouping，stream在所有的bolt tasks之间复制。
  - Global grouping，整个stream会进入bolt其中一个任务，即进入id最小的任务。
  - None grouping，不需要关心如何分组，当前和shuffle grouping等价，同时storm将使用none grouping的bolts和上游订阅的bolt和spout运行在同一个线程。
  - Direct grouping，一种特殊的grouping，stream用这种方式分组意味着由这个tuple的生产者来决定哪个消费者来接收它，Direct grouping只能被用于direct streams，被发射到direct stream的tuple必须使用emitDirect来发送，bolt可以使用TopologyContext或者通过保持对OutputCollector中emit方法输出跟踪，获取所有消费者的ID。
  - Local or shuffle grouping，如果目标bolt有多个task和stream源，在同一个worker进程中，tuple只会shuffle到相同的worker的任务，否则，就和shuffle grouping一样。

- Task

  每个Spout或者Bolt都以跨集群的多个task方式执行，每个task对应一个execution的线程，stream groupings定义如何从一个task发送tuple到另一个task。

- Worker

  拓扑在一个或者多个worker上执行，每个worker进程是一个物理的JVM，执行拓扑 Tasks中的一个子集，比如一个拓扑的并行度是300，总共有50个Worker在运行，每个worker会分配到6个task，storm会尽量把所有的task均匀地分配到所有的worker上。

- 调度器

  - pluggable scheduler

    可插拔调度器，开发者可以实现自己的调度器来替换默认的调度器，自定义executor到worker的调度算法，使用的时候，在storm配置文件中将storm.scheduler配置为自定义类即可。

  - Isolation Scheduler

    隔离调度器使得多个拓扑共享集群资源更加容易和安全，允许开发者制定某些拓扑是隔离的，这意味着隔离的拓扑运行在集群的特定及其上，这些及其没有其他图谱运行，隔离的集群具有高优先级，所以如果和非隔离的拓扑竞争资源，资源就会分配给隔离的拓扑，如果必须给隔离拓扑分配资源，那么会从非隔离拓扑中抽取资源，一旦所有的隔离拓扑的所需资源被满足，那么剩下的机器将会被非隔离的拓扑共享。
    
    