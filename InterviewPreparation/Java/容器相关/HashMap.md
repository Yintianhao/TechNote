## HashMap

面试常客。

首先说一下，1.7和1.8是有区别的，结构上，1.7是数组+链表，1.8是数字+链表/红黑树。而在插入的时候，1.7是头插法，1.8是尾插法。

另外，梳理一下HashMap插入和移除的过程：

插入：

```
先通过key的hashCode经过扰动函数来处理之后得到hash值，然后通过(n-1)&hash来判断当前元素存放的位置，如果这个位置存在元素的化，那么判断这个元素是否和要存入点元素的hash和key相等，如果相等，那么直接覆盖，不相同就通过拉链法解决冲突。在1.8之后呢，当HashMap的容量到达64并且链表长度到达8的时候，转为红黑树，而不再是链表了。在链表长度小于6以后，又会退化。
```

### 1.8源码分析

- 类成员

  ```
  	/**
  	 * 默认的初始容量为16
  	 */
  	static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  
      /**
       * 最大的容量为2的30次方
       * The maximum capacity, used if a higher value is implicitly specified
       * by either of the constructors with arguments.
       * MUST be a power of two <= 1<<30.
       */
      static final int MAXIMUM_CAPACITY = 1 << 30;
  
      /**
       * 默认的装载因子
       * The load factor used when none specified in constructor.
       */
      static final float DEFAULT_LOAD_FACTOR = 0.75f;
  
      /**
       * 当一个桶中的元素个数大于等于8时进行树化
       * The bin count threshold for using a tree rather than list for a
       * bin.  Bins are converted to trees when adding an element to a
       * bin with at least this many nodes. The value must be greater
       * than 2 and should be at least 8 to mesh with assumptions in
       * tree removal about conversion back to plain bins upon
       * shrinkage.
       */
      static final int TREEIFY_THRESHOLD = 8;
  
      /**
       * 当一个桶中的元素个数小于等于6时把树转化为链表
       * The bin count threshold for untreeifying a (split) bin during a
       * resize operation. Should be less than TREEIFY_THRESHOLD, and at
       * most 6 to mesh with shrinkage detection under removal.
       */
      static final int UNTREEIFY_THRESHOLD = 6;
  
      /**
       * 当桶的个数达到64的时候才进行树化
       * The smallest table capacity for which bins may be treeified.
       * (Otherwise the table is resized if too many nodes in a bin.)
       * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
       * between resizing and treeification thresholds.
       */
      static final int MIN_TREEIFY_CAPACITY = 64;
      /* ---------------- Fields -------------- */
  
      /**
       * 存储元素的数组，总是2的幂次倍
       * The table, initialized on first use, and resized as
       * necessary. When allocated, length is always a power of two.
       * (We also tolerate length zero in some operations to allow
       * bootstrapping mechanics that are currently not needed.)
       */
      transient Node<K,V>[] table;
  
      /**
       * 保存entrySet()的缓存
       * Holds cached entrySet(). Note that AbstractMap fields are used
       * for keySet() and values().
       */
      transient Set<Map.Entry<K,V>> entrySet;
  
      /**
       * 元素的数量
       * The number of key-value mappings contained in this map.
       */
      transient int size;
  
      /**
       * 每次扩容和更改map结构的计数器
       * The number of times this HashMap has been structurally modified
       * Structural modifications are those that change the number of mappings in
       * the HashMap or otherwise modify its internal structure (e.g.,
       * rehash).  This field is used to make iterators on Collection-views of
       * the HashMap fail-fast.  (See ConcurrentModificationException).
       */
      transient int modCount;
  
      /**
       * 临界值 当实际大小(容量*填充因子)超过临界值时，会进行扩容
       * The next size value at which to resize (capacity * load factor).
       *
       * @serial
       */
      // (The javadoc description is true upon serialization.
      // Additionally, if the table array has not been allocated, this
      // field holds the initial array capacity, or zero signifying
      // DEFAULT_INITIAL_CAPACITY.)
      int threshold;
  
      /**
       * The load factor for the hash table.
       *加载因子
       * @serial
       */
      final float loadFactor;
  
  ```

  科普一下几个字段的作用。

  1，容量，为数组的长度，默认为16，最大为2的30次方，当容量达到64的时候才能转红黑树。	

  2，加载因子，这是用来控制数组存放数据的疏密程度的，越接近于1，数组存放的数组越密集，存在冲突的可能性越大，越小，那么相反就越稀疏，碰撞概率越小，但是同时空间利用率也越低。		

  3，树化有两个条件，和上面的两个字段有关，也就是数组达到8，总容量达到64才树化。

