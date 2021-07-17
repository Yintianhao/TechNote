/**
大家都知道斐波那契数列，现在要求输入一个整数n，请你输出斐波那契数列的第n项（从0开始，第0项为0）。
n<=39
*/
//矩阵快速幂或者快速幂
public class Solution {
    public int Fibonacci(int n) {
        //初始化A
        int[][] base = {{1,1},{1,0}};
        //answer初始化为单位矩阵
        int[][] answer = {{1,0},{0,1}};
        while (n>0){
            if(n%2==1)
                answer = Matrix(answer,base);
            base = Matrix(base,base);
            n = n /2;
        }
        return answer[0][1];
    }
    public int[][] Matrix(int[][] a,int[][] b){
        int[][] temp = {{0,0},{0,0}};
        for (int i = 0;i < 2;i++)
            for (int j = 0;j < 2;j++)
                for (int k = 0;k < 2;k++)
                    temp[i][j] += a[i][k]*b[k][j];
        return temp;
    }
}
