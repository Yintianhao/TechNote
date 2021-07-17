package JZOffer;

/**
 * @author yintianhao
 * @createTime 2020110 9:55
 * @description 31
 */
/**
 * 求出1~13的整数中1出现的次数,
 * 并算出100~1300的整数中1出现的次数？
 * 为此他特别数了一下1~13中包含1的数字
 * 有1、10、11、12、13因此共出现6次,
 * 但是对于后面问题他就没辙了。ACMer希望你们帮帮他,
 * 并把问题更加普遍化,可以很快的求出任意非负整数区间中1出现的次数（从1 到 n 中1出现的次数）。
 * */

public class Solution31 {
    public static void main(String[] args){
        System.out.println(NumberOf1Between1AndN_Solution(1));
    }

    public static int NumberOf1Between1AndN_Solution(int n) {
        if(n<=0)
            return 0;
        int count = 0;
        int k = 0;
        for(int i = 1;i <= n;i*=10){
            k = n%(i*10);
            count+=i*(n/(i*10));
            count+=k>2*i-1?i:k<i?0:(k-i+1);
        }
        return count;
    }
}
