## ConcurrentHashMap

为什么使用它呢，主要还是应对多线程环境下的操作，因为HashMap多线程下会造成数据问题，而HashTable虽然线程安全但是是使用synchronized来实现线程安全，因为HashTable里是采用的同步方法，这样的话同时访问的线程会竞争同一把锁，效率太低。

### 1.7 1.8的不同

1.7里，是采用的数组+链表，内部结构采用分段锁，分段锁继承自ReentrantLock，尝试获取锁的时候存在并发竞争，自旋，阻塞。

1.8里，是采用数组+链表/红黑树，内部采用的CAS+synchronized，CAS失败自旋保证成功，再失败就synchronized保证，synchronized的话。1.6已经做了优化了，之后再提一嘴。synchronized只锁定当前链表或者红黑树的首节点，这样的话只要hash不冲突，就不会产生并发。

### 1.7

- 基本成员

  ```
  //默认为16个Segment
  final Segment<K,V>[] segments;
  //默认容量，必须是2^n
  static final int DEFAULT_INITIAL_CAPACITY = 16;
  //默认负载因子
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  //默认并发量，
  static final int DEFAULT_CONCURRENCY_LEVEL = 16;
  //map的最大容量
  static final int MAXIMUM_CAPACITY = 1 << 30;
  //HashEntry的默认容量
  static final int MIN_SEGMENT_TABLE_CAPACITY = 2、
  //最大的并发量
  static final int MAX_SEGMENTS = 1 << 16;
  //重试次数
  static final int RETRIES_BEFORE_LOCK = 2;
  //计算Segment的掩码
  final int segmentMask;
  //用于计算Segment位置时，hash参与运算的次数
  final int segmentShift;
  ```

  Segment里的HashEntry和HashMap是差不多的，但不同的是这里面用volatile修饰了value以及下一个结点next。

- put

  找到Segment的位置，先判断当前位置有没有初始化，没有则调用ensureSegment进行初始化，然后调用put。put会先获取锁，成功则继续执行，失败则自旋加锁，成功后继续执行。通过hash计算出位置，获取结点，找出相同的key和hash替换value返回，没有相同的，判断是否需要扩容，然后头插法插入，返回，释放锁。

  ```
  自旋有次数限制，因为竞争激烈的情况下有可能获取不到锁，并且自旋锁需要消耗cpu，所以达到一定的次数之后就需要阻塞线程，直到有新的线程释放锁来通知这些阻塞的线程。
  ```

- get

  get没有加锁，因为get采用了getObjectVolatile来获取Segment，可以保证是最新的。（UNSAFE的getObjectVolatile）

- 有啥不好的地方？

  1.7采用数组+链表，查询的时候，就需要遍历链表，效率比较低，同时Segment虽然用分段锁了，但是存在锁竞争，自旋，阻塞。

### 1.8

- 主要的类成员

  table，默认为null，初始化发生在第一次插入操作，默认大小为16的数组，用来存储node结点，扩容总是两倍扩。

  nextTable，默认为null，扩容的时候形成的数组，大小为原数组的两倍。

  sizeCtl，默认为0，用来控制table的初始化和扩容操作。-1表示正在初始化，-n表示有n-1个线程在进行扩容操作。其他情况，如果table没有初始化，那么表示需要初始化的大小，如果已经初始化完成，表示table的容量，默认是table大小的0.75倍。

  Node，保存key，value以及key的hash值的数据结构，其中value和key都用volatile修饰用来保证可见性。

  ForwardingNode，一个特殊的Node结点，hash值为-1，存储nextTable的引用，只有table发生扩容的时候，forwardingNode才会发生作用，作为一个占位符放在table中表示前结点为空或者已经被移动。

