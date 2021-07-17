### ThreadLocal

- 内部实现

每个线程内部维护一个类似于HashMap的对象，称为ThreadLocalMap，里面包含若干个Entry键值对。Entry的key是一个ThreadLocal实例，value是一个线程特有的对象，entry的作用是为其所属线程建立起一个ThreadLocal实例与一个线程特有对象之间对应关系。entry对key的引用是弱引用，entry对value的引用是强引用。

```
ThreadLocal里面的map解决哈希冲突使用的使用的是开放地址法，hashmap里面则使用的是链地址法。
```



### Atomic类

内部使用CAS来保证原子性。

