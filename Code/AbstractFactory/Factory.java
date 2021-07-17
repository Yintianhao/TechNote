/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 15:59:36
 * @LastEditTime: 2020-03-25 15:59:37
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\AbstractFactory\Factory.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public interface Factory{
    Gun produceGun();
    Bullet produceBullet();
}