- Node

  ```
  /**
  *Basic hash bin node, used for most entries. (See below for TreeNode subclass, and in 
  *LinkedHashMap for its Entry subclass.)
  */
  static class Node<K,V> implements Map.Entry<K,V> {
          final int hash;
          final K key;
          V value;
          // 指向下一个节点
          Node<K,V> next;
  
          Node(int hash, K key, V value, Node<K,V> next) {
              this.hash = hash;
              this.key = key;
              this.value = value;
              this.next = next;
          }
  
          public final K getKey()        { return key; }
          public final V getValue()      { return value; }
          public final String toString() { return key + "=" + value; }
  		// 重写hashCode()方法
          public final int hashCode() {
              return Objects.hashCode(key) ^ Objects.hashCode(value);
          }
  
          public final V setValue(V newValue) {
              V oldValue = value;
              value = newValue;
              return oldValue;
          }
  		// 重写 equals() 方法
          public final boolean equals(Object o) {
              if (o == this)
                  return true;
              if (o instanceof Map.Entry) {
                  Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                  if (Objects.equals(key, e.getKey()) &&
                      Objects.equals(value, e.getValue()))
                      return true;
              }
              return false;
          }
      }
  ```

  Node是HashMap静态内部类，是一个单链表结点，hash存放key计算来的hash值。

- TreeNode

  这是红黑树结构里的了，我没那么大能耐，暂时没时间啃，就不啃了。

- 构造方法

  ```
  	/// 指定“容量大小”和“加载因子”的构造函数
      public HashMap(int initialCapacity, float loadFactor) {
      	//初始容量合法性校验
          if (initialCapacity < 0)
              throw new IllegalArgumentException("Illegal initial capacity: " +
                                                 initialCapacity);
          if (initialCapacity > MAXIMUM_CAPACITY)
              initialCapacity = MAXIMUM_CAPACITY;
          //装载因子合法性校验
          if (loadFactor <= 0 || Float.isNaN(loadFactor))
              throw new IllegalArgumentException("Illegal load factor: " +
                                                 loadFactor);
          this.loadFactor = loadFactor;
          // 扩容门槛计算
          this.threshold = tableSizeFor(initialCapacity);
      }
      //传入默认装载因子
      public HashMap(int initialCapacity) {
          this(initialCapacity, DEFAULT_LOAD_FACTOR);
      }
      
  	//  默认无参构造方法， 所有属性使用默认值
      public HashMap() {
          this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
      }
      
      public HashMap(Map<? extends K, ? extends V> m) {
          this.loadFactor = DEFAULT_LOAD_FACTOR;
          putMapEntries(m, false);
      }
  
  ```

  提一嘴传入加载因子的，threshold计算不是取的直接计算出来的值，而是传入初始容量的最接近的2次方的值。

  ```
  这里解释一下为什么需要这样来搞，首先hashCode的值返回的是int，其实这个范围是很大的，发生碰撞概率还是没那么容易，但很明显内存中没法放入那么大的数组，所以就需要对hashCode进行取模了，那么问题，但是取模可以直接用%，为什么用(n-1)&hash呢，因为对于2的次幂的数来说，满足(n-1)&hash = hash%n，这样就方便进行位运算了。
  ```

  

### put

```
public V put(K key, V value) {
    //如果table数组为空数组{}，进行数组填充（为table分配实际内存空间），入参为threshold，此时threshold为initialCapacity 默认是1<<4(2^4=16)
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
   //如果key为null，存储位置为table[0]或table[0]的冲突链上
    if (key == null)
        return putForNullKey(value);//单独处理
    int hash = hash(key);//对key的hashcode进一步计算，确保散列均匀
    int i = indexFor(hash, table.length);//获取在table中的实际位置
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
    //查询table，如果该对应数据已存在，执行覆盖操作。用新value替换旧value，并返回旧value
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    modCount++;//保证并发访问时，若HashMap内部结构发生变化，快速响应失败，Fail-Fast
    addEntry(hash, key, value, i);//新增一个entry
    return null;
} 
```

注意，null值会放在第0位，因为无法得到hash值。这里注意HashTable里的Key或者Value是不能为空的，在HashTable的源码里，put的时候会取key的hashcode，会抛出异常，在设置value的时候也做了判断，会抛出空指针异常。另外就是HashTable在进行迭代的时候，会判断这个数据是不是过时使用的，所以这个时候必然要查看key和value，那么这也要求了HashTable的key和value不能为空。h'h

### 多线程下的问题

多线程下死循环的问题，这个问题在1.7会有，但是1.8已经解决了，尽管如此，多线程还用HashMap就没必要了，用ConcurrentHashMap就行了。这里面的深层次原因我也不探讨了，感觉没必要。



