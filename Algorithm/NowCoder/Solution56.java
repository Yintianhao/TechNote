package JZOffer;

import common.ListNode;

import java.util.HashMap;

/**
 * @author yintianhao
 * @createTime 11 1:33
 * @description
 */
public class Solution56 {
    /**
     * 在一个排序的链表中，存在重复的结点，请删除该链表中重复的结点，
     * 重复的结点不保留，返回链表头指针。 例如，链表1->2->3->3->4->4->5 处理后为 1->2->5
     * */
    public static ListNode deleteDuplication(ListNode head) {
        //排序链表，重复一定是相邻的
        if (head==null||head.next==null)
            return head;
        ListNode h = new ListNode(-1);
        h.next = head;
        ListNode p = h;
        ListNode t = p.next;
        while (t!=null){
            if (t.next!=null&&t.next.val==t.val){
                while(t.next!=null&&t.next.val == t.val){
                    t = t.next;
                }
                p.next = t.next;
                t =t.next;
            }else {
                p = p.next;
                t = t.next;
            }
        }
        return h.next;
    }
    /**
     * public static ListNode fun(ListNode head){
     *         HashMap<ListNode,Integer> map = new HashMap<>();
     *         if (head==null)
     *             return head;
     *         ListNode p = head;
     *         ListNode t = p.next;
     *     }*/
    private static ListNode generate(int[] arr){
        ListNode h = new ListNode(0);
        ListNode p = h;
        for (int i = 0;i < arr.length;i++){
            p.next = new ListNode(arr[i]);
            p = p.next;
        }
        return h;
    }
    public static void main(String[] args){
        int[] arr = {1,1,1,1,1,1,2};
        ListNode p = generate(arr);
        p = deleteDuplication(p);
        //p = p.next;
        while (p!=null){
            System.out.println(p.val);
            p = p.next;
        }
    }
}
