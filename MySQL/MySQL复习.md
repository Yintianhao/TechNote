## MySQL

### MyISAM和InnoDB的区别

1，MyISAM只支持表级锁，而InnoDB支持表级锁和行级锁，默认是行级锁。

2，MyISAM不支持事务，MyISAM强调性能，每一次查询具有原子性，执行速度比InnoDB快，而InnoDB支持事务。

3，MyISAM不支持外键，InnoDB支持

4，是否支持MVCC，MyISAM不支持，InnoDB支持

### 索引

1，MyISAM，B+Tree索引的叶子结点的data域只保存数据记录的地址，查询记录的时候，先查询到响应的叶节点取出数据的地址，然后读取地址的响应记录，它的数据文件和索引文件是分开的。

2，InnoDB，其数据文件本身就是索引文件，叶节点data域保存的是完整的数据记录，而这个索引的key就是数据表的主键，而其他的索引都只作为辅助索引，辅助索引的data域存的是响应记录的主键的值，在根据辅助索引查询记录的时候，是先查询辅助索引找到主键的值，然后再根据主键的值在主键索引里查询到记录。所以不宜使用太长的字段作为主键，也不建议使用非单调的字段作为主键，否则容易造成主索引频繁分裂。

### ACID

A，原子性，事务是最小的执行单位，不可分割。

C，一致性，执行事务前后，数据保持一致。多个事务对同一条数据读取的结果是相同的，

I，隔离性，并发访问数据的时候，一个用户的事务不被其他事务所打扰，并发事务之间数据库是独立的。

D，持久性，一个事务被提交之后，在数据库里的改变是持久的，及时数据库发生故障，也不应该对其有任何影响。

### 并发事务带来的问题

- 脏读 

当换一个事务正在访问数据的时候对数据进行了修改，而这种修改还没有提交到数据库中，这时候另一个事务也访问到了这个数据，然后使用了这个数据，这个数据是还没有提交的数据，那么另一个事务读到的数据就是脏数据，依据脏数据所做的操作可能是不正确的。

- 丢失修改

一个事务读取一个数据的时候，另一个事务也访问了这个数据，在第一个事务中修改了这个数据后，第二个事务也修改了这个数据，那么第一个事务修改的结果就丢失了。

- 不可重复读

一个事务内多次读写同一个数据，在这个事务还没有结束的时候，另一个事务也在访问这个数据，那么第一个事务的几次读中就可能出现某一次数据不一致的情况。

- 幻读

幻读和不可重复读类似，它发生在一个事务读取了几行数据，接着另一个并发事务插入了一些数据，在随后的查询中，第一个事务中就出现了多次读中就会出现其中一次读取到了原本不存在的记录的情况。

### 隔离级别

- 读取未提交 最低的隔离级别，允许读取尚未提交的数据变更
- 读取已提交 允许读取并发事务已经提交的数据，可以阻止脏读，但是幻读和不可重复读仍然有可能发生
- 可重复读 对同一个字段的多次读取的结果都是一致的，除非数据是本身事务所修改，可以阻止脏读和不可重复读，但是幻读仍然是有可能发生的。
- 可串行化，最高的隔离级别，完全服从ACID的隔离界别，所以事务逐个执行，脏读和不可重复读幻读都可以避免。



### binlog，redolog

在mysql的使用中，我们的更新操作并不是直接更新到磁盘的，这样的话io操作会很高，更新操作会先写日志，在合适的时间才会写进磁盘，日志更新完毕就将执行结果返回给客户端。

- redolog

是InnoDB引擎独有的日志模块，redolog是物理日志，记录了某个数据页上有哪些修改，InnoDB的redolog固定大小的。InnoDB的redolog保证了数据库在发生异常重启之后，之前提交的记录不会丢失，这个能力也叫crash-safe。

- binlog

binlog是server层自带的日志模块， binlog是逻辑日志，记录本次修改的原始逻辑，说白了就是sql语句，binlog是追加写的形式，可以写多个文件，不会覆盖之前的日志。

```
格式：
statement，记录完整的SQL，优点是日志文件小，性能较好，缺点也很明显，准确性差，遇到sql语句中有now()等函数就会不准确。
row，记录数据行的实际数据的变更，优点是记录数据准确，缺点是日志文件较大。
mixed，前两者混合模式。
```

### 为什么mysql会突然慢一下

当内存数据页和磁盘数据页内容不一致的时候，这个内存页就是脏页，内存数据写到磁盘之后，数据一致了，这个时候内存页才是干净页，更新数据库的时候回先写日志，那么当redolog写满了，就要flush脏页，也就是把数据写到磁盘中，这个时候就会突然慢一下。

