package JZOffer;

import common.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author yintianhao
 * @createTime 16 2:31
 * @description
 */
public class Solution60 {
    /**
     * 从上到下按层打印二叉树，同一层结点从左至右输出。每一层输出一行。
     * */
    public ArrayList<ArrayList<Integer>> Print(TreeNode root) {
        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
        Queue<TreeNode> nodes = new LinkedList<>();
        if (root==null)
            return ans;
        nodes.offer(root);
        while (!nodes.isEmpty()){
            ArrayList<Integer> s = new ArrayList<>();
            int size = nodes.size();//未加点前的大小
            for (int i = 0;i < size;i++){
                TreeNode n = ((LinkedList<TreeNode>) nodes).get(i);
                s.add(n.val);
                if (n.left!=null)
                    nodes.offer(n.left);
                if (n.right!=null)
                    nodes.offer(n.right);
            }
            ans.add(s);
            for (int i = 0;i < size;i++)
                nodes.poll();
        }
        return ans;

    }
}
