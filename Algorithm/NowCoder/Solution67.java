package JZOffer;

/**
 * @author yintianhao
 * @createTime 17 22:50
 * @description
 */
public class Solution67 {
    public int cutRope(int target) {
        if (target<2)
            return 0;
        if (target==2)
            return 1;
        if (target==3)
            return 2;
        int[] p = new int[target+1];
        p[0] = 0;
        p[1] = 1;
        p[2] = 2;
        p[3] = 3;
        for (int i = 4;i <= target;i++){
            for (int j = 1;j <= target/2;j++){
                p[i] = Math.max(p[i],p[i-j]*p[j]);
            }
        }
        return p[target];
    }
}
