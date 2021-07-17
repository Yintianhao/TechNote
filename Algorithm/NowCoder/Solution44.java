package JZOffer;

/**
 * @author yintianhao
 * @createTime 01 0:57
 * @description
 */
public class Solution44 {
    public static void main(String[] a){
        String arr = "student. a am I";
        System.out.println(new Solution44().ReverseSentence(arr));
    }
    public String ReverseSentence(String str) {
        char[] arr = str.toCharArray();
        int len = arr.length;
        int l = 0,r = 0;
        int i = 0;
        reverse(arr,0,len-1);
        while (i<len){
            while(i<len&&arr[i]==' ')
                i++;
            l = r = i;
            while(i<len&&arr[i]!=' '){
                r++;
                i++;
            }
            reverse(arr,l,r-1);
        }
        return new String(arr);

    }
    public void reverse(char[] word, int l, int r){
        char t;
        while (l<r){
            t = word[l];
            word[l] = word[r];
            word[r] = t;
            l++;
            r--;
        }
    }
}
