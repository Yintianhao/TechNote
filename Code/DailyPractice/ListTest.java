package DailyPractice;
import java.util.ArrayList;
import java.util.HashSet;

/*
 * @Author: Yintianhao
 * @Date: 2020-03-25 01:25:19
 * @LastEditTime: 2020-03-25 01:28:44
 * @LastEditors: Yintianhao
 * @Description: List的去重API
 * @FilePath: \TechNote\src\Code\ListTest.java
 * @Copyright@Yintianhao
 */
public class ListTest{
    public static void main(String[] args){
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);
        list.add(3);
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.add(1);
        list.retainAll(list2);
        for(int i:list){
            System.out.println(i);
        }
        HashSet<Character> set = new HashSet<>();
        set.contains('c');
        
    }
}