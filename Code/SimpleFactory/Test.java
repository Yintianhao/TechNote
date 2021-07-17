/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 10:05:29
 * @LastEditTime: 2020-03-25 10:16:46
 * @LastEditors: Yintianhao
 * @Description: 测试类
 * @FilePath: \TechNote\src\Code\SimpleFactory\Test.java
 * @Copyright@Yintianhao
 */
package SimpleFactory;
public class Test{
    public static void main(String[] args){
        Circle circle = (Circle) ShapeFactory.getClass(Circle.class);
        circle.draw();
        Rectangle rectangle = (Rectangle) ShapeFactory.getClass(Rectangle.class);
        rectangle.draw();
    }
}
