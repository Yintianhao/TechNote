package JZOffer;

/**
 * @author yintianhao
 * @createTime 08 2:26
 * @description
 */
public class Solution52 {

    public static void main(String[] args){

        System.out.println(new Solution52().match("aaa".toCharArray(),"a.a".toCharArray()));
    }
    public boolean match(char[] str, char[] pattern) {
        //模式中的字符'.'表示任意一个字符，而'*'表示它前面的字符可以出现任意次（包含0次）
        //"aaa"与模式"a.a"和"ab*ac*a"匹配，但是与"aa.a"和"ab*a"均不匹配
        if (str==null||pattern==null)
            return false;
        int s_index = 0;
        int p_index = 0;
        return core(str,pattern,s_index,p_index);
    }
    public boolean core(char[] s,char[] p,int si,int pi){
        if (si==s.length&&pi==p.length)
            return true;
        if (pi==p.length&&si!=s.length)
            return false;
        if (pi<p.length-1&&si!=s.length&&p[pi+1]=='*'){
            if ((si!=s.length&&p[pi]==s[si])||(si!=s.length&&p[pi]=='.')){
                return core(s,p,si,pi+2)&&core(s,p,si+1,pi+2)&&core(s,p,si+1,pi);
            }else{
                return core(s,p,si,pi+2);
            }
        }
        if ((si!=s.length&&p[pi+1]!='*'&&s[si]==p[pi])||
                (p[pi]=='.'&&si!=s.length)){
            return core(s,p,si+1,pi+1);
        }
        return false;
    }
}
