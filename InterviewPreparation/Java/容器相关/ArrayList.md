## ArrayList

直接看类源码：

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

大体上可以看出，底层数据结构是数组，默认大小是10。

### 扩容过程

从add方法开始看：

```
  public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
```

可以看到第一个方法是用来确保容量足够的，这里面肯定有扩容的逻辑。继续往下看

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

然后又是一个方法，首先这里面比较了本身是不是初始的空数组，找出了默认的容量和传进来的参数中比较大的。然后再往下看，着重看grow方法了。

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

所以扩容是扩容为原来的1.5倍，最终变成int的最大值。同时也可以看出，这里是Arrays.copyOf来进行操作，所以代价比较大。使用ArrayList的时候最好一开始就指定大小。

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

可以看到是将删除之后的元素往前推一步，这里也可以看出开销是非常大的。





