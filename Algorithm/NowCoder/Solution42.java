package JZOffer;

import java.util.ArrayList;

/**
 * @author yintianhao
 * @createTime 31 1:38
 * @description
 */
public class Solution42 {

    public static void main(String[] args){
        int[] arr = {1,2,3,4,5,6,7,8,9};
        ArrayList<Integer> ans = FindNumbersWithSum(arr,10);
        for (int i:ans){
            System.out.println(i);
        }
    }
    public static ArrayList<Integer> FindNumbersWithSum(int [] array, int sum) {
        ArrayList<Integer> ans = new ArrayList<>();
        int low = 0,high = array.length-1;
        while(low<high){
            int t = array[low]+array[high];
            if (t==sum){
                ans.add(array[low]);
                ans.add(array[high]);
                return ans;
            }else if(t<sum){
                low++;
            }else high--;
        }
        return ans;
    }
}
