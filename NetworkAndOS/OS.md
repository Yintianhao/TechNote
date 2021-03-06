## 计算机系统的构成
### 硬件
包括CPU，储存器（内存，外存），输入设备，输出设备       
### 软件
包括系统软件（操作系统，编译程序，DBMS等），工具软件（软硬件检测诊断程序），应用软件。
### 什么是操作系统
紧贴在硬件之上，所有软件之下。          



#### 引入操作系统的目标            
1，有效性，系统管理员的观点，管理和分配硬件，软件资源，合理地组织计算机的工作流程           
2，方便性，用户的观点，提供良好的一致的用户接口，弥补硬件系统的类型和数量差别。         
3，可扩充性，硬件类型和规模，操作系统本身的功能和管理策略，多个系统间的资源共享和互操作。       

### 作用
#### 1 从资源管理角度
1，跟踪资源状态。       
2，分配资源。       
3，回收资源。           
4，保护资源。       
#### 计算机资源的划分
1，处理器。2，存储器。3，IO设备。4，信息(程序和数据)        
相应地可将操作系统分为四类管理器：      
处理器管理，存储管理，设备管理，文件系统。      
#### 2 从软件分层，扩充机器的角度
提供硬件的高层界面，屏蔽硬件限制。          
#### 3，从服务用户的角度
为方便用户使用计算机提供了二级访问接口：        
1，命令接口：命令行，菜单式，命令脚本，GUI。        
2，调用接口，类似于过程调动，在应用编程中使用。         
3，图形接口，GUI        
### 定义
控制和管理计算机系统的硬件和软件资源，合理的组织计算机工作了流程以及方便用户使用的程序和数据的集合。        
### 发展
#### 1，手工操作
特点：用户独占全机，CPU等待用户。       
矛盾：低效率，用户独占全机所有资源。        
#### 2，单道批处理
特点：用磁带将若干个作业分类变成作业执行序列，每个批作业都有一个监督程序自动依次处理。      
分类：      
1,联机批处理：用户提交作业--操作员合成批作业--批作业处理。          
存在的问题：慢速的输入输出处理仍直接由主机来完成，输入输出时，CPU处于等待。         
2，脱机批处理：卫星机完成输入输出，主机和卫星机并行工作。           
优点：改善CPU和io设备的使用效率，提高了吞吐量。     
缺点：磁盘需要人工装卸，作业需要人工分类，监督程序容易遭到破坏。        
矛盾：一个用户独占CPU，CPU和io速度不匹配。资源利用率低。
#### 多道批处理系统
在计算机内存中同时存放多道相互独立的程序，它们在管理程序的控制下相互穿插的进行，共享CPU和外设等资源，采用多道程序设计技术的批处理系统称为多道批处理系统。           
实现：          
1，存储保护和重定位，在多道程序设计环境中，几道程序共享同一内存，硬件必须提供必要的手段保证各个程序之间不受侵犯。           
2，处理机管理和调度，多道程序共享同一个处理器，因此存在处理机的调度问题。           
3，资源的管理和调度，多道程序共享资源，同样存在资源的分配问题。         
##### 多批次的特点
多道：同时存在两道或者两道以上的程序处于执行的开始点和结束点。      
宏观并行，都处于运行状态，但都未运行完。            
微观串行，各作业交替使用CPU。           
优点：资源利用率高，吞吐量大        
缺点：用户交互性差，整个作业完成后或者中间出错时，才与用户交互，不利于调试和修改。          
作业平均周期时间长。
**核心技术**            
-作业调度：作业的现场保存和恢复。           
-资源共享：互斥机制。       
-内存使用：覆盖，交换，虚拟存储。           
-文件非顺序存放，随机存储。         

### 中断和通道      
#### 中断   
指CPU在收到外部中断信号之后，停止原来的工作，然后去处理这个中断时间，完成之后再回到断点继续工作。       
#### 过程
中断请求--中断响应--中断点暂停当前任务保存现场--中断处理例程--中断返回恢复现场继续任务。            
#### 分类
硬件中断：硬件故障中断，IO中断，外部中断。          
软件中断：程序中断，如地址越界等，访管中断。            
#### 通道
实际上是一台功能单一，结构简单的io处理器，单独与CPU，并直接控制外部设备，与内存进行数据传输。       
-通道有自己的io处理器，可以与CPU并行工作，通道具有自己的指令，可编程实现各种复杂的io处理。       
-可实现io联机处理。         
#### CPU和通道之间的通讯
CPU向通道发出io指令，通道执行通道程序进行io操作，io完成或者出错时，以中断的方式请求CPU处理。            

