/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:49:44
 * @LastEditTime: 2020-03-25 16:49:44
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\DynamicProxy\GuitaiA.java
 * @Copyright@Yintianhao
 */
package DynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class GuitaiA implements InvocationHandler{
    private Object pingpai;
    public GuitaiA(Object pingpai){
        this.pingpai = pingpai;
    }
    @Override
    public Object invoke(Object proxy,Method method,Object[] args) throws Throwable{
        System.out.println("销售开始，柜台是"+this.getClass().getSimpleName());
        method.invoke(pingpai, args);
        System.out.println("销售结束");
        return null;
    }
}