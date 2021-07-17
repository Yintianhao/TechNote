package JZOffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yintianhao
 * @createTime 23 1:42
 * @description
 */
public class Solution34 {
    public static void main(String[] args){
        String str = "google";
        System.out.print(FirstNotRepeatingChar(str));
    }
    public static int FirstNotRepeatingChar(String str) {
        /*
        * 在一个字符串(0<=字符串长度<=10000，全部由字母组成)中
        * 找到第一个只出现一次的字符,并返回它的位置, 如果没有则返回 -1（需要区分大小写）.
        * */
        HashMap<Character,Integer> map = new HashMap<>();
        for (int i = 0;i < str.length();i++){
            char c = str.charAt(i);
            if (map.get(c)==null){
                map.put(c,i);
            }else{
                map.put(c,-1);
            }
        }
        int loc = Integer.MAX_VALUE;
        Set<Map.Entry<Character,Integer>> set = map.entrySet();
        for (Map.Entry<Character,Integer> en:set){
            int t = en.getValue();
            if (t!=-1&&t<loc)
                loc = t;
        }
        if (loc<Integer.MAX_VALUE)
            return loc;
        else
            return -1;
    }
}
