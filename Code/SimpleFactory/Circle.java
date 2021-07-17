/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 10:06:38
 * @LastEditTime: 2020-03-25 10:06:38
 * @LastEditors: Yintianhao
 * @Description: 圆形类
 * @FilePath: \TechNote\src\Code\SimpleFactory\Circle.java
 * @Copyright@Yintianhao
 */
package SimpleFactory;
public class Circle implements Shape{
    public Circle(){
        System.out.println("Construct a Circle");
    }
    public void draw(){
        System.out.println("Draw Circle");
    }
}