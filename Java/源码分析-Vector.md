## Vector        
相比于ArrayList，它里面为add参数加上了synchronized进行同步。      
类成员
```
    //存放数据的数组
    protected Object[] elementData;
    //当做元素的个数
    protected int elementCount;
    //扩容系数，为0时，每次扩容翻倍
    protected int capacityIncrement;
```       
构造方法：       
```
public Vector(int initialCapacity, int capacityIncrement) {
        super();
        //容量必须大于0
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }
    public Vector(int initialCapacity) {
        //扩容系数为0
        this(initialCapacity, 0);
    }
    public Vector() {
        //默认大小10
        this(10);
    }

```     
## 扩容      
跟ArrayList类似，
```
private void ensureCapacityHelper(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```       
## 与ArrayList比较      
Vector采用同步方法，开销比ArrayList大，访问速度要慢。
Vector每次扩容扩到原来的两倍。而ArrayList是1.5倍。        
## 替代方案        
Collection.synchronizedList或者并发包下的CopyOnWriteArrayList        
### CopyOnWriteArrayList     
具有读写分离的特点，写在一个复制的操作上进行，读在原始数组上进行，读写分离，互不影响。       
写操作加锁，防止多线程下导入数据写入错误。       
写操作结束之后仍旧需要将复制的数组写入原数组。     
```//添加元素
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        //上锁
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
        //释放锁
            lock.unlock();
        }
    }
        final Object[] getArray() {
        return array;
    }

    /**
     * Sets the array.
     */
    final void setArray(Object[] a) {
        array = a;
    }
```
```
    final Object[] getArray() {
        return array;
    }
```
## 适应场景      
适合读多写少的情景，但是缺点在于，写操作需要重新复制一个新的数组，使得内存占用达到原来的两倍左右。同时还有数据不一致的问题，读操作不能读取实时写入的数据。读的同时写得数据可能并没有写入数组。
          
