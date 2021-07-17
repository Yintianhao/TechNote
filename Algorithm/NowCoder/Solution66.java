package JZOffer;

/**
 * @author yintianhao
 * @createTime 17 21:48
 * @description
 */
public class Solution66 {
    public static void main(String[] args){
        System.out.println(new Solution66().movingCount(5,10,10));
    }
    public int movingCount(int threshold, int rows, int cols) {
        int[][] view = new int[rows][cols];
        return count(threshold,rows,cols,0,0,view);
    }
    public int count(int k,int rows,int cols,int i,int j,int[][] view){
        if(i<0||i>=rows)
            return 0;
        if (j<0||j>=cols)
            return 0;
        if (view[i][j]==1)
            return 0;
        if (sum(i)+sum(j)>k)
            return 0;
        view[i][j] = 1;
        return 1+count(k,rows,cols,i+1,j,view)+count(k,rows,cols,i-1,j,view)
                +count(k,rows,cols,i,j+1,view)+count(k,rows,cols,i,j-1,view);
    }
    private int sum(int num){
        int sum = 0;
        while(num>0){
            sum+=num%10;
            num /=10;
        }
        return sum;
    }
}
