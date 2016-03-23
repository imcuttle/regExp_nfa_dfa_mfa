package xyz.moyuyc.model;

import xyz.moyuyc.graph.ALGraph;

import java.util.List;
import java.util.Set;

/**
 * Created by Yc on 2016/3/21 for compiler2.
 */
public class Convert {

    public static ALGraph regExpressToNFA(String reg){
        return null;
    }

    public static int[] SetToIntArray(Set s){
        int[] arr = new int[s.size()];
        int i = 0;
        for (Object o:s) {
            arr[i++] = (int) o;
        }
        return arr;
    }
    public static int[] ListToIntArray(List l){
        int[] arr = new int[l.size()];
        for (int i = 0; i < l.size(); i++) {
            arr[i] = (int) l.get(i);
        }
        return arr;
    }

    public static char[] ListToCharArray(List l){
        char[] arr = new char[l.size()];
        for (int i = 0; i < l.size(); i++) {
            arr[i] = (char) l.get(i);
        }
        return arr;
    }
}
