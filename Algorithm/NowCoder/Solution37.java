package JZOffer;

import java.util.HashMap;

/**
 * @author yintianhao
 * @createTime 27 1:28
 * @description
 */
public class Solution37 {
    public static void main(String[] args){
        int[] arr = {1,2,3,4,5,6,7,8,8};
        System.out.print(new Solution37().GetNumberOfK(arr,8));
    }
    public int GetNumberOfK(int [] array , int k) {
        return binSearch(array,k+0.5)-binSearch(array,k-0.5);
    }
    public int binSearch(int[] arr,double k){
        int s = 0;
        int e = arr.length-1;
        int mid = 0;
        while(s<=e){
            mid = (e-s)/2+s;
            if(arr[mid]<k)
                s = mid+1;
            else
                e = mid-1;
        }
        return s;
    }
}
