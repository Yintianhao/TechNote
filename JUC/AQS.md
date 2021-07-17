

## AbstractQueuedSynchronizer        

AQS 是用来构建锁或者其他同步组件的基础框架，使用一个int成员来表示同步状态，通过内置的FIFO队列来完成资源获取的排队工作。         
AQS的核心思想是，如果请求的共享资源空闲，那么将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态，如果被请求的资源被占用，那么就需要一套线程阻塞等待以及被唤醒时的锁分配机制，这个机制AQS是用CLH(Craig,Lantin and Hagersten)队列锁实现的，即将暂时获取不到锁的线程加入到这队列中。

主要使用方式是继承，需要三个方法对同步状态进行安全地更改：     
```
getState():获取同步状态     
setState(int newState);设置当前的同步状态
compareAndSetSate(int expect,int update);使用CAS来设置当前状态。此过程保持原子性。
```
其他可重写的方法：     
```
protected boolean tryAcquire(int arg);独占的获取同步状态，先查询同步状态是否符合预期然后采用CAS设置。
protected boolean tryRelease(int arg);独占的释放同步状态
protected int tryAcquireShared(int arg);共享式的获取同步状态，>=0表示获取成功，否则表示失败。
protected boolean tryReleaseShared(int arg);v共享式释放。
protected boolean isHeldExclusive();是否当前线程被独占。
```
同步器的工作原理：       
重要组件：同步队列，独占式同步状态获取与释放，共享式同步状态获取与释放以及超时获取同步状态等模板方法。       
### 同步队列      
FIFO双向队列，当前线程获取状态失败时，会将当前线程以及等待状态信息构造成一个结点并加入同步队列，同时阻塞该线程，同步状态释放后，会把首节点线程唤醒，使其再次获取状态。       
该队列是线程安全的。
### 独占式状态的获取与释放    
首先调用自定义同步器的tryAcquire，该方法保证线程安全的获取同步状态，如果获取失败，则构造同步结点然后加入队列尾部。        
结点进入队列之后进入自旋的过程，当条件满足时，获取到了同步状态，就可以从自旋的过程中退出，否则仍旧自旋。        
### 共享式和独占式的区别     
区别在于同一时刻能否有多个线程同时获取同步状态。        
独占：只有一个线程在能执行，如ReentrantLock。       
共享，多个线程可以同时执行，如Semaphore，CountDownLatch，CyclicBarrier，ReadAndWrite等。      
### 共享式同步状态获取和释放        
调用tryAcquireShared时会尝试获取同步状态，当返回值大于等于0，表示可以获取，成功获取同步状态并退出自旋的条件就是其返回值大于等于0。释放同步状态后，将会唤醒等待的结点，
但是和独占式不同的是，共享式必须用循环和CAS来保证资源安全释放，因为共享式释放同步状态的操作会来自于多个线程。          
### CountDownLatch          
用来控制一个或者多个线程等待多个线程。       
维护一个计数器，每次调用countDown()来让计数器减一，减到0的时候，那些因为调用await方法而在等待的线程就会被唤醒。        
```
public class CountDownLatchTest {
    public static void main(String[] args)throws InterruptedException{

        final int num = 10;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 0;i < num;i++){
            service.execute(()->{
                System.out.println("run..");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("end");
        service.shutdown();

    }
}
```
CountdownLatch的await方法会阻塞当前线程，直至为0，传入的N可以看做是N个线程也可以看做是N个步骤，只有等待这么多个步骤/线程完成。

### 同步屏障        
它的作用是让一组线程达到一个屏障的时候被阻塞。直到最后一个线程达到屏障时，屏障才会开放，所有被屏障拦截的线程才会运行。     
实现上，它与CountDownLatch类似，都是维护计数器，线层执行await就会使得减一，并且开始等待，直至计数器为0，所有调用了await方法的线程才会继续执行。      
区别是，同步屏障可以通过reset循环使用。          
```
//第二个参数表示线程达到后将执行的动作。
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    public CyclicBarrier(int parties) {
        this(parties, null);
    }

```
```
public class CyclicBarrierTest {
    public static void main(String[] args){
        final int num = 10;
        CyclicBarrier barrier = new CyclicBarrier(num, new Runnable() {
            @Override
            public void run() {
                System.out.println("arrived..");
            }
        });
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 0;i < num;i++){
            service.execute(()->{
                System.out.println("before..");
                try {
                    barrier.await();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }catch (BrokenBarrierException e){
                    e.printStackTrace();
                }
                System.out.println("after..");
            });
        }
        service.shutdown();
    }
}

```
### Semaphore           
类似于信号量，可以控制互斥资源的访问线程数，以下模拟一个对某个服务的并发请求，每次只能有三个请求同时访问。         
```
public class SemaphoreTest {
    public static void main(String[] args){
        final int clients = 3;
        final int totalRequests = 10;
        Semaphore semaphore = new Semaphore(clients);
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 0;i < totalRequests;i++){
            service.execute(()->{
                try {
                    semaphore.acquire();
                    System.out.println("可用余量："+semaphore.availablePermits());
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            });
        }
        service.shutdown();
    }
}

```



