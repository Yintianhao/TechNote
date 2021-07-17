/**
输入一个链表，反转链表后，输出新链表的表头。
*/
/*
public class ListNode {
    int val;
    ListNode next = null;

    ListNode(int val) {
        this.val = val;
    }
}*/
public class Solution {
    public ListNode ReverseList(ListNode head) {
        ListNode preNode = null;//前一个
        ListNode currentNode = head;//当前
        ListNode reserveNode = null;//反转后的头结点
        while (currentNode!=null){
            ListNode nextNode = currentNode.next;
            if (nextNode==null){
                reserveNode = currentNode;
            }
            currentNode.next = preNode;
            preNode = currentNode;
            currentNode = nextNode;
        }
        return reserveNode;
    }
}
