/**
一只青蛙一次可以跳上1级台阶，也可以跳上2级……它也可以跳上n级。求该青蛙跳上一个n级的台阶总共有多少种跳法。
*/
public class Solution {
    public int JumpFloorII(int target) {
        int sum = target;
        if (target==0)
            return 1;
        if (target==1)
            return 1;
        target = 1;
        for(int i = 1;i<sum;i++){
            target = target * 2;
        }
        return target;
    }
}
