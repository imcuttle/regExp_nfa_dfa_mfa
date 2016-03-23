package xyz.moyuyc.model;

import xyz.moyuyc.graph.ALGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yc on 2016/3/21 for compiler2.
 */
public class RegExpress {
    private String reg;
    public int length(){
        return reg.length();
    }
    public static void main(String[] args) throws Exception {
        //(a*|b*)b(ba)*
        RegExpress re = new RegExpress("(a*|b*)b(ba)*");//  a*|s*d  (a|b)a(a|b|c)*
        BinTree root = null;
        try {
            root = re.makeTree();
        }catch (Exception e){
            e.printStackTrace();
        }
        root.print();
        ALGraph g = root.makeNFAGraph("nfa");
        System.out.println(g);
        ALGraph dfa = g.toDFAGraph();
        System.out.println(dfa);
        ALGraph mfa = dfa.toMFAGraph();
        System.out.println(mfa);
    }



    public BinTree makeTree(){
        BinTree root = new BinTree();
        makeTree(root,reg,false);
        return root;
    }
    private void makeTree(BinTree node,String str,boolean flag){
        System.out.println(str);
        char[] charArr = str.toCharArray();
        boolean cat = false;
        if(str.length()==1){
            node.nodeType = BinTree.NodeType.DATA;
            node.data = str;
            return;
        }
//        if(charArr[0]=='('&&charArr[charArr.length-1]==')'){
//            charArr = str.substring(1,str.length()-1).toCharArray();
        if(charArr[charArr.length-1]=='*'&&flag){
            node.nodeType = BinTree.NodeType.OPERATION;
            node.opType = BinTree.OpType.ANY;
            node.left = new BinTree();
            node.left.parent = node;
            if(charArr.length>=2&&charArr[charArr.length-2]==')'){
                makeTree(node.left,str.substring(1,str.length()-2),false);
            }else
                makeTree(node.left,str.substring(0,str.length()-1),false);
        }else {
            for (int i = 0; i < charArr.length; i++) {
                if (charArr[i] == '|') {
                    node.nodeType = BinTree.NodeType.OPERATION;
                    node.opType = BinTree.OpType.OR;
                    node.left = new BinTree();
                    node.right = new BinTree();
                    node.right.parent = node;
                    node.left.parent = node;
                    if (charArr[i - 1] == '*') {
                        makeTree(node.left, str.substring(0, i),true);
                        makeTree(node.right, str.substring(i+1),false);
                        cat = false;
                    }else {
                        if (charArr[i - 1] == ')') {
                            makeTree(node.left, str.substring(1, i - 1),false);
                        } else {
                            makeTree(node.left, str.substring(0, i),false);
                        }
                        makeTree(node.right, str.substring(i + 1),false);
                    }
                    break;
                } else if (cat) {
                    node.nodeType = BinTree.NodeType.OPERATION;
                    node.opType = BinTree.OpType.CONCAT;
                    node.left = new BinTree();
                    node.right = new BinTree();
                    node.right.parent = node;
                    node.left.parent = node;
                    if(i+1!=str.length()&&(charArr[i+1]=='|')){
                        cat = false;
                        continue;
                    }
                    if(charArr[i]=='*'){

                        if(i+1!=str.length()) {
                            makeTree(node.left,str.substring(0,i+1),true);
                            makeTree(node.right, str.substring(i + 1), false);
                        } else {
                            makeTree(node,str.substring(0,i+1),true);
                            node.right = null;
                        }
                        cat = false;
                    } else if (charArr[i - 1] == '*') {

                        makeTree(node.left, str.substring(0, i),true);
                        makeTree(node.right, str.substring(i),false);
                        cat = false;
                    } else {
                        if (charArr[i - 1] == ')') {
                            makeTree(node.left, str.substring(1, i - 1),false);
                        } else {
                            makeTree(node.left, str.substring(0, i),false);
                        }
//                        if(charArr[i+1]=='(')
//                            makeTree(node.right, str.substring(i+2,charArr.length-1));
//                        else
                            makeTree(node.right, str.substring(i),false);
                    }
                    break;
                } else if (charArr[i] == '('){
                    int j = str.indexOf(')');
                    if(j==charArr.length-1){
                        makeTree(node,str.substring(i+1,j),false);
                        break;
                    }else {
                        i = j;
                        cat = true;
                    }
                }
                else {
                    cat = true;
                }
            }
        }
    }

    /*
        flag:  0 -> normal    1->((x)*;a*)    2->base (a;a|b)
     */
    private void divideReg(int bg,int ed,int flag){
        if(ed<=bg) return;
        System.out.println(reg.substring(bg,ed));
        for(int i = bg;i<ed;i++){
            char curCh = reg.charAt(i);
            if(curCh=='(') {//with bracket
                int right = reg.indexOf(')', i + 1);
                if (right + 1 <= reg.length() - 1 && reg.charAt(right + 1) == '*') {
                    //end by '*'
                    divideReg(i,right+2,1);
                    i = right + 1;
                } else {
                    divideReg(i+1,right,0);
                    i = right;
                }
            }else{
                if(i+1==reg.length()){
                    divideReg(i, i + 1,2);
                }else {
                    char next = reg.charAt(i + 1);
                    switch (next) {
                        case '*': {
                            divideReg(i, i + 2,1);
                            i = i + 1;
                            break;
                        }
                        case '|': {
                            divideReg(i, reg.length(),0);
                            break;
                        }
                        default:
                            divideReg(i, reg.length(),0);
                            break;
                    }
                }
            }
        }
    }

    public ALGraph makeNFA(){
        List<Character> vexsdata = new ArrayList<>();
        List<Integer> va = new ArrayList<>();
        List<Integer> vb = new ArrayList<>();
        for (int i = 0; i < reg.length(); i++) {
            char curCh = reg.charAt(i);
            if(curCh=='('){//with bracket
                int right = reg.indexOf(')',i+1);
                if(right+1<=reg.length()-1 && reg.charAt(right+1)=='*'){
                    //end by '*'
                    i = right+1;
                }else{

                    i = right;
                }
            }else if(curCh==')'){

            }else {

            }
        }
        return null;
    }

    public RegExpress(String reg) {
        this.reg = reg;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public String getReg() {
        return reg;
    }
}
