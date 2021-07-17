package JZOffer;

/**
 * @author yintianhao
 * @createTime 15 23:10
 * @description
 */
public class Solution32 {


    public static void main(String[] args){
        int[] arr = {3,32,321};
        System.out.println(PrintMinNumber(arr));
    }
    //如输入数组{3，32，321}，则打印出这三个数字能排成的最小数字为321323。
    //321 32 3
    public static String PrintMinNumber(int [] numbers) {
        int i = 0;
        int j = 0;
        StringBuilder bd = new StringBuilder();
        for(i = 0;i < numbers.length;i++){
            for(j = i+1;j < numbers.length;j++){
                int a = Integer.valueOf(numbers[i]+""+numbers[j]);
                int b = Integer.valueOf(numbers[j]+""+numbers[i]);
                if(a > b){
                    int t = numbers[i];
                    numbers[i] = numbers[j];
                    numbers[j] = t;
                }
            }
        }
        for(int k = 0;k < numbers.length;k++){
            bd.append(String.valueOf(numbers[k]));
        }
        return bd.toString();
    }
}
