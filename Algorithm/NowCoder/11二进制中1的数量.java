/**
输入一个整数，输出该数二进制表示中1的个数。其中负数用补码表示。
正数补码为本身,负数为2^n+本身
*/
public class Solution {
    public int NumberOf1(int target) {
        int count = 0;
        long tmp = 0;
        long max = 4294967296L;
        if(target==0)
            return 0;
        if (target>0){
            tmp = target;
        }else {
            tmp = max + target;
        }
        while (tmp>0){
            if (tmp%2==1)
                count++;
            tmp = tmp>>1;
        }
        return count;
    }
   
}
