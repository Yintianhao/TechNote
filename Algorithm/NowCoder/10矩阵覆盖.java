/**
我们可以用2*1的小矩形横着或者竖着去覆盖更大的矩形。请问用n个2*1的小矩形无重叠地覆盖一个2*n的大矩形，总共有多少种方法？
*/
//斐波拉契
public class Solution {
    public int RectCover(int target) {
        int[] n = {0,1,2};
        if (target<=2)
            return n[target];
        int pre = 1;//f(n-2)
        int in = 2;//f(n-1)
        int after = 0;//f(n)
        for (int i = 3;i<=target;i++){
            after = pre+in;
            pre = in;
            in = after;
        }
        return after;
    }
}
