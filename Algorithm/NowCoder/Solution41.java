package JZOffer;

import java.util.ArrayList;

/**
 * @author yintianhao
 * @createTime 31 1:11
 * @description
 */
public class Solution41 {

    public static void main(String[] args){
        ArrayList<ArrayList<Integer>> ans = FindContinuousSequence(100);
        for(ArrayList<Integer> a:ans){
            for (Integer n:a){
                System.out.print(n+" ");
            }
            System.out.println();
        }
    }
    public static ArrayList<ArrayList<Integer>> FindContinuousSequence(int sum) {
        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
        int low = 1,high = 2;
        while(low<high&&low<sum&&high<sum){
            int temp = (low+high)*(high-low+1)/2;
            if (temp==sum){
                ArrayList<Integer> part = new ArrayList<>();
                for (int i = low;i <= high;i++){
                    part.add(i);
                }
                ans.add(part);
                low++;
            } else if (temp<sum){
                high++;
            }else {
                low++;
            }
        }
        return ans;
    }
}
