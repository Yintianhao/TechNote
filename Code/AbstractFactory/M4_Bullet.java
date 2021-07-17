/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 15:58:13
 * @LastEditTime: 2020-03-25 15:58:14
 * @LastEditors: Yintianhao
 * @Description: M4子弹
 * @FilePath: \TechNote\src\Code\AbstractFactory\M4_Bullet.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class M4_Bullet implements Bullet{
    @Override
    public void load(){
        System.out.println("M4_BULLET shoot");
    }
}