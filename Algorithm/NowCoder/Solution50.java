package JZOffer;

/**
 * @author yintianhao
 * @createTime 04 2:02
 * @description
 */
public class Solution50 {
    public boolean fun(int numbers[],int length,int [] duplication){

        if (numbers==null||numbers.length<=1)
            return false;
        //int len = numbers.length;
        for (int i = 0;i < length;i++){
            int index = numbers[i];
            if (index>length){
                index = index%length;
            }
            if (numbers[index]>length) {
                duplication[0] = numbers[index];
                return true;
            }
            numbers[index] += length;
        }
        return false;
    }
}
