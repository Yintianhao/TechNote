## LinkedHashMap       
```
public class LinkedHashMap<K,V>
    extends HashMap<K,V>
    implements Map<K,V>
```
LinkedHashMap继承自HashMap，因此和HashMap一样具有快速查找特性。       
LinkedHashMap内部维护一个双向链表，用来维护插入顺序或者LRU顺序。      
```
     //指向头结点
    transient LinkedHashMap.Entry<K,V> head;
    //指向尾结点
    transient LinkedHashMap.Entry<K,V> tail;
    
    
    //Entry
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
    
    //accessOrder 决定了顺序，默认为false，即为插入顺序，如果是true，则为LRU 
    final boolean accessOrder;
    
```
LinkedHashMap核心是两个维护顺序的函数。          
## afterNodeAccess     
当一个结点被访问时，如果accessOrder为true，那么会将结点移动到链表尾部，也就是制定为LRU顺序之后，在每次访问一个结点的时候，会将这个结点移动到链表尾部。      
保证链表尾部是最近访问的结点，那么链表首部就是最远最久未使用的的结点。       
```
void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }
```
## afterNodeInsertion        
在put操作之后执行此方法，当removeEldestEntry执行返回true时会移除最晚的结点，即首部结点。      
```
void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        //evict只有在构建map时才为true，这里为false
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }
```
removeEldestEntry默认为false，如果需要为true，需要继承LinkedHashMap并且重写这个方法，在实现LRU的缓存起到很大的作用，通过移除最近最久的结点，从而保证缓存空间足够。        
## LRU缓存         
步骤：         
设定最大的缓存空间MAX_ENTRIES为3
使用LinkedHashMap的构造函数将accessOrder设置为true，开启LRU顺序。       
覆盖removeEldestEntry，在跌点多余MAX_ENTRY的时候就会将最近最久未使用的数据移除。      




