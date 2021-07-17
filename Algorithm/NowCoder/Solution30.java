package JZOffer;

/**
 * @author yintianhao
 * @createTime 08 23:08
 * @description
 */
/**在古老的一维模式识别中,常常需要计算连续子向量的最大和,当向量全为正数的时候,
 * 问题很好解决。但是,如果向量中包含负数,是否应该包含某个负数,
 * 并期望旁边的正数会弥补它呢？例如:{6,-3,-2,7,-15,1,2,2},
 * 连续子向量的最大和为8(从第0个开始,到第3个为止)。
 * 给一个数组，返回它的最大连续子序列的和，
 * */
public class Solution30 {
    public static void main(String []args) {
        int[] a = {6,-3,-2,7,-15,1,2,2};
        System.out.println(FindGreatestSumOfSubArray(a));
    }
    public static int FindGreatestSumOfSubArray(int[] a) {
        if(a.length==1)
            return a[0];
        int sum = a[0];//以a[i]结尾的子序列的最大和
        int max = sum;//最大连续子序列的最大和
        for(int i = 1;i < a.length;i++){
            sum = sum+a[i];
            //sum和a[i]中保留一个
            if(sum<a[i])
                sum = a[i];
            if(max<sum)
                max = sum;
        }
        return max;
    }
}
