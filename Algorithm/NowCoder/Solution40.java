package JZOffer;

/**
 * @author yintianhao
 * @createTime 29 2:18
 * @description
 */
public class Solution40 {
    public static void main(String[] args){
        System.out.print((4&(-4)));
    }
    public void FindNumsAppearOnce(int [] array,int num1[] , int num2[]) {
        int xor = 0;
        for(int n:array){
            xor^=n;
        }
        int index = xor&(-xor);
        int res1 = 0;
        int res2 = 0;
        for (int n:array){
            if ((index&n)==0){
                res1 = res1^n;
            }else {
                res2 = res2^n;
            }
        }
        num1[0]=res1;
        num2[0]=res2;
    }
}
