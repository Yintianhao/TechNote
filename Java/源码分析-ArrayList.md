 ## 源码分析       
 ### ArrayList
 ```
 public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;
     /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};
    ......
 ```
 底层是数组，默认大小为10          
 ### 扩容       
 查看原代码可以看到add函数里通过ensureCapacityInternal()来确保容量足够。 添加元素+1     
 ```
  public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
 ```
 转向ensureCapacityInternal();判断elementData是不是初始的空数组，找出默认的容量和参数两者时间较大的        
 ```
  private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

 ```
调用ensureExplicitCapacity()，如果不够，则调用grow()来扩容。     
```
private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;//扩容前的数组大小
        int newCapacity = oldCapacity + (oldCapacity >> 1);//新的数组为1.5倍。
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)//超过最大数组大小限制，给最大值给newCapacity
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
            //超过了最大就返回214783647，否则返回最大值
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```
即扩容到原来的1.5倍。从grow这里可以看出，使用了Arrays.copyOf()，代价比较大，所以使用ArrayList最好在开始使用的时候就指定大小。          
### 删除        
```
 public E remove(int index) {
        rangeCheck(index);//检查index

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;//移动的次数
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }

```
可以看到将index+1后面的元素复制到index的位置上，所以开销也是非常大的。         
### 序列化       
```
transient Object[] elementData; //transient表示数组默认不会被序列化。 
```
再看里面的几个方法：    
```
private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }
```
writeObject()和readObject()来控制只序列化填充部分的内容。         
序列化使用ObjectOutputStream的writeObject将对象转化为字节流，而writeObject的方法在传入的对象存在writeObject的时候会运用反射调用对象的writeObject方法来实现序列化。        

### Fail-List         
快速失败机制，如果在迭代的操作时，数组的大小发生了变化，即modCount != expectedCount，会抛出ConcurrentModificationException异常。        

## 总结        
 ```
 ArrayList本质上是一个数组。区别于数组的地方是可以自动扩容，关键作用是grow函数。     
 ArrayList查询相对很快，而删除较慢。
 ```
