package JZOffer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yintianhao
 * @createTime 21 23:29
 * @description
 */
public class Solution33 {

    public static void main(String[] args){
        for(int i = 1;i < 100;i++){
            System.out.print(GetUglyNumber_Solution(i)+" ");
        }
        System.out.println("\n");
        for(int i = 1;i < 100;i++){
            System.out.print(ans(i)+" ");
        }

    }
    public static int GetUglyNumber_Solution(int index) {
        if (index<7)
            return index;
        List<Integer> ans = new ArrayList<>();
        ans.add(1);
        int t=0;
        int t2=0,t3=0,t5=0;
        for (int i = 1;i < index;i++){
            t = Math.min(ans.get(t2)*2,Math.min(ans.get(t3)*3,ans.get(t5)*5));
            ans.add(t);
            if(ans.get(i)==ans.get(t2)*2) t2++;
            if(ans.get(i)==ans.get(t3)*3) t3++;
            if(ans.get(i)==ans.get(t5)*5) t5++;
        }
        return ans.get(index-1);
    }
    public static int ans(int n){
        if(n<=0)return 0;
        ArrayList<Integer> list=new ArrayList<Integer>();
        list.add(1);
        int i2=0,i3=0,i5=0;
        while(list.size()<n)//循环的条件
        {
            int m2=list.get(i2)*2;
            int m3=list.get(i3)*3;
            int m5=list.get(i5)*5;
            int min=Math.min(m2,Math.min(m3,m5));
            list.add(min);
            if(min==m2)i2++;
            if(min==m3)i3++;
            if(min==m5)i5++;
        }
        return list.get(list.size()-1);
    }
    public static boolean is(int n){
        int[] a = {2,3,5};
        int i = 0;
        for(i =0;i < 3;){
            if(n%a[i]==0)
                n = n/a[i];
            else
                i++;
        }
        if (n==1)
            return true;
        return false;

    }
}
