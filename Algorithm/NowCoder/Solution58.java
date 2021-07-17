package JZOffer;

import common.TreeNode;

import java.util.Stack;

/**
 * @author yintianhao
 * @createTime 15 1:05
 * @description
 */
public class Solution58 {
    public boolean isSymmetrical(TreeNode root)
    {
        if (root==null)
            return true;
        Stack<TreeNode> lstack = new Stack<>();
        Stack<TreeNode> rstack = new Stack<>();
        TreeNode lt = root.left;
        TreeNode rt = root.right;
        TreeNode t1;
        TreeNode t2;
        if(lt!=null&&rt!=null){
            while(lt.left!=null){
                lstack.push(lt);
                lt = lt.left;
            }
            while(rt.right!=null){
                rstack.push(rt);
                rt = rt.right;
            }
            if (lstack.size()!=rstack.size())
                return false;
            while(!lstack.empty()&&!rstack.empty()){
                t1 = lstack.pop();
                t2 = rstack.pop();
                if (t1.val!=t2.val)
                    return false;
                if (t1.right!=null)
                    lstack.push(t1.right);
                if (t2.left!=null)
                    rstack.push(t2.left);
            }
        }
        return true;
    }
}
