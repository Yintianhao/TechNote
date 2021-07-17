package JZOffer;

/**
 * @author yintianhao
 * @createTime 02 1:35
 * @description
 */
public class Solution45 {
    public boolean isContinuous(int [] numbers) {
        if (numbers.length!=5)
            return false;
        int[] times = new int[14];
        int min = 14;
        int max = -1;
        for(int i = 0;i < numbers.length;i++){
            times[numbers[i]]++;
            if (numbers[i]==0)
                continue;
            if (numbers[i]>max)
                max = numbers[i];
            if (numbers[i]<min)
                min = numbers[i];
            if (times[numbers[i]]>1)
                return false;
        }
        if (max-min<5)
            return true;
        return false;
    }
}
