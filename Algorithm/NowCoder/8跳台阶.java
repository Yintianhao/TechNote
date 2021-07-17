/**
一只青蛙一次可以跳上1级台阶，也可以跳上2级。求该青蛙跳上一个n级的台阶总共有多少种跳法（先后次序不同算不同的结果）。
*/
//斐波拉契的解法
import java.util.Arrays;
public class Solution {
    public int JumpFloor(int target) {
        int[] n = {0,1,2};
        if (target<=2)
            return n[target];
        int pre = 1;//n-2
        int in = 2;//n-1
        int after = 0;//n
        for (int i = 3;i<=target;i++){
            after = pre+in;
            pre = in;
            in = after;
        }
        return after;
    }
}
