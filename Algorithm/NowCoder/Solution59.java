package JZOffer;

import common.TreeNode;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author yintianhao
 * @createTime 15 1:51
 * @description
 */
public class Solution59 {
    public ArrayList<ArrayList<Integer>> Print(TreeNode root) {
        int level = 1;
        Stack<TreeNode> s1 = new Stack<>();
        Stack<TreeNode> s2 = new Stack<>();
        s1.push(root);
        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
        while(!s1.empty()||!s2.empty()){

            if (level%2==1){
                //奇数层
                ArrayList<Integer> t = new ArrayList<>();
                while (!s1.empty()){
                    TreeNode p = s1.pop();
                    if (p!=null){
                        t.add(p.val);
                        s2.push(p.left);
                        s2.push(p.right);
                    }
                }
                if (t.size()!=0){
                    ans.add(t);
                    level++;
                }
            }else {
                ArrayList<Integer> t = new ArrayList<>();
                while(!s2.empty()){
                    TreeNode p = s2.pop();
                    if (p!=null){
                        t.add(p.val);
                        s1.push(p.right);
                        s1.push(p.left);
                    }
                }
                if (t.size()!=0){
                    ans.add(t);
                    level++;
                }
            }
        }
        return ans;
    }
}
