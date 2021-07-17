package JZOffer;

import common.ListNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yintianhao
 * @createTime 02 14:00
 * @description
 */
public class Solution46 {

    public static void main(String[] args){
        int n = 6;
        int m = 4;
        int res = new Solution46().LastRemaining_Solution(n,m);
        System.out.println(res);
    }

    public int LastRemaining_Solution(int n, int m) {
        if (n<=0||m<=0){
            return -1;
        }
        int[] arr = new int[n];
        int step=0;
        int i = -1;
        int count = n;
        while(count>0){
            i++;
            if (i>=n)
                i=0;
            if(arr[i]==-1)
                continue;
            step++;
            if (step==m){
                arr[i] = -1;
                step = 0;
                count--;
            }
        }
        return i;
    }
}
