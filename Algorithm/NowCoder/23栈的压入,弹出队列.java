/**
输入两个整数序列，第一个序列表示栈的压入顺序，请判断第二个序列是否可能为该栈的弹出顺序。假设压入栈
的所有数字均不相等。例如序列1,2,3,4,5是某栈的压入顺序，序列4,5,3,2,1是该压栈序列
对应的一个弹出序列，但4,3,5,1,2就不可能是该压栈序列的弹出序列。（注意：这两个序列的长度是相等的）
*/
import java.util.ArrayList;
import java.util.Stack;
public class Solution {
    public boolean IsPopOrder(int [] pushA,int [] popA) {
      Stack<Integer> stack = new Stack<>();
        int push = 0;
        int pop = 0;
        while (push<pushA.length){
            if (pushA[push]==popA[pop]){
                push++;
                pop++;
            } else{
                stack.push(pushA[push]);
                push++;
            }
        }
        for (int i = pop;i < pushA.length;i++){
            if (stack.pop()!=popA[i])
                return false;
        }
        return stack.empty()?true:false;
    }
}
