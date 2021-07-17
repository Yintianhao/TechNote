### 每日一题 24 两两交换链表中的节点
```
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode(int x) { val = x; }
 * }
 */
class Solution {
    public ListNode swapPairs(ListNode head) {
          ListNode vHead = new ListNode(-1);
	       vHead.next = head;
	       ListNode p = vHead;
	       while(p.next!=null&&p.next.next!=null){
	    	   ListNode node1 = p.next;
	    	   ListNode node2 = p.next.next;
	    	   ListNode next =node2.next;
	    	   node2.next = node1;
	    	   node1.next = next;
	    	   p.next = node2;
	    	   p = node1;//p的下一个位置
	       }
	       return vHead.next;
    }
}
```