**进程线程的概念和区别**  
1 进程 资源分配的基本单位
跟进程有关的资源都存在PCB里面，操作进程实质就是对PCB进行操作，几个进程可以并发运行
2 线程
是独立调度的基本单位，一个进程可以有多个线程，这多个线程共享进程资源    
区别    
1 拥有资源，进程拥有资源，是资源分配的基本单位，线程不拥有，线程可以访问隶属进程的资源    
2 线程是独立调度的基本单位，同一进程中，线程的切换不回引起进程切换，但是从一个进程的线程切换到另一个进程的线程会   
3系统开销，进程开销大，线程小   
4通信，线程间可以直接读写同一个进程中的信息进行通信，进程通信需要IPC（Inter-Process Communication）   
通常一个进程可以有多个线程，一个线程只属于一个进程，进程之间相互独立

**关于PCB**   
进程由程序代码，数据，PCB组成，即进程映像
PCB描述进程的基本信息和运行状态，PCB是一种数据结构，常驻内存，其中PID唯一标识进程，    
处理器状态，主要是由处理器各个寄存器的内容组成，包括通用寄存器，程序计数器，存储下一条指令的地址：程序状态字，
用户栈指针，存放过程与系统调用的参数和调用地址   
进程调度信息：包括进程状态，优先级，事件    
进程控制信息：包括程序和数据的地址，进程的程序和数据所在的内存或者外存地址。进程同步和通信机制，指实现进程同步和通信的机制，
资源清单，进程所需的全部资源及已经分配到该进程的资源的清单   

**PCB的组织形式**    
链接和索引。    
链接指运行，就绪，阻塞三态分别维护一个链表，每种状态的PCB 通过链表链接，其中就绪态的链表只有一个PCB   
索引：   
三态分别维护一个PCB表。表中的每一个entry指向一个PCB   

**进程的创建(由顶层到底层)**   
--用户的API，fork(),vfork(),pthread_create()    
--系统调用，fork(),vfork(),clone()   
以上是用户空间   
以下是内核空间   
--系统调用处理函数:system_call()    
--系统调用服务例程:sys_fork,sys_vfork,sys_clone   
--do_fork()--copy_process()     
特点：   
1，新创建的子进程和父进程拥有相同的代码，代码段的内容从fork()函数之后开始的   
2，如果子进程想执行不同的程序，则需要调用exec函数族来覆盖进程映像，它的PID不改变    
3，fork()调用失败返回-1，成功父进程中返回子进程的PID，子进程中返回0    
4，父子进程拥有相同的代码段，但是会打印不同的信息     
5，子进程通过getpid()返回自己的PID，获得子进程PID必须使用变量保存fork()函数的返回值    

**fork()**
传统的fork,子进程复制父进程的虚拟空间，共享代码段，数据段，栈，堆都分配新的物理内存，出了PID和PCB不同，子进程是父进程的精准复制     
linux的fork，写时复制，内核一开始只为子进程复制父进程的虚拟空间，并不分配物理内存，当自己成想修改物理内存时，再为子进程分配单独的物理内存    
fork出的子线程和父线程无关，运行先后顺序不固定。

**进程终止**    
正常结束：exit,halt,logoff   
异常结束：无可用内存，越界，保护错误，算术错误等    

**上下文切换**   
1，保存当前进程上下文   
2，选择某个进程，恢复其上一次换出时保存的上下文    
3，当前进程将控制权给新的进程   

**进程状态切换**  
就绪--运行--阻塞
就绪运行可以互相转化
阻塞由缺少需要资源的运行状态转化而来

**线程状态的切换**

**调度算法：**   
1***批处理系统：***
1 先来先服务
2 短作业优先
3 最短剩余时间 按照剩余运行时间顺序进行调用

2***交互式系统：***
1时间片轮转：
按FCFS的原则排成一个队列，每次调度时，把CPU时间分配给队首，执行完发出中断，将它送往队尾，再讲CPU时间分配给队首
2优先级
每个进程设置优先级
3多级反馈队列

**进程同步**        
***临界区***                   
将访问临界资源的代码分为四个区：进入区，临界区，退出区，剩余区         
进入区：在进入区进行检查，并且设置访问临界区的标志，阻止其他进程进如临界区。      
临界区：访问临界资源的代码，称之为临界区        
退出区：将访问标志清除。        
剩余区：代码的其他部分。                

