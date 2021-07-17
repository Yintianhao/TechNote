package JZOffer;

/**
 * @author yintianhao
 * @createTime 23 15:21
 * @description
 */
public class Solution35 {

    int count = 0;
    public static void main(String[] args){
        int[] arr = {1,2,3,4,5,6,7,0};
        System.out.print(new Solution35().InversePairs(arr));
    }
    /**
     *     在数组中的两个数字，如果前面一个数
     *     字大于后面的数字，则这两个数字组成
     *     一个逆序对。输入一个数组,求出这个数
     *     组中的逆序对的总数P。并将P对1000000007取模的结果输出。 即输出P%1000000007
     * */
    public int InversePairs(int [] arr) {
        if (arr==null||arr.length==1)
            return 0;
        divide(arr,0,arr.length-1);
        return count;
    }
    public void divide(int[] arr,int l,int r){
        if (l>=r)
            return;
        int mid = (l+r)/2;
        divide(arr,l,mid);
        divide(arr,mid+1,r);
        merge(arr,l,mid,r);
    }
    public void merge(int[] arr,int l,int m,int r){
        int[] t = new int[r-l+1];
        int i = l;
        int j = m+1;
        int k = 0;
        while(i<=m&&j<=r){
            if(arr[i]<=arr[j]){
                t[k++] = arr[i++];
            }
            else{
                t[k++] = arr[j++];
                count = (count+m-i+1)%1000000007;
            }
        }
        while(i<=m)
            t[k++] = arr[i++];
        while(j<=r)
            t[k++] = arr[j++];
        for (k = 0;k < t.length;k++)
            arr[l+k] = t[k];
    }

}
