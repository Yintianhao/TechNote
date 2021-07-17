package JZOffer;

import common.TreeNode;

/**
 * @author yintianhao
 * @createTime 28 2:06
 * @description
 */
public class Solution39 {
    public boolean IsBalanced_Solution(TreeNode root) {
        if (root==null)
            return true;
        return depth(root)!=-1;
    }

    public int depth(TreeNode root){
        if (root==null)
            return 0;
        int leftDepth = depth(root.left);
        if (leftDepth==-1)
            return -1;
        int rightDepth = depth(root.right);
        if (rightDepth==-1)
            return -1;
        return Math.abs(leftDepth-rightDepth)<=1?Math.max(leftDepth,rightDepth)+1:-1;
    }
}
