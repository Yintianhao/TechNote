package JZOffer;

/**
 * @author yintianhao
 * @createTime 31 1:55
 * @description
 */
public class Solution43 {

    public static void main(String[] args){
        String str= "abcde";
    }
    public String LeftRotateString(String str,int n) {
        int len = str.length();
        char[] arr = str.toCharArray();
        reverse(arr,0,n-1);
        reverse(arr,n,len-1);
        reverse(arr,0,len-1);
        return new String(arr);
    }
    public void reverse(char[] arr,int l,int r){
        char t;
        while(l<r){
            t = arr[l];
            arr[l] = arr[r];
            arr[r] = t;
            l++;
            r--;
        }
    }
}
