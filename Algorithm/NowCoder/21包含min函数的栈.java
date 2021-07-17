/**
定义栈的数据结构，请在该类型中实现一个能够得到栈中所含最小元素的min函数（时间复杂度应为O（1））。
*/
import java.util.Stack;

public class Solution {

    private Stack<Integer> stack;
    private Stack<Integer> min;

    public Solution(){
        stack = new Stack<>();
        min = new Stack<>();
    }

    public void push(int node) {
        stack.push(node);
        if (min.size()==0){
            min.push(node);
        }
        if (min.size()!=0&&min.peek()>=node){
            min.push(node);
        }
        if (min.size()!=0&&min.peek()<node){
            min.push(min.peek());
        }
    }

    public void pop() {
        stack.pop();
        min.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int min() {
        return min.peek();
    }
}
