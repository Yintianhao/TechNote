## Vector

相比于ArrayList，它添加元素的时候用synchronized来同步。

扩容和删除都和ArrayList类似就不再继续看了。

### Vector和ArrayList做一个比较

1，Vector采用了synchronized，开销比ArrayList大，访问速度更慢。

2，Vector每次扩容到原来点两倍，而ArrayList是扩容到1.5倍。

### 替代方案

- CopyOnWriteArrayList

  具有读写分离的特点，写在一个复制的操作上进行，读在原始数组上进行，读写分离，互不影响。

  写操作进行加锁，防止线程安全问题，写操作结束之后将复制的数组写入原数组。

  适用于读多写少的场景，但是缺点是写操作需要重新复制一个数组，内存会变大到原来的两倍，另外就是读操作不能实时读取实时写入的数据，读的同时写的数据可能没有写入数组。



