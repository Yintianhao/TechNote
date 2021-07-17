package JZOffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author yintianhao
 * @createTime 16 17:07
 * @description
 */
public class Solution64 {

    public static void main(String[] args){
       System.out.println(4&7);

    }

    public static ArrayList<Integer> maxInWindows(int [] num, int size) {
        ArrayList<Integer> ans = new ArrayList<>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        if (num==null||size>num.length||size==0)
            return ans;
        for (int i = 0;i+size <= num.length;i++){
            for (int j = 0;j < size;j++){
                maxHeap.offer(num[i+j]);
            }
            ans.add(maxHeap.peek());
            while (!maxHeap.isEmpty()){
                maxHeap.poll();
            }
        }
        return ans;
    }
}
