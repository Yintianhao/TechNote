/**
请实现一个函数，将一个字符串中的每个空格替换成“%20”。例如，当字符串为We Are Happy.则经过替换之后的字符串为We%20Are%20Happy。
*/
public class Solution {
    public String replaceSpace(StringBuffer str) {
    	String string = str.toString();
        int turn = -1;
        for (int i = 0;i < string.length();i++){
            if (string.charAt(i)==' '){
                turn++;
                str.replace(i+2*turn,i+1+turn*2,"%20");
            }
        }
        return str.toString();
    }
}
## 改进版
从后往前替换
```
public class Solution2 {

    public static void main(String[] agrs){
        StringBuffer buffer = new StringBuffer("we are happy");
        System.out.println(new Solution2().replaceSpace(buffer));
    }

    public String replaceSpace(StringBuffer str) {
        int count = 0;
        int oldLen = str.length();
        for (int i = 0;i < str.length();i++){
            if (str.charAt(i)==' ')
                count++;
        }
        if (count==0)
            return str.toString();
        int newLen = oldLen+count*2;
        str.setLength(newLen);
        newLen--;
        for (int j = oldLen-1;j >= 0;j--){
            if (str.charAt(j)==' '){
                str.setCharAt(newLen--,'0');
                str.setCharAt(newLen--,'2');
                str.setCharAt(newLen--,'%');
            }else {
                str.setCharAt(newLen--,str.charAt(j));
            }
        }
        return str.toString();
    }
}

```
