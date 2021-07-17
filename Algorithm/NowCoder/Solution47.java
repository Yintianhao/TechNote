package JZOffer;

/**
 * @author yintianhao
 * @createTime 02 15:51
 * @description
 */
public class Solution47 {

    public static void main(String[] args){
        System.out.println(new Solution47().Sum_Solution(10));
    }
    public int Sum_Solution(int n) {
        int sum = n;
        boolean a = (sum>0)&&((sum+=Sum_Solution(--n))>0);
        return sum;
    }
}
