/*
题目描述
在一个二维数组中（每个一维数组的长度相同），每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。
*/
public class Solution {
    public boolean Find(int target, int [][] array) {
        if (array.length==0||array[0].length==0)
            return false;
        int row = array.length;//行
        int column = array[0].length;//列
        for (int i = 0;i < row;i++){
            if(target>=array[i][0]&&target<=array[i][column-1]){
                for (int j = 0;j < column;j++){
                    if (target==array[i][j]){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
### 改进版
从左下角开始，数组的特点是每一行从左往右递增，每一列从上到下递减、小于target则左移，大于target则下移。
```
public class Solution {
    public boolean Find(int target, int [][] array) {
         if (array==null||array.length==0||array[0].length==0)
            return false;
        int rows = array.length;//行
        int cols = array[0].length;//列
        for (int i = rows-1,j = 0;i>=0&&j<cols;){
            if (array[i][j]==target)
                return true;
            if (array[i][j]>target){
                i--;
                continue;
            }
            if (array[i][j]<target){
                j++;
                continue;
            }
        }
        return false;
    }
}
```

