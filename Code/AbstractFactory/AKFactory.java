/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 16:01:10
 * @LastEditTime: 2020-03-25 16:01:10
 * @LastEditors: Yintianhao
 * @Description: AK工厂
 * @FilePath: \TechNote\src\Code\AbstractFactory\AKFactory.java
 * @Copyright@Yintianhao
 */
package AbstractFactory;
public class AKFactory implements Factory{

    @Override
    public Gun produceGun(){
        return new AK();
    }
    @Override
    public Bullet produceBullet(){
        return new AK_Bullet();
    }
}