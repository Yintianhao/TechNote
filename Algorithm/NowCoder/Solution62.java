package JZOffer;

import common.TreeNode;

import java.util.Stack;

/**
 * @author yintianhao
 * @createTime 16 15:19
 * @description
 */
public class Solution62 {
    /**
     * 给定一棵二叉搜索树，请找出其中的第k小的结点。
     * 例如， （5，3，7，2，4，6，8）    中，按结点数值大小顺序第三小结点的值为4。
     * */
    public TreeNode KthNode(TreeNode root, int k) {
        if (root==null||k==0)
            return null;
        int order = 0;
        Stack<TreeNode> s = new Stack<>();
        do {
            if (root!=null){
                s.push(root);
                root = root.left;
            }else{
                root= s.pop();
                order++;
                if (order==k)
                    return root;
                root = root.right;
            }
        }while (root!=null||!s.isEmpty());
        return null;
    }

}
