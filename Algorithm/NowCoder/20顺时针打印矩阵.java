/**
输入一个矩阵，按照从外向里以顺时针的顺序依次打印出每一个数字，例如，如果输入如下4 X 4矩阵：
1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 则依次打印出数字1,2,3,4,8,12,16,15,14,13,9,5,6,7,11,10.
*/
import java.util.ArrayList;
public class Solution {
    public ArrayList<Integer> printMatrix(int [][] matrix) {
        int rowEnd = matrix.length-1;//行
        int columnEnd = matrix[0].length-1;//列
        int columnStart = 0;
        int rowStart = 0;
        ArrayList<Integer> res = new ArrayList<>();
        //特殊情况,只有一行
        if (matrix.length==1){
            for (int i = 0;i < matrix[0].length;i++){
                res.add(matrix[0][i]);
            }
            return res;
        }
        //特殊情况,只有一列
        if (matrix[0].length==1){
            for (int i = 0;i < matrix.length;i++){
                res.add(matrix[i][0]);
            }
            return res;
        }
        //把矩形看做一圈一圈的,一圈一圈打印,控制好边界
        while (true){
            if (columnEnd<columnStart||rowStart>rowEnd){
                break;
            }
            res.addAll(printIn(matrix,rowStart,rowEnd,columnStart,columnEnd));
            rowStart+=1;
            columnStart+=1;
            rowEnd-=1;
            columnEnd-=1;
        }
        return res;
    }
    //全部常量均为下标,即从0开始
    public ArrayList<Integer> printIn(int[][] matrix,int rowStart,int rowEnd,int columnStart,int columnEnd){
        ArrayList<Integer> list = new ArrayList<>();
        int i = 0;
        if (columnEnd==columnStart){
            //列开始和列结束重合,即最里圈只剩一个数
            list.add(matrix[rowEnd][columnEnd]);
            return list;
        }
        if (rowEnd==rowStart){
            //行开始和行结束重合,最里圈只有一行,将此行加入list
            for (i = columnStart;i <= columnEnd;i++){
                list.add(matrix[rowStart][i]);
            }
            return list;
        }
        //四个for循环遍历一圈
        for (i = columnStart;i <= columnEnd;i++){
            list.add(matrix[rowStart][i]);
        }
        for (i = rowStart+1;i < rowEnd;i++){
            list.add(matrix[i][columnEnd]);
        }
        for (i = columnEnd;i>=columnStart;i--){
            list.add(matrix[rowEnd][i]);
        }
        for (i = rowEnd-1;i>=rowStart+1;i--){
            list.add(matrix[i][rowStart]);
        }
        return list;
    }
}
