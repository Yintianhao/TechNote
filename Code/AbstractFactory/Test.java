/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:06:48
 * @LastEditTime: 2020-03-25 16:08:52
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\AbstractFactory\Test.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class Test{
    public static void main(String[] args){
        Factory factory = new AKFactory();
        Gun gun = factory.produceGun();
        Bullet bullet = factory.produceBullet();
        bullet.load();
        gun.shoot();
    }
}