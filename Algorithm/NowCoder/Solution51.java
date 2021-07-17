package JZOffer;

/**
 * @author yintianhao
 * @createTime 06 1:27
 * @description
 */
public class Solution51 {
    public int[] multiply(int[] A) {
        int len = A.length;
        int[] B = new int[len];
        if (len<=0)
            return B;
        B[0] = 1;
        for (int i = 1;i < len;i++){
            B[i] = B[i-1]*A[i-1];
        }
        int t = 1;
        for (int j = len-2;j>=0;j--){
            t = t * A[j+1];
            B[j] = B[j]*t;
        }
        return B;
    }
}
