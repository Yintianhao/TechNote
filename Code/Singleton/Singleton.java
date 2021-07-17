/*
 * @Author: Yintianhao
 * @Date: 2020-03-24 21:26:22
 * @LastEditTime: 2020-03-24 21:26:26
 * @LastEditors: Yintianhao
 * @Description: 单例模式 双重锁校验
 * @FilePath: \TechNote\src\Code\Singleton.java
 * @Copyright@Yintianhao
 */
package Singleton;
public class Singleton{
    private volatile static Singleton uniqueInstance;
    private Singleton(){}
    public static Singleton getUniqueInstance(){
        if(uniqueInstance==null){
            synchronized(Singleton.class){
                if(uniqueInstance==null){
                    return uniqueInstance = new Singleton();
                }
            }
        }
        return uniqueInstance;
    }
}