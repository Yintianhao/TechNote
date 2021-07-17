## 来源        
在JDK1.2之前，Java内存模型都是从主存中读取变量，是不要注意这些的，但是在这之后，线程可以把变量保存在本地内存中，
而不是直接前往主存读写，这就可能造成一个线程在主存中修改了一个变量的值，而一个线程在主存中修改了变量的值，而这个
线程却还是用着以前的变量，这就造成了数量的不一致。         

## 可见性       
volatile修饰的变量在每次被线程访问时，都强迫地从主存中重读该变量的值，并且在修改之后都将它写进主存。        
例子：

```
public class RunThread implements Runnable {
    private boolean isRunning = true;
    @Override
    public void run(){
        System.out.println("enter run");
        while(isRunning){
        }
        System.out.println("end");
    }
    public boolean isFlag(){
        return isRunning;
    }
    public void setFlag(boolean isRunning){
        this.isRunning = isRunning;
    }
    public static void main(String[] args)  {
        RunThread runThread = new RunThread();
        new Thread(runThread).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runThread.setFlag(false);
    }
}
```
运行需要实现让JVM运行在server模式下，因为在这个模式下JVM为了线程运行的效率，会一直在私有堆栈读取数值，能体现出没加volatile关键字的效果。即一直运行。      
而加上volatile关键字之后，当main线程中将isRunning修改后会将其写入主存。而在另一个线程中，因为被申明为volatile，所以会强迫该线程读取主存。     
但是在实际情况下，JVM会尽力保证变量的可见性，换个说法，只要CPU有时间，JVM就会尽力更新该数据，但是这与volatile不同，volatile是强迫性质的。         
## Volatile的非原子性       
```
public class VolatileAutomicTest extends Thread{
    volatile public static int count;
    private  static void addCount(){
        for(int i = 0;i < 100;i++){
            count++;
        }
        System.out.println("count = "+count);
    }
    @Override
    public void run(){
        addCount();
    }
}
//
public class Run {
    public static void main(String[] args)throws InterruptedException{
        VolatileAutomicTest[] arr = new VolatileAutomicTest[100];
        for(int i = 0;i<100;i++){
            arr[i] = new VolatileAutomicTest();
        }
        for (VolatileAutomicTest t:arr){
            t.start();
        }
    }
}
```
这里如果不在addCount加syn同步的话，输出的结果就会有重复，说明不具有原子性了，所以volatile只是保证变量写操作的原子性，不能保证读写操作的原子性。volatile可以禁止指令重排序，典型案例是在单例中使用。并且volatile不会导致线程上下文切换，但是会是读写变量的成本变高，因为每次都需要从高速缓存或者主内存中读取。

### volatile什么情况下可以代替锁

volatile是一种轻量级锁，适合多个线程共享一个变量的情况，可以将多个线程共享的一组状态变量合并成一个对象，用一个volatile引用这个对象，从而代替锁。

## SYN 和 Volatile比较      
```
Volatile关键字是线程同步的轻量级实现，所以volatile关键字比syn好，但是volatile仅能适用于变量而syn可以用于代码块等。
多线程访问volatile不会发生阻塞，而syn可能会阻塞。    
volatile保持可见性但不保证原子性，syn两者都保证。        
volatile用于解决线程之间变量的可见性，而syn是用于多个线程之间的访问资源的同步性。      
```
