package JZOffer;

/**
 * @author yintianhao
 * @createTime 04 0:47
 * @description
 */
public class Solution49 {

    public static void main(String[] args){
        String str1 = "2147483648";
        String str2 = "123";
        System.out.println(new Solution49().StrToInt(str1));
    }
    public int StrToInt(String str) {
        int flag = 1;
        long res = 0;
        if (str==null||str.length()==0)
            return 0;
        char[] arr = str.toCharArray();
        if (arr[0]=='-')
            flag = -1;
        for (int i = (arr[0]=='+'||arr[0]=='-')?1:0;i<arr.length;i++){
            if (arr[i]>'9'||arr[i]<'0')
                return 0;
            //long s = res*10 + (arr[i]-'0');
            res = res*10 + (arr[i]-'0');
            if((flag==1&&res*flag>Integer.MAX_VALUE)||(flag==-1&&res*flag<Integer.MIN_VALUE))
                return 0;
        }
        return (int)(res*flag);
    }
}
