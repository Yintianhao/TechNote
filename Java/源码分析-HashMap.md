### HashMap(1.7)           
存储结构：包含一个Entry类型的数组table，Entry存储键值对，包含四个字段（int hashCode，K key，V value，Entry<K,V> next），Entry是一个链表，数组中每一个位置被当做一个桶，一个桶存放一个链表。
HashMap采用拉链法来解决冲突，同一个链表中存放哈希值和散列桶取模运算结果相同的Entry         
```
transient Entry[] table;
```
 HashMap通过key的Hashcode再经过扰动函数处理之后得到hash值，然后通过(n-1)&hash判断插入的位置，n是数组的长度，如果当前位置存在元素的话，判断该元素和要存入的元素的hash值是否相同，是则覆盖，否则通过拉链法解决冲突。
### 拉链法       
如：    
```
HashMap<String,String> map = new HashMap<>();
map.put("k1","v1");
map.put("k2","v2");
map.put("k3","v3");
```
插入：       
新建一个HashMap，默认大小是16.      
插入<k1,v1>键值对，先计算k1的hashCode为115，使用除留余数法，115%16=3； 桶下标为3      
插入<k2,v2>键值对，计算k2的hashCode为118，118%16=6，桶下标为6         
插入<k3,v3>键值对，k3的hashCode为118,桶下标为6，插在k2之前。          
查找：       
一：计算键值所在的桶的下标，在链表上进行顺序查找，时间复杂度跟链表长度成正比。         

### 关键的几个成员变量       
```
//实际存储的kv键值对的个数。
transient int size;
//负载因子，代表了table的填充度是多少，默认是0.75
final float loadFactor;
//threshold一般是capacity*loadFactor，在进行扩容是要考虑。      
int threshold;
```
### put 1.7
1，如果定位到的数组位置没有元素，就直接插入         
2，如果定位到的数组位置有元素，遍历以这个元素为头结点的链表，一次和插入的key比较，如果相同就覆盖，否则采用头插法插入这个元素。
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
HashMap允许null键值存在，但是null无法得到哈希值。所以强制用table[0]存放键值为null的键值对   

#### put 1.8
如果定位到的数组位置没有元素，就直接插入，如果定位到的数组位置有元素就和插入的key比较，如果key相同，那么覆盖，否则判断p是否是一个树节点，是则调用putTreeVal将元素添加插入将元素加入，如果不是则遍历链表插入         
### inflateTable  
当数组是空时，需要分配内存。
```
private void inflateTable(int toSize) {
        int capacity = roundUpToPowerOf2(toSize);//capacity一定是2的次幂 即是大于toSize的最小的2的N次方数 toSize为12返回的是16  toSize为16返回的为16 toSize为17返回的是32
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);//此处为threshold赋值，取capacity*loadFactor和MAXIMUM_CAPACITY+1的最小值
        table = new Entry[capacity];
        initHashSeedAsNeeded(capacity);
    }
```
### addEntry       
```
void addEntry(int hash, K key, V value, int bucketIndex) {
        if ((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);//当size超过临界阈值threshold，并且即将发生哈希冲突时进行扩容
            hash = (null != key) ? hash(key) : 0;//是空则为哈希值为0，在之后查找桶位置的时候0%table.length=0，即存放在第0号桶
            bucketIndex = indexFor(hash, table.length);
        }

        createEntry(hash, key, value, bucketIndex);//table[bucketIndex]=new Entry<>(hash,key,value,e);e是该桶的原来的头结点，即头插法
    }

```
### 确认桶的下标       
#### hash()        
hash函数运用许多的异或和移位操作对key的hashCode做进一步的计算，以及二进制的调整来保证最终存储的位置的均匀。
```
final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }
 
        h ^= k.hashCode();
 
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
   
    public final int hashCode(){
      return Objects.hashCode(key)^Objects.hashCode(value);
    }
```
#### 运用位运算达到取模的效果   
针对两个数，x,y,y%x等价于y&(x-1)         
所以确定桶下标的最后一步：indexFor()            
```
static int indexFor(int h, int length) {
      return h & (length-1);
  }

```
### 扩容        
#### 基本原理       
假设HashMap中的table长度为M，需要存的键值对为N，则为了满足哈希函数均匀的特点，每条链表长度大约为N/M。       
要使得查找效率越高，则N/M应该越小，N一定的情况下，M尽可能大，也就是table的长度尽可能大，HashMap采用动态扩容的方法根据N值来计算M值。       
和扩容有关的有那么四个因素，分别是capacity，size，threshold，load_factor          
```
capacity：table的容量大小，默认16，并且保持是2的n次方。        
size：键值对数量        
threshold：size的临界值，size大于等于它是就需要进行扩容      
loadFactor：装载因子，table能够使用的比例，threshold=(int)(capacity*loadFactor)；
```
从addEntry代码中可以看出扩容时，capacity为原来的两倍。       
并且由resize()实现扩容，扩容操作会把原来的oldTable的键值对都加入到newTable中，所以较为耗时。      
```
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }
    //创建新的散列表，通过transfer将旧的数据传输到newTable，并且重新定义当前Map的table，阈值threshold
    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable, initHashSeedAsNeeded(newCapacity));
    table = newTable;
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}
void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
　　　　　//for循环中的代码，逐个遍历链表，重新计算索引位置，将老数组数据复制到新数组中去
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);//根据新数组的大小下标计算出重新存入的位置。
　　　　　　　　　 //将当前entry的next链指向新的索引位置,newTable[i]有可能为空，有可能也是个entry链，如果是entry链，直接在链表头部插入。
                e.next = newTable[i];
                newTable[i] = e;
                e = next;//链表向后
            }
        }
    }
```

