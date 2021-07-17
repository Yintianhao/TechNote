package JZOffer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yintianhao
 * @createTime 02 16:14
 * @description
 */
public class Solution48 {
    public int Add(int num1,int num2){
        int sum = 0;
        int delta = 0;
        while(num2!=0){
            sum = num1^num2;
            delta = (num1&num2)<<1;
            num1 = sum;
            num2 = delta;
            HashMap<String,String> map = new HashMap<>();
            map.put("k1","v1");
            map.put("k2","v2");
            map.put("k3","v3");
        }
        return sum;
    }
}
