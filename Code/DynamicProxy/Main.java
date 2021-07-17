/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:54:17
 * @LastEditTime: 2020-03-25 16:57:21
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\DynamicProxy\Main.java
 * @Copyright@Yintianhao
 */
package DynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Main{
    public static void main(String[]  args){
        Maotai maotai =  new Maotai();
        InvocationHandler invocationHandler = new GuitaiA(maotai);
        SellWine dynamicProxy = (SellWine)Proxy.newProxyInstance(Maotai.class.getClassLoader(), Maotai.class.getInterfaces(), invocationHandler);
        dynamicProxy.maiJiu();
    }
}