***重新计算桶下标***     
扩容时，键值对需要重新计算桶下标，HashMap通过hash%capacity来确认桶下标，HashMap capacity为2的n次方的特性降低了复杂性。    
因为只需要移位即可，如原有数组长度是16,扩容之后长度为32，即00010000-->00100000   
对于一个key，如果哈希值在第五位，那么有：    
为0  hash%00010000=hash%0010000，跟原位置一致。      
为1 hash%00010000=hash%00100000+16 在原位置下+16        
***计算数组容量***            
HashMap允许用户传入容量不是2的n次方，因为之后会转换为适合的2的n次方。      
需要了解掩码，一个数的掩码的求法：     
假设一个数为mask = 10010000，掩码为11111111，由以下步骤获得：     
```
mask|=mask>>1
mask|=mask>>2
mask|=mask>>4       
经过以上步骤，mask+1就为大于该数的最小的2的n次方数。
```
在HashMap做法也类似：      
```
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

```
***JDK1.8的变化***    
1.7的HashMap是数组+链表；1.8的HashMap是数组+链表+红黑树。      
在一个桶的链表长度大于等于8时就会变成红黑树。    
红黑树的几个参数：    
```
1，static final int TREEIFY_THRESHOLD = 8; //桶的树化阈值，即在链表值大于该值时，会转为红黑树。     
2，static final int UNTREEIFY_THRESHOLD = 6; //桶的链表还原阈值，将红黑树转为链表的阈值，在扩容resize重新计算存储位置时，原有的红黑树小于6时，红黑树转化为链表。 
3，static final int MIN_TREEIFY_CAPACITY = 64;//最小树形化容量阈值，当哈希表的容量大于该值时，才允许树化为链表，否则，若桶内元素太多时，则直接扩容，而不是树形化,为了避免进行扩容、树形化选择的冲突，这个值不能小于 4 * TREEIFY_THRESHOLD
```
***与HashMap与HashTable的区别***       
HashTable使用synchronized同步。      
HashMap可以插入null的键。    
HashMap的迭代器是fail-fast的迭代器。     
HashMap不能保证随着时间的推移Map中的元素的位置是不变的。    


