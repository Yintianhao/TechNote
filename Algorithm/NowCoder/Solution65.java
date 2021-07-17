package JZOffer;

import javafx.geometry.Pos;

import java.util.ArrayList;

/**
 * @author yintianhao
 * @createTime 17 0:57
 * @description
 */
public class Solution65 {

    public static void main(String[] args){
        char[] m = {'a','b','c','c','b','a','a','b','c'};


    }
    public boolean hasPath(char[] matrix, int rows, int cols, char[] str)
    {
        ArrayList<Position> positions = searchPositionsOfHead(matrix,rows,cols,str[0]);
        int[][] view = new int[rows][cols];
        for(Position p:positions){
            if (judge(matrix,rows,cols,p.r,p.c,view,str,0))
                return true;
        }
        return false;
    }
    public boolean judge(char[] matrix,int rows,int cols,int r,int c,int[][] view,char[] str,int order){
        Character ch = getChar(matrix,rows,cols,r,c);
        if (ch==null||!ch.equals(str[order])||view[r][c]==1)
            return false;
        if (order==str.length-1)
            return true;//达到最后
        view[r][c]=1;
        if(judge(matrix,rows,cols,r,c-1,view,str,order+1)||
                judge(matrix,rows,cols,r-1,c,view,str,order+1)||
                judge(matrix,rows,cols,r,c+1,view,str,order+1)||
                judge(matrix,rows,cols,r+1,c,view,str,order+1)){
            return true;
        }
        view[r][c]=0;
        return false;
    }
    /**
     * 获得特定位置的字符
     * */
    public Character getChar(char[] matrix,int rows,int cols,int r,int c){
        if (r>=rows||r<0)
            return null;
        if (c>=cols||c<0)
            return null;
        if (r*cols+c>matrix.length)
            return null;
        return matrix[r*cols+c];
    }
    /**
     * 搜索head的位置
     * */
    public ArrayList<Position> searchPositionsOfHead(char[] matrix, int rows, int cols, char head){
        ArrayList<Position> positions = new ArrayList<>();
        for (int i = 0; i < matrix.length;i++){
            if (head==matrix[i]){
                int r = i/cols;
                int c = i - cols*r;
                Position p = new Position(r,c);
                positions.add(p);
            }
        }
        return positions;
    }
    class Position {
        private int r;
        private int c;
        public Position(int r,int c){
            this.r = r;
            this.c = c;
        }
    }

}
