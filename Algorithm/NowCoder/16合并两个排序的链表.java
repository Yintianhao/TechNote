/**
输入两个单调递增的链表，输出两个链表合成后的链表，当然我们需要合成后的链表满足单调不减规则。
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
    public ListNode Merge(ListNode list1,ListNode list2) {
        ListNode p1 = list1;
        ListNode p2 = list2;
        ListNode res = new ListNode(0);
        res.next = null;
        ListNode p3 = res;
        while (p1!=null&&p2!=null){
            if (p1.val<=p2.val){
                p3.next = p1;
                p3 = p1;
                p1 = p1.next;
            } else {
                p3.next = p2;
                p3 = p2;
                p2 = p2.next;
            }
        }
        p3.next = p1==null?p2:p1;
        return res.next;
    }
}
