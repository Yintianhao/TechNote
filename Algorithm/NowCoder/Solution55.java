package JZOffer;

import common.ListNode;

/**
 * @author yintianhao
 * @createTime 11 1:15
 * @description
 */
public class Solution55 {
    public ListNode EntryNodeOfLoop(ListNode head)
    {
        if(head==null)
            return null;
        ListNode slow = head;
        ListNode fast = head;
        while(fast!=null&&fast.next!=null){
            slow = slow.next;
            fast = fast.next.next;
            if (fast==slow){
                //有环
                slow = head;
                while(slow!=fast){
                    slow = slow.next;
                    fast = fast.next;
                }
                return slow;
            }
        }
        return null;
    }
}
