package JZOffer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author yintianhao
 * @createTime 10 2:42
 * @description
 */
public class Solution54 {
    private int[] chars = new int[128];
    private Queue<Character> q = new LinkedList<Character>();
    public void Insert(char ch) {
        chars[ch-'\0']++;
        if (chars[ch-'\0']==1)
            q.add(ch);
    }
    //return the first appearence once char in current stringstream
    public char FirstAppearingOnce() {
        while(!q.isEmpty()&&chars[q.peek()-'\0']>=2)
            q.poll();
        if (q.isEmpty())
            return '#';
        return q.peek();
    }
    public static void main(String[] args){
        String str = "!@$%^&*()_";
        char[] c = str.toCharArray();
        Solution54 s = new Solution54();
        for (char ch:c){
            s.Insert(ch);
        }
        System.out.println(s.FirstAppearingOnce());
    }
}
