/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 15:56:46
 * @LastEditTime: 2020-03-25 15:56:46
 * @LastEditors: Yintianhao
 * @Description: AK 子弹类
 * @FilePath: \TechNote\src\Code\AbstractFactory\AK_Bullet.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class AK_Bullet implements Bullet{
    @Override
    public void load(){
        System.out.println("AK_BULLET load");
    }
}