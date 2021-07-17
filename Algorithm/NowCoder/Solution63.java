package JZOffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author yintianhao
 * @createTime 16 15:59
 * @description
 */
public class Solution63 {
    private PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    private PriorityQueue<Integer> maxHeap = new PriorityQueue<Integer>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2-o1;
        }
    });
    int index = 0;
    public void Insert(Integer num) {
        //奇数
        if (index%2==1){
            //插入小顶堆
            minHeap.offer(num);
            maxHeap.offer(minHeap.poll());
        }else{
            //插入大顶堆
            maxHeap.offer(num);
            minHeap.offer(maxHeap.poll());
        }
        index++;
    }

    public Double GetMedian() {
        if (index%2==0){
            double ans = new Double((minHeap.peek()+maxHeap.peek()))/2;
            return ans;
        }else {
            double ans = new Double(minHeap.peek());
            return ans;
        }
    }
}
