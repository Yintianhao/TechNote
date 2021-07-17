## ConcurrentHashMap       
```
(1) 背景
HashMap死循环:HashMap在并发执行put操作时会引起死循环,是因为多线程会导致HashMap的Entry链表形成环形数据结构,
一旦形成环形数据结构,Entry的next节点永远不为空,就会产生死循环获取Entry.
HashTable效率低下:HashTable容器使用synchronized来保证线程安全,但在线程竞争激烈的情况下HashTable的效率非常
低下.因为当一个线程访问HashTable的同步方法,其它线程也访问HashTable的同步方法时,会进入阻塞或轮询状态.如线程1
使用put进行元素添加,线程2不但不能使用put方法添加元素,也不能使用get方法获取元素,所以竞争越激烈效率越低.
(2) 简介
HashTable容器在竞争激烈的并发环境下表现出效率低下的原因是所有访问HashTable的线程都必须竞争一把锁,假如容器里
有多把锁,每一把锁用于锁容器其中一部分数据,那么多线程访问容器里不同的数据段时,线程间不会存在竞争,从而可以有效
提高并发访问效率,这就是ConcurrentHashMap所使用的锁分段技术.首先将数据分成一段一段地储存,然后给每一段配一把锁,当
一个线程占用锁访问其中一段数据时,其它段的数据也能被其它线程访问.
```
ConcurrentHashMap由Segment和HashEntry组成，Segment是一种可重入锁，HashEntry用于存储键值对数据，一个ConcurrentHashMap包含
一个Segment组，Segment的结构和HashMap类似，是一种数组加链表的设计，每个Segment维护一个HashEntry数组里面的元素，
当对HashEntry进行修改时，首先需要获得与它对应的Segment锁。       
Segment：
```
static final class Segment<K,V> extends ReentrantLock implements Serializable {

    private static final long serialVersionUID = 2249069246763182397L;

    //加锁时，在阻塞之前的自旋次数
    static final int MAX_SCAN_RETRIES =
        Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;
    //每个Segment的HashEntry，volatile保证可见性
    transient volatile HashEntry<K,V>[] table;
    //元素的数量，只能在锁中或者其他保证可见性的地方才能访问
    transient int count;
    //当前Segment发生变化的次数，为ConcurrentHashMap.isEmpty()和size方法中的稳定性检查提供了足够的准确性。
    transient int modCount;
    //扩容阈值 threshold = capacity*loadFactor
    transient int threshold;
    //负载因子
    final float loadFactor;
}
```
size操作          
执行size，需要遍历所有的Segment然后把count累计计算，ConcurrentHashMap在执行size操作的时候，先尝试不加锁，如果连续两次不加锁的情况下
得到的结果一致，就认为结果是正确的，尝试次数使用RETRIES_BEFORE_LOCK定义（等于2），初始值为-1，因此尝试次数为3，如果尝试次数超过三，则
需要对Segment加锁。

ConcurrentHashMap的基本成员      
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
### put()       
流程：       
```
1，找出Segment的位置，先判断当前位置有没有初始化，没有则调用ensureSegment进行初始化。然后调用put。
2，put方法获取Segment的锁，成功继续执行，失败则调用scanAndLockForPut自旋加锁，成功后继续执行。       
3，通过hash计算出位置，获取节点，找出相同的key和hash替换value返回。没有相同的，判断是否需要扩容，然后头插法插入。返回。释放锁。
```
put失败自旋锁：       
```
在put方法中获取锁失败，就会进入scanAndLockForPut,这个方法采用自旋锁进行取锁，直到成功再返回。自旋锁次数的限制的好处在于：
竞争激烈的情况下可能获取不到锁，因为自旋锁需要消耗CPU性能，所以达到次数之后就应该阻塞该线程，直到有线程释放锁，
通知这些线程。并且在这个过程中，其他线程有可能对Segment进行修改，这时使用volatile的方法重新读取了数据，在自旋的过程中遍历了些
数据，把新数据就从数据缓存中读出到工作内存，当前线程获取锁，我们的数据就是最新的，而不需要重新从内存中获取。从而提高了效率，
```

```
private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
        HashEntry<K,V> first = entryForHash(this, hash); // 根据hash获取头结点
        HashEntry<K,V> e = first;
        HashEntry<K,V> node = null;
        int retries = -1; // 是为了找到对应hash桶,遍历链表时找到就停止
        while (!tryLock()) { // 尝试获取锁,成功就返回,失败就开始自旋
            HashEntry<K,V> f; // to recheck first below
            if (retries < 0) {
                if (e == null) {  // 结束遍历节点
                    if (node == null) // 创造新的节点
                        node = new HashEntry<K,V>(hash, key, value, null);
                    retries = 0; // 结束遍历
                }
                else if (key.equals(e.key)) // 找到节点 停止遍历
                    retries = 0;
                else
                    e = e.next; // 下一个节点 直到为null
            }
            else if (++retries > MAX_SCAN_RETRIES) { // 达到自旋的最大次数
                lock(); // 进入加锁方法,失败进入队列,阻塞当前线程
                break;
            }
            else if ((retries & 1) == 0 &&
                    (f = entryForHash(this, hash)) != first) {
                e = first = f; // 头结点变化,需要重新遍历,说明有新的节点加入或者移除
                retries = -1;//归位
            }
        }
        return node;
    }
```
### get方法       
get没有使用锁。但是，get方法采用了getObjectVolatile获取Segment和HashEntry，可以保证是最新的。
```
(1) 首先获取value,我们要先定位到segment,使用了UNSAFE的getObjectVolatile具有读的volatile语义,也就表示在多线程情况下,我们依旧能获取最新的segment.
(2) 获取hashentry[],由于table是每个segment内部的成员变量,使用volatile修饰的,所以我们也能获取最新的table.
(3) 然后我们获取具体的hashentry,也时使用了UNSAFE的getObjectVolatile具有读的volatile语义,然后遍历查找返回.
(4) 总结我们发现怎个get过程中使用了大量的volatile关键字,其实就是保证了可见性(加锁也可以,但是降低了性能),get只是读取操作,所以我们只需要保证读取的是最新的数据即可.
```
### 与1.8      
JDK 1.7 中的CHM使用分段锁机制来实现并发更新操作，核心类为 Segment，它继承自重入锁 ReentrantLock，并发度与 Segment 数量相等。

JDK 1.8 的CHM使用了 CAS 操作来支持更高的并发度，在 CAS 操作失败时使用内置锁 synchronized。

并且 JDK 1.8 CHM的实现也在链表过长时会转换为红黑树。