为了互斥进行访问临界资源，每个进程进入临界区之前，先检查        
同步：多个线程因合作产生直接制约关系，使得进程有一定的先后执行关系       
互斥：多个进程在同一时刻在只有一个进程进入临界区        

同步进程应该遵循：       
空闲让进。忙则等待，有限等待，让权等待。        
**软件同步机制**         
***软件***        
单标志法:设置一个公用的整型变量。       
双标志法先检查：        
在每一个进程访问临界区资源之前，先查看临界资源是否正在被访问，是则等待，否则进入临界区。        
双标志法后检查，先检测进程状态标志，再置自己的标志，再检测对方的标志，如果对方标志为true，则等待，否则进入临界区。     
Peterson算法：     
设置变量turn表示不允许进入临界区的进程编号，每个进程在设置自己的标志后再设置turn标志，不允许另一个进程再进入。此时再同事检测另一个进程的状态标志和turn，这样就可以保证两个进程同事要求进入临界区而只允许一个进入临界区。          

**信号量：**
一个整型变量，可以对其进行down、up操作      
down:如果>0，减一，等于0进程睡眠        
up，加一操作，唤醒线程        

这两个操作都是原子操作，通常要设置屏蔽中断       


**消费者--生产者问题：**
```
#define N 100
typedef int semaphore;
semaphore mutex = 1;
semaphore empty = N;
semaphore full = 0;

void producer() {
    while(TRUE) {
        int item = produce_item();
        down(&empty);
        down(&mutex);
        insert_item(item);
        up(&mutex);
        up(&full);
    }
}

void consumer() {
    while(TRUE) {
        down(&full);
        down(&mutex);
        int item = remove_item();
        consume_item(item);
        up(&mutex);
        up(&empty);
    }
}
```
**管程机制：**       
引入目的：使用信号量时，每个访问临界资源的进程都必须同时自备同步操作，使得大量的同步操作分散在各个进程中，为了解决因为同步操作而导致死锁，使用管程来使得进程同步。       
**管程特性**        
局部于管程的数据只能被局部于管程内的进程所访问。        
一个进程只有通过调用管程内的过程才能进入管程访问共享数据。       
每次仅允许一个进程在管程内执行某一个内部过程，管程的互斥由编译器自动添加。       

**进程通信：**
1 管道：       
特点：半双工，反向交替
只能在父子进程或兄弟进程中使用    
不属于文件系统，只存在于内存中     
2 FIFO（命名管道）      
比起管道，去除了父子进程的限制     
与路径名相关联，存储在文件系统中        
3 消息队列：     
独立于读写进程而存在，进程终止消息队列及其内容并不会被删除 避免了 FIFO中同步管道开关的困难       
避免了FIFO的同步阻塞问题，读进程可以根据消息类型有选择的接收信息      
4 信号量       
用于进程间同步，基于PV操作      
5 共享存储      
最快的IPC，因为进程可以直接对内存进行读取。··
多个进程可以同时操作。
6 套接字       

**虚拟内存**        
将内存抽象成地址空间，每个程序拥有自己的地址空间，地址空间被分做许多块，每一块称之为一页。页被映射到物理内存，但不一定是连续的物理内存，也不需要所有页都必须在物理内存中，虚拟内存允许程序不用将地址空间全部映射到内存就可以运行。       
**分页系统地址映射**            
页表存储程序地址空间和物理内存空间，即页和页框。根据页得到页号，然后得到页表项，页表项最后一位表示是否在内存中。后面的位表示偏移量。      
**几种页面置换算法**        
1，最佳，即选择最长时间不被访问的页，通常可以保证最低的缺页率。        
2，LRU，LRU将最近最久未使用的页面换出，实现：维护一个所有页面的链表，一个页面被访问时，移动到表头，保证表尾是最久未使用。        
3，最近未使用，页面有两个状态位，一个R一个M，R=1表示被访问，M=1表示被修改，发生缺页中断时，随机从类编号最小的非空类中选出一个页面换出。优先换出脏页面，即R=0M=1的页面，而不是频繁使用的界面，R=1M=0        
4，先进先出  
5，第二次机会     
在先进先出的基础上，如果该页面R是0，则换掉，如果R是1，则将R清零 。        
6，时钟        
使用一个环形链表将页面连接起来，使用指针指向最老界面。     

**磁盘调度**        
1，FCFS      
2，最短寻道时间优先。通常两端的磁道请求容易出现饥饿现象。       
3，SCAN，先按一个方向进行调度，直到这个方向上没有磁盘请求。接着反过来。      

