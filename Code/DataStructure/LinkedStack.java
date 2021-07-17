/*
 * @Author: Yintianhao
 * @Date: 2020-05-08 17:31:31
 * @LastEditTime: 2020-05-09 00:58:06
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Code\DataStructure\LinkedStack.java
 * @Copyright@Yintianhao
 */
package DataStructure;

public class LinkedStack {
    private int count;
    private Node top = null;
    public LinkedStack(){
        count = 0;
    }
    public void push(int data){
        Node node = new Node(data);
        node.next = top;
        count++;
        top = node;
    }
    public int pop(){
        int n = top.data;
        top = top.next;
        count--;
        return n;
    }
    public int peek(){
        return top.data;
    }
    public boolean isEmpty(){
        return count==0;
    }
    class Node{
        private int data;
        private Node next;
        public Node(){}
        public Node(int data){
            this.data = data;
        }
    }    
    public static void main(String[] args){
        System.out.println(Math.ceil(4.3));
        int x = 0x16;
        x = x|8;
        System.out.println(x);
    }
}
