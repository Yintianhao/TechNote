/**
给定一个double类型的浮点数base和int类型的整数exponent。求base的exponent次方。
*/
public class Solution {
    public double Power(double a, int b) {
        double answer = 1;
        int tmp = Math.abs(b);
        if (b==0)
            return (double)1;
        while (tmp>0){
            if(tmp%2==1)
                answer = answer * a;
            tmp = tmp/2;
            a = a * a;
        }
        if (b>0)
            return answer;
        else
            return (double)(1/answer);
  }
}
