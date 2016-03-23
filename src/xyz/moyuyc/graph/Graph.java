package xyz.moyuyc.graph;

import java.io.Serializable;

/**
 * Created by Yc on 2015/12/9.
 */
public class Graph implements Serializable{
    public enum GraphType{
        undigraph,digraph,undinetwork,dinetwork
    }
    public int vexnum;
    public int edgenum;
    public GraphType type;
    public Graph(){}
}

