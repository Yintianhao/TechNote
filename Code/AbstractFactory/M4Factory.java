/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:05:35
 * @LastEditTime: 2020-03-25 16:05:36
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\AbstractFactory\M4Factory.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class M4Factory implements Factory{
    @Override
    public Gun produceGun(){
        return new M4A1();
    }
    @Override
    public Bullet produceBullet(){
        return new M4_Bullet();
    }
}