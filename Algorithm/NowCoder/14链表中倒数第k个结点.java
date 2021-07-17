/**
输入一个链表，输出该链表中倒数第k个结点。
*/
/*
public class ListNode {
    int val;
    ListNode next = null;

    ListNode(int val) {
        this.val = val;
    }
}*/
import java.util.ArrayList;
import java.util.List;
public class Solution {
    public ListNode FindKthToTail(ListNode head,int k) {
        if (k<=0)
            return null;
        ListNode pre = head,after = head;//先走后走的指针
        int i = 0;
        for (i = 0;pre != null;i++){
            if (i>=k)
                after = after.next;
            pre = pre.next;
        }
        return i<k?null:after;
    }
}
