## LinkedList        
使用双向链表实现，结点结构为：     
```
private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
```   
每一个LinkedList对象都有指向第一个和最后一个结点的指针：      
```
    transient int size = 0;

    transient Node<E> first;

    transient Node<E> last;

```
add()     
```
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }
```
remove,跟双链表原理一样，并且支持null对象
```
public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
```
使用场景：       
```
ArrayList跟LinkedList的区别就是数组和双向链表的区别。
ArrayList在随机访问方面比较擅长，LinkedList在随机增删方面比较擅长
对于需要快速插入，删除元素，使用LinkedList。因为ArrayList要想在数组中任意两个元素中间添加对象时，数组需要移动所有后面的对象。
对于需要快速随机访问元素（get()），应该使用ArrayList，因为LinkedList要移动指针、遍历节点来定位，所以速度慢。
对于“单线程环境” 或者 “多线程环境，但List仅仅只会被单个线程操作”，此时应该使用非同步的类(如ArrayList)。
对于“多线程环境，且List可能同时被多个线程操作”，此时，应该使用同步的类(如Vector)。
```
