/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:48:34
 * @LastEditTime: 2020-03-25 16:48:35
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\DynamicProxy\Maotai.java
 * @Copyright@Yintianhao
 */
package DynamicProxy;
public class Maotai implements SellWine{
    @Override
    public void maiJiu(){
        System.out.println("我卖茅台");
    }
}