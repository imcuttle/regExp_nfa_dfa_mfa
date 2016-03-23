package xyz.moyuyc.model;

import xyz.moyuyc.graph.ALGraph;
import xyz.moyuyc.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yc on 2016/3/22 for compiler2.
 */
public class BinTree {
    public enum NodeType{OPERATION,DATA}
    public enum OpType{CONCAT,OR,ANY}

    NodeType nodeType;
    OpType opType;
    String data;
    public BinTree left;
    public BinTree right;
    public BinTree parent;
    public void print(){
        print(this);
    }


    public ALGraph makeNFAGraph(String title){
        List<Character> vexs = new ArrayList<>();
        List<Integer> va = new ArrayList<>();
        List<Integer> vb = new ArrayList<>();
        List<Integer> ws = new ArrayList<>();
        vexs.add(ALGraph.BEGIN);
        vexs.add(ALGraph.END);
//        va.add(0);
//        vb.add(1);
        c = 0;

        makeALGraph(this, vexs, va, vb, ws,0,1);
        ALGraph g = new ALGraph(
                Graph.GraphType.dinetwork,Convert.ListToCharArray(vexs),Convert.ListToIntArray(va),
                Convert.ListToIntArray(vb),Convert.ListToIntArray(ws),title,new int[]{0},new int[]{1});
        return g;
    }
    private int c = 0;
    private void makeALGraph(BinTree node,List<Character> vexs,List<Integer> va,List<Integer> vb,List<Integer> ws,int bgi,int edi){
        if(node == null){
            va.add(bgi);
            vb.add(edi);
            ws.add(((int) ALGraph.NULL));
            return;
        }
        if(node.nodeType==NodeType.DATA){
            char data = node.data.charAt(0);
            va.add(bgi);
            vb.add(edi);
            ws.add(((int) data));
        }else if(node.nodeType==NodeType.OPERATION){
            OpType op = node.opType;
            if(op==OpType.CONCAT){
//                if(node.left.opType == OpType.ANY) {
//                    makeALGraph(node.left, vexs, va, vb, ws, bgi,edi);
//                    return;
//                }
                char ch = (char) ('A' + c++);
                ch = ch != ALGraph.BEGIN && ch != ALGraph.END && ch!=ALGraph.BE ? ch : (char) ('A' + c++);
                vexs.add(ch);
                int t = vexs.size()-1;
                makeALGraph(node.left, vexs, va, vb, ws, bgi,t);
                makeALGraph(node.right, vexs, va, vb, ws, t, edi);
            }else if(op==OpType.OR){
                makeALGraph(node.left, vexs, va, vb, ws, bgi,edi);
                makeALGraph(node.right, vexs, va, vb, ws,bgi,edi);
            }else if(op==OpType.ANY){
                char ch = (char) ('A' + c++);
                ch = ch != ALGraph.BEGIN && ch != ALGraph.END && ch!=ALGraph.BE ? ch : (char) ('A' + c++);
                vexs.add(ch);
                int t = vexs.size()-1;
                va.add(bgi);
                vb.add(t);
                ws.add(((int) ALGraph.NULL));
                va.add(t);
                vb.add(edi);
                ws.add(((int) ALGraph.NULL));
                makeALGraph(node.left, vexs, va, vb, ws, t, t);
            }
        }
    }
    @Override
    public String toString() {
        return "[nodeType:"+this.nodeType+",opType:"+this.opType+",data:"+this.data+"]";
    }

    private void print(BinTree node){
        System.out.println(node);
        if(node.left!=null)
            print(node.left);
        if(node.right!=null)
            print(node.right);
    }
}
