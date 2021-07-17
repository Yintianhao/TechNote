package JZOffer;

/**
 * @author yintianhao
 * @createTime 10 2:10
 * @description
 */
public class Solution53 {
    /**
     *
     * 链接：https://www.nowcoder.com/questionTerminal/6f8c901d091949a5837e24bb82a731f2?f=discussion
     * 来源：牛客网
     *
     * 12e说明e的后面必须有数字，不能有两个e
     * +-5说明符号位要么出现一次在首位，要么出现一次在e的后一位，其他地方都不能有
     * 12e4.3说明e的后面不能有小数，1.2.3说明不能有两个小数点
     * 1a3.14说明不能有其他的非法字符，比如这里的a
     * */
    public boolean isNumeric(char[] str) {
        boolean hasE = false;
        boolean spot = false;
        boolean signal = false;
        for (int i = 0;i < str.length;i++){
            if (str[i]=='E'||str[i]=='e'){
                if (i==str.length-1){
                    //E是最后一位
                    System.out.println("line "+27);
                    return false;
                }
                if (hasE){
                    //出现第二个E
                    System.out.println("line "+32);
                    return false;
                }
                hasE = true;
            }else if (str[i]=='+'||str[i]=='-'){
                //第一次出现，检查是否在第一位和前面是否有E
                if (!signal&&i>0&&(str[i-1]!='e'&&str[i-1]!='E')){
                    System.out.println("line "+39);
                    return false;
                }
                //不是第一次出现，检查前面是否有E
                if (signal&&str[i-1]!='E'&&str[i-1]!='e'){
                    System.out.println("line "+44);
                    return false;
                }
                signal = true;
            }else if (str[i]=='.'){
                if (hasE){
                    System.out.println("line "+53);
                    return false;
                }
                //第二次出现.
                if (spot){
                    System.out.println("line "+58);
                    return false;
                }
                spot = true;
            }else if (str[i]<'0'||str[i]>'9'){
                System.out.println("line "+49);
                return false;
            }
        }
        return true;
    }
    public static void main(String[] args){
        boolean is = new Solution53().isNumeric("123.45e+6".toCharArray());
        System.out.println(is);
    }
}
