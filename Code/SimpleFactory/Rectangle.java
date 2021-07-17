/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 10:13:02
 * @LastEditTime: 2020-03-25 10:13:03
 * @LastEditors: Yintianhao
 * @Description: 长方体
 * @FilePath: \TechNote\src\Code\SimpleFactory\Rectangle.java
 * @Copyright@Yintianhao
 */
package SimpleFactory;
public class Rectangle implements Shape{
    public Rectangle(){
        System.out.println("Construct a rectangle");
    }
    public void draw(){
        System.out.println("Draw a rectangle");
    }
}