- put

  ```
  final V putVal(K key, V value, boolean onlyIfAbsent) {
          if (key == null || value == null) throw new NullPointerException();
          int hash = spread(key.hashCode());
          int binCount = 0;
          for (Node<K,V>[] tab = table;;) {
              Node<K,V> f; int n, i, fh;
              if (tab == null || (n = tab.length) == 0)
                  tab = initTable();  // lazy Initialization
              else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {  // 当前bucket为空
                  if (casTabAt(tab, i, null,
                               new Node<K,V>(hash, key, value, null)))
                      break;                   // no lock when adding to empty bin
              }
              else if ((fh = f.hash) == MOVED)  // 当前Map在扩容，先协助扩容，在更新值。
                  tab = helpTransfer(tab, f); 
              else {  // hash冲突
                  V oldVal = null;
                  synchronized (f) {
                      if (tabAt(tab, i) == f) {  // 链表头节点
                          if (fh >= 0) {
                              binCount = 1;
                              for (Node<K,V> e = f;; ++binCount) {
                                  K ek;
                                  if (e.hash == hash &&   // 节点已经存在，修改链表节点的值
                                      ((ek = e.key) == key ||
                                       (ek != null && key.equals(ek)))) {
                                      oldVal = e.val;
                                      if (!onlyIfAbsent)
                                          e.val = value;
                                      break;
                                  }
                                  Node<K,V> pred = e;
                                  if ((e = e.next) == null) { // 节点不存在，添加到链表末尾
                                      pred.next = new Node<K,V>(hash, key,
                                                                value, null);
                                      break;
                                  }
                              }
                          }
                          else if (f instanceof TreeBin) { // 红黑树根节点
                              Node<K,V> p;
                              binCount = 2;
                              if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                             value)) != null) {
                                  oldVal = p.val;
                                  if (!onlyIfAbsent)
                                      p.val = value;
                              }
                          }
                      }
                  }
                  if (binCount != 0) {
                      if (binCount >= TREEIFY_THRESHOLD)  //链表节点超过了8，链表转为红黑树
                          treeifyBin(tab, i);
                      if (oldVal != null)
                          return oldVal;
                      break;
                  }
              }
          }
          addCount(1L, binCount);  // 统计节点个数，检查是否需要resize
          return null;
      }  
  ```

  大致过程可以概述为：

  ```
  1，根据key计算出hashCode
  2，判断是否需要进行初始化
  3，即为当前key定位出Node，如果为空表示当前位置可以写入数据，利用CAS尝试写入，失败则自旋保证成功。
  4，如果当前位置的hashCode==-1==MOVED，则表示需要扩容。
  5，如果都不满足，那么利用synchronized写入数据。
  6，如果线程数量大于TREEIFY_THREASHOLD则转换为红黑树。
  ```

  趁热打铁，讲一讲CAS和自旋锁。

  -  CAS

    是一种乐观锁，一种轻量级锁，全称是CompareAndSet，流程是：线程在读取的时候不加锁，在写入数据的时候，比较原值是否修改，若没有被其他线程修改，那么就写，如果已经被修改了，那么重新执行读流程。比如要修改一条数据，那么修改之前就先拿到他原来的值，然后再SQL里加一个判断，原来的值和我手上拿到的他的值是否一样，一样就可以修改，不一样那么久返回错误不处理了。

    但CAS自然也有不好的地方，比如ABA问题，一个线程把值改成了A，然后另一个线程又改回了A，而对于这个时候判断的线程，他拿到的值还是A，所以他知不知道这个值有没有被修改过。那么这个问题怎么能够改善了， 其实也有办法：

    1，加版本号，这样就可以区分修改了。

    2，加时间戳之类的。

  - 再讲一下这里的synchronized

    之前说synchronized性能差，但是后面做了优化，那么这里来收尾。1.6之前它的效率的确低，但后来jvm进行了锁升级的优化，就是先使用偏向锁优先同一个线程再次获取锁，如果失败，那么成为CAS轻量级锁，如果失败，就短暂自旋，因为自旋还是需要cpu的，不能一直自旋下去，最后才会成为重量级锁。

- get

  简单说一下流程：

  1，根据计算出来的hashCode寻址，如果就在桶上那么直接返回，如果是红黑树，那么按照树的方式获取值，如果不满足，那么按照链表的方式遍历获取值。