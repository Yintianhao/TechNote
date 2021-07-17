/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 09:57:02
 * @LastEditTime: 2020-03-25 09:57:56
 * @LastEditors: Yintianhao
 * @Description:
 * @FilePath: \TechNote\src\Code\SimpleFactory\ShapeFactory.java
 * @Copyright@Yintianhao
 */
package SimpleFactory;
public class ShapeFactory{
    public static Object getClass(Class<? extends Shape> clazz){
        Object obj = null;
        try{
            obj = Class.forName(clazz.getName()).newInstance();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }catch(InstantiationException e){
            e.printStackTrace();
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
        return obj;
    }

}