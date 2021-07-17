package JZOffer;

import common.TreeNode;

/**
 * @author yintianhao
 * @createTime 27 1:47
 * @description
 */
public class Solution38 {
    public int TreeDepth(TreeNode root) {
        return len(root,0);
    }
    public int len(TreeNode root,int level){
        if (root == null)
            return level;
        else
            return Math.max(len(root.left,level+1),len(root.right,level+1));
    }
}
