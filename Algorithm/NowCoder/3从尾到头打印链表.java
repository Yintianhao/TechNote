/**
输入一个链表，按链表值从尾到头的顺序返回一个ArrayList。
*/
/**
*    public class ListNode {
*        int val;
*        ListNode next = null;
*
*        ListNode(int val) {
*            this.val = val;
*        }
*    }
*
*/
import java.util.ArrayList;
public class Solution {
    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        ArrayList<Integer> result = new ArrayList<>();
        if (listNode==null)
            return result;
        while (listNode!=null){
            result.add(listNode.val);
            listNode = listNode.next;
        }
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = result.size()-1;i>=0;i--){
            integers.add(result.get(i));
        }
        return integers;
    }
}
## 改进版
递归
```
import java.util.ArrayList;
public class Solution {
    private ArrayList<Integer> ans = new ArrayList<>();
    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        if (listNode!=null){
            printListFromTailToHead(listNode.next);
            ans.add(listNode.val);
        }
        return ans;
    }
}
```



