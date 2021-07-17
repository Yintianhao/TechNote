## 事务

### @Transactional

可以作用于接口，类，类方法

- 作用于类，表示该类的public方法都配置相同的事务属性信息
- 作用于方法，当类配置了@Transactional，方法也配置了@Transactional，方法的事务会覆盖类的事务配置信息
- 作用于接口，不推荐这样使用，这样如果配置了SpringAOP使用CGLIB动态代理，会导致注解失效

属性：

- propagation，代表事务的传播行为，默认值是Propagation.REQUIRED
  - REQUIRED 如果当前存在事务，则加入该事务，如果不存在当前事务，则创建新的事务。即，如果A和B方法都添加了注解，在A中调用了B，那么会把这两个方法的事务合并为一个事务。
  - SUPPORTS  如果当前存在事务，则加入该事务，如果不存在事务，则以非事务的方式继续运行。
  - MANDATORY，如果当前存在事务，则加入该事务，如果当前不存在事务，则抛出异常。
  - REQUIRES_NEW，重新创建一个新的事务，如果当前存在事务，暂停当前的事务。当类A中的a方法用默认模式，类B中的b方法采用了这种模式，然后在a中调用了b，然而加入a抛异常后，b方法并没有进行回滚，因为这种模式会暂停a方法的事务。
  - NOT_SUPPORTED，以非事务的方式运行，如果当前存在事务，暂停当前事务。
  - NERVER，以非事务的方式运行，如果当前存在事务，则抛出异常。
  - NESTED，和默认效果一样。

rollbackFor属性，用于指定能够触发事务回滚的异常类型，可以指定多个异常类型。

### 注意点

1，@Transactional应用在非public方法上，会失效。

2，@Transactional注解属性rollbackFor设置错误，rollbackFor可以指定能够触发事务回滚的异常类型，Spring默认抛出了未检查的异常或者Error才会回滚事务，其他异常不会触发回滚，如果在事务中抛出其他类型的异常，但却期望这个时候可以回滚事务，就需要指定rollbackFor属性

3，同一个类中方法调用，导致@Transactional失效。

方法A调用B，A没有注解，但B有，那么外接调用A的时候，注解是不会生效的，这是因为SpringAOP代理造成的，因为只有在当事务方法被当前类以外的代码调用的时候，才会由Spring生成的代理对象来管理。

4，异常被catch吃了导致注解失效，这个比较好理解。

```
    @Transactional
    private Integer A() throws Exception {
        int insert = 0;
        try {
            CityInfoDict cityInfoDict = new CityInfoDict();
            cityInfoDict.setCityName("2");
            cityInfoDict.setParentCityId(2);
            /**
             * A 插入字段为 2的数据
             */
            insert = cityInfoDictMapper.insert(cityInfoDict);
            /**
             * B 插入字段为 3的数据
             */
            b.insertB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

