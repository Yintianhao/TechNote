## FutureTask          
```
public class FutureTask<V> implements RunnableFuture<V>
```
实现了RunnableFuture接口，该接口继承于Runnable和Future     
FutureTask可以用于异步获取执行结果或取消封装这个任务的场景，当一个计算任务需要执行很长时间时，那么可以用FutureTask封装这个任务，主线程在完成自己的任务再去获取结果。        
```
public class Main {
    public static void main(String[] args)throws InterruptedException,ExecutionException {
        FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int result = 0;
                for (int i =0;i < 100;i++){
                    Thread.sleep(10);
                    result+=i;
                }
                return result;
            }
        });
        Thread computeThread = new Thread(task);
        computeThread.start();
        System.out.println(task.get());
    }
}
```
## BlockingQueue       
FIFO队列，LinkedBlockingQueue，ArrayBlockingQueue（固定）         
优先级队列：PriorityBlockingQueue       
提供了阻塞的take()和put()方法，如果队列为空，take()将阻塞，直到队列中有内容，如果队列为满的put()将阻塞，直到有空闲位置。         
```
public class ProducerConsumer {

    private static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(5);

    private static class Producer extends Thread{
        @Override
        public void run(){
            try {
                queue.put("Product");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("produce...");
        }
    }
    private static class Consumer extends Thread{
        @Override
        public void run(){
            try {
                String p = queue.take();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("consume...");
        }
    }

    public static void main(String[] args){

        Producer[] producers = new Producer[10];
        for (int i = 0;i < producers.length;i++){
            producers[i] = new Producer();
        
        for (Producer p:producers){
            p.start();
        }
        Consumer[] consumers = new Consumer[5];
        for (int i = 0;i < consumers.length;i++){
            consumers[i] = new Consumer();
        }
        for (Consumer c:consumers){
            c.start();
        }
    }

}

```
## ForkJoin        
可以将大的任务拆分成多个小任务。
```
public class ForkJoinTest extends RecursiveTask<Integer> {

    private final int threshold = 5;
    private int start;
    private int end;

    public ForkJoinTest(int start,int end){
        this.start =start;
        this.end = end;
    }
    @Override
    protected Integer compute() {
        int result = 0;
        if(end-start<=threshold){
            for (int i = start;i <= end;i++){
                result+=i;
            }
        }else {
            int mid = start + (end-start)/2;
            ForkJoinTest leftTask = new ForkJoinTest(start,mid);
            ForkJoinTest rightTask = new ForkJoinTest(mid+1,end);
            leftTask.fork();
            rightTask.fork();
            result = leftTask.join()+rightTask.join();
        }
        return result;
    }
}

```
```
public static void main(String[] args)throws InterruptedException,ExecutionException {
        ForkJoinTest test = new ForkJoinTest(1,1000);
        ForkJoinPool pool = new ForkJoinPool();
        Future res = pool.submit(test);
        System.out.println(res.get());
    }
```


