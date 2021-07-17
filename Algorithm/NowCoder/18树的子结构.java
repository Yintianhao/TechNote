/**
输入两棵二叉树A，B，判断B是不是A的子结构。（ps：我们约定空树不是任意一个树的子结构）
*/
/**
public class TreeNode {
    int val = 0;
    TreeNode left = null;
    TreeNode right = null;

    public TreeNode(int val) {
        this.val = val;

    }

}
*/
public class Solution {
    //判断是否为子结构
    public boolean HasSubtree(TreeNode a,TreeNode b) {
        boolean is = false;
        if(b==null)
            return false;
        if (a!=null){
            if (a.val == b.val){
                is = isEquals(a,b);
            }
            if (!is){
                is = HasSubtree(a.left,b);
            }
            if (!is){
                is = HasSubtree(a.right,b);
            }
        }else
            return false;
        return is;
    }
    public boolean isEquals(TreeNode a,TreeNode b){
        boolean is = false;
        if (b==null)
            //b为空,说明b已经被遍历完了,可以肯定含有子树
            return true;
        if (a==null)
            //b不为空,a为空,不再有存在子结构的可能
            return false;
        if (a.val==b.val){
            is = isEquals(a.right,b.right)&&isEquals(a.left,b.left);
        }else {
            return false;
        }
        return is;
    }
}
