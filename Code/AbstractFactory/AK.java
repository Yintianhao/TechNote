/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 15:04:33
 * @LastEditTime: 2020-03-25 15:04:34
 * @LastEditors: Yintianhao
 * @Description:  AK
 * @FilePath: \TechNote\src\Code\AbstractFactory\AK.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class AK implements Gun{
    @Override
    public void shoot(){
        System.out.println("AK shoot");
    }
}