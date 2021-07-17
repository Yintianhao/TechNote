package JZOffer;

import common.TreeLinkNode;

/**
 * @author yintianhao
 * @createTime 15 0:09
 * @description
 */
public class Solution57 {

    /**
     * 给定一个二叉树和其中的一个结点，
     * 请找出中序遍历顺序的下一个结点并且返回。
     * 注意，树中的结点不仅包含左右子结点，同时包含指向父结点的指针。
     * */
    public TreeLinkNode GetNext(TreeLinkNode p) {
        if(p==null)
            return null;
        if (p.right!=null){
            //有右孩子
            p = p.right;
            while(p.left!=null){
                p = p.left;
            }
            return p;//右子树最左
        }
        while(p.next!=null){
            TreeLinkNode t = p.next;
            if (t.left==p)
                return t;
            p = p.next;
        }
        return null;
    }
}
