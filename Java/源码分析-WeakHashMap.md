## WeakHashMap       
顾名思义，跟虚引用有关，也就是在下一次GC的时候会被回收。       
WeakHashMap主要用来实现缓存，通过WeakHashMap来引用缓存对象，由JVM来对缓存进行回收。        
## 重要部分--ConcurrentCache     
Tomcat中的ConcurrentCache使用了WeakHashMap来实现缓存功能      
ConcurrentCache采取的是分代缓存的策略。     
1，经常使用的对象放在Eden中，Eden使用ConcurrentHash实现。      
2，不常用的对象放入longterm中，使用WeakHashMap实现，这些老对象会被GC。      
3，调用get时，先从Eden取，没有找到再到longterm找，当从longterm获取到就移至Eden，从而保证经常访问的结点不容易被回收。        
4，调用put时，如果Eden超过了size，就想Eden所有对象存入longterm，利用VM会受到一部分不常使用的对象。      
