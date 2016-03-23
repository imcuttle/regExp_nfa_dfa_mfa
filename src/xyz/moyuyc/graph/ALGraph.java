package xyz.moyuyc.graph;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import xyz.moyuyc.model.Convert;

import java.io.*;
import java.util.*;

/**
 * Created by Yc on 2015/10/19.
 */
public class ALGraph extends Graph implements Serializable{
    public static char BEGIN = 'S';
    public static char END = 'E';
    public static char BE = 'Z';
    public static char NULL = '∑';
    private String title;



    public void setTitle(String title) {
        this.title = title;
    }

    public List<Closure> dfa2nfa;
    private final Set<Integer> weights = new TreeSet<>();
    private void toDFAGraph(int c,List<Character> vexs,List<Integer> va,List<Integer> vb,List<Integer> ws){

    }
    private class Closure implements Cloneable,Serializable{
        Set<Integer> closure = new TreeSet<>();
        List<Integer> fromIndexs;
        int fromWeight;
        public Closure(Object[] fromIndexs,int fromWeight){
            this.fromIndexs = new LinkedList<Integer>();
            for(Object i:fromIndexs)
                this.fromIndexs.add((int)i);
            this.fromWeight=fromWeight;
        }
        public boolean add(Closure c){ return closure.addAll(c.closure); }
        public boolean add(Integer o){return closure.add(o);}
        public boolean isEmpty(){return closure.isEmpty();}
        public Closure getMyClosure(int weight){
            Closure c = new Closure(this.closure.toArray(),weight);
            for(Integer integer:this.closure) {
                Closure in = null;
                if(weight!=ALGraph.NULL)
                    in = getAdjClosure(integer, weight);
                else
                    in = getClosure(integer, weight);
                if(!in.isEmpty()) {
                    c.add(in);
                    if(weight!=ALGraph.NULL)
                        c.add(in.getMyClosure(ALGraph.NULL));
                }
            }
            return c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Closure)) return false;

            Closure closure1 = (Closure) o;

            return closure.equals(closure1.closure);

        }

        @Override
        public int hashCode() {
            return closure.hashCode();
        }

        @Override
        public String toString() {
            return "Closure-> fromIndexs:"+Arrays.toString(fromIndexs.toArray())+"; fromWeight:"+fromWeight+"; closure"+Arrays.toString(closure.toArray());
        }
    }
    private Closure getAdjClosure(int i,int weight){
        Closure c = new Closure(new Object[]{i},weight);
        if(i<adjlist.size()) {
            VexNode vexNode = adjlist.get(i);
            EdgeNode edgeNode = vexNode.firstedge;
            while (edgeNode != null) {
                if(edgeNode.weight==weight) {
                    c.add(edgeNode.adjvex);
                }
                edgeNode = edgeNode.nextedge;
            }
        }
        return c;
    }
    private Closure getClosure(int i,int weight){
        Closure c = new Closure(new Object[]{i},weight);
        if(i<adjlist.size()) {
            VexNode vexNode = adjlist.get(i);
            EdgeNode edgeNode = vexNode.firstedge;
            while (edgeNode != null) {
                if(edgeNode.weight==weight) {
                    if(i!=edgeNode.adjvex)
                        c.add(getClosure(edgeNode.adjvex, weight));
                    c.add(edgeNode.adjvex);
                }
                edgeNode = edgeNode.nextedge;
            }
        }
        return c;
    }
    public ALGraph toDFAGraph(){
        int count = 0;
        List<Closure> dfa2nfa = new ArrayList<>();
        Closure c = getClosure(0,ALGraph.NULL);
        c.add(0);
//        Queue<Closure> queue = new LinkedList<>();
        dfa2nfa.add(c);//queue.add(c);
        List<Character> vexs = new LinkedList<>();List<Integer> va=new LinkedList<>(),vb=new LinkedList<>(),ws=new LinkedList<>();
        Set<Integer> endIndexes = new HashSet<>();
        vexs.add(ALGraph.BEGIN);
        int i = 0;
        while (i<dfa2nfa.size()) {
            c = dfa2nfa.get(i);
            for (Integer w : weights) {
                Closure newc = c.getMyClosure(w);
                System.out.println(newc);
                if(!newc.isEmpty()) {
                    int pos = dfa2nfa.indexOf(newc);
                    if (pos==-1) {
                        char ch = (char) ('A' + count++);
                        ch = ch != ALGraph.BE && ch != ALGraph.BEGIN && ch != ALGraph.END ? ch : (char) ('A' + count++);
                        vexs.add(ch);
                        va.add(i);
                        vb.add(dfa2nfa.size());
                        ws.add(w);
                        dfa2nfa.add(newc);
                    } else {
                        va.add(i);
                        vb.add(pos);
                        ws.add(w);
                    }
                }
            }
            if(c.closure.contains(1)){
                endIndexes.add(i);
            }
            i++;
        }
        ALGraph g = new ALGraph(GraphType.dinetwork, Convert.ListToCharArray(vexs),Convert.ListToIntArray(va),
                Convert.ListToIntArray(vb),Convert.ListToIntArray(ws),"DFA",new int[]{0},
                Convert.SetToIntArray(endIndexes));
        g.dfa2nfa = dfa2nfa;
        return g;
    }
    public ArrayList<VexNode> adjlist=new ArrayList<>();
    protected List<Integer> topoList;
    private int clock;
    public ALGraph getInverseGraph(){
        if(this.type==GraphType.digraph || this.type==GraphType.dinetwork){
            ArrayList<VexNode> newadjlist = new ArrayList<>();
            for (int i = 0; i < this.adjlist.size(); i++)
                newadjlist.add(new VexNode(this.adjlist.get(i).data,null));

            for (int i = 0; i < this.adjlist.size(); i++) {
                EdgeNode edge = this.adjlist.get(i).firstedge;
                while (edge!=null){
                    newadjlist.get(edge.adjvex).firstedge = new EdgeNode(i,edge.weight,newadjlist.get(edge.adjvex).firstedge);
                    edge = edge.nextedge;
                }
            }
            ALGraph newobj = this.deepClone();
            newobj.adjlist=newadjlist;
            return newobj;
        }else
           return this.deepClone();
    }
    public ALGraph(){}
    public ALGraph deepClone() {
        ALGraph newone = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            newone = (ALGraph) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return newone;
        }
    }

    @Override
    /**
     * json string {type: ,vertexes:[{data:'',status:'',edges:[{vertex: ,weight:}]}]}
     */
    public String toString() {
        try {
            int i = 0;
            while (topoList==null)
                dfsNoComponent(i++);
            System.out.println(Arrays.toString(topoList.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject graph = new JSONObject();
        graph.put("type", this.type.name());
        graph.put("topolist", this.topoList);
        graph.put("title", this.title);
        JSONArray vertexes = new JSONArray();

        for (int i = 0; i < adjlist.size(); i++) {
            JSONObject vertex = new JSONObject();
            if(garbage!=null&&garbage.contains(i)){
                vertexes.add(vertex);
                continue;
            }
            VexNode vex = adjlist.get(i);
            vertex.put("data", vex.data);
            vertex.put("status", vex.status.name());
            EdgeNode edge = vex.firstedge;
            JSONArray edges = new JSONArray();
            while (edge != null) {
                JSONObject e = new JSONObject();
                e.put("vertex", edge.adjvex);
                e.put("weight", edge.weight);
                edges.add(e);
                edge = edge.nextedge;
            }
            vertex.put("edges", edges);
            vertexes.add(vertex);
        }
        graph.put("vertexes",vertexes);
        return graph.toString();
    }

    public ALGraph(GraphType type,char[] vexsdata,int[] va,int[] vb,int[] weights,String title,int[] startIndexs,int[] endIndexs){
        if(va.length!=vb.length) return;
        this.title = title;
        vexnum=vexsdata.length;
        edgenum=va.length;
        this.type=type;

        startIndexs = startIndexs!=null?startIndexs:new int[]{};
        endIndexs = endIndexs!=null?endIndexs:new int[]{};


        //初始化adjlist
        for(int i=0;i<vexnum;i++){
            int a =Arrays.binarySearch(startIndexs, i),b=Arrays.binarySearch(endIndexs, i);
            if(a>=0&&b>=0){
                adjlist.add(new VexNode(vexsdata[i], null,VexStatus.startend));
            }else if(a>=0){
                adjlist.add(new VexNode(vexsdata[i], null,VexStatus.start));
            }else if(b>=0){
                adjlist.add(new VexNode(vexsdata[i], null,VexStatus.end));
            }else {
                adjlist.add(new VexNode(vexsdata[i], null,VexStatus.other));
            }
        }
        for(int j=0;j<edgenum;j++){
            switch (this.type){
                case undigraph:
                    adjlist.get(va[j]).firstedge=new EdgeNode(vb[j],0,adjlist.get(va[j]).firstedge);
                    adjlist.get(vb[j]).firstedge=new EdgeNode(va[j],0,adjlist.get(vb[j]).firstedge);
                    break;
                case digraph:
                    adjlist.get(va[j]).firstedge=new EdgeNode(vb[j],0,adjlist.get(va[j]).firstedge);
                    break;
                case undinetwork:
                    break;
                case dinetwork:
                    adjlist.get(va[j]).firstedge=new EdgeNode(vb[j],weights[j],adjlist.get(va[j]).firstedge);
                    break;
                default:
                    return;
            }
            if(weights[j]!=ALGraph.NULL)
                this.weights.add(weights[j]);
        }
    }

    public int getWeight(int startIndex,int endIndex){
        if(startIndex>=adjlist.size())
            throw new ArrayIndexOutOfBoundsException(startIndex+" over adjlist's size!");
        if(endIndex>=adjlist.size())
            throw new ArrayIndexOutOfBoundsException(endIndex+" over adjlist's size!");
        EdgeNode node = adjlist.get(startIndex).firstedge;
        while(node!=null){
            if(node.adjvex==endIndex)
                return node.weight;
            node=node.nextedge;
        }
        return Integer.MAX_VALUE;
    }


    public void dfsNoComponent(int s){
        int[] prev = new int[vexnum],post = new int[vexnum];
        clock = 1;
        topoList = new LinkedList<>();
        //if(prev[s]==0)//not visited
        explore(prev, post, s);
        System.out.println();
    }


    private void explore(int[] prev, int[] post, int i) {
        prev[i] = clock++;
        ALGraph.EdgeNode edge = adjlist.get(i).firstedge;
        while (edge!=null){
            if(prev[edge.adjvex]==0)
                explore(prev,post,edge.adjvex);
            edge = edge.nextedge;
        }
        topoList.add(0,i);
        System.out.print(adjlist.get(i).data + " ");//PREV:"+prev[i]+" POST:"+clock);
        post[i]=clock++;
    }

    public List<Integer> topoSort() throws Exception {
        if(type==GraphType.undigraph||type==GraphType.undinetwork)
            throw new Exception("undigraph and undinetwork can't do toposort");
        int[] indegree = new int[vexnum];
        //initial indegree array
        for(VexNode vexNode: adjlist){
            EdgeNode edgeNode=vexNode.firstedge;
            while (edgeNode!=null){
                indegree[edgeNode.adjvex]++;
                edgeNode=edgeNode.nextedge;
            }
        }

        Queue<Integer> zeroQueue = new LinkedList<>();
        //load the index whose indegee is 0 to zeroQueue
        for(int i=0;i<indegree.length;i++){
            if(indegree[i]==0)
                zeroQueue.offer(i);
        }
        List<Integer> topoList = new ArrayList<>();
        while(!zeroQueue.isEmpty()){
            int index=zeroQueue.poll();
            topoList.add(index);
            //refresh indegree
            EdgeNode node=adjlist.get(index).firstedge;
            while (node!=null){
                indegree[node.adjvex]--;
                if(indegree[node.adjvex]==0)
                    zeroQueue.offer(node.adjvex);
                node=node.nextedge;
            }
        }
        this.topoList=topoList;
        return topoList;
    }
    public Object[] shortestPathDP() throws Exception {
        if(topoList==null)
            topoSort();
        int[] dist=new int[vexnum];
        for(int i=1;i<vexnum;i++){
            dist[i]=Integer.MAX_VALUE;
        }
        int[] path=new int[vexnum];
        for(int i=1;i<topoList.size();i++){
            for(int j=i-1;j>=0;j--){
                int d = getWeight(topoList.get(j),topoList.get(i));
                if(d!=Integer.MAX_VALUE && dist[j]+d<dist[i]){
                    path[topoList.get(i)]=topoList.get(j);
                    dist[i] = d + dist[j];
                }
            }
        }
        Object[] list=new Object[2];
        list[0]=dist[dist.length-1];
        list[1]=path;
        return list;
    }


    public class VexNode implements Serializable{
        public char data;
        public EdgeNode firstedge;
        public VexStatus status;
        public VexNode(char data, EdgeNode firstedge,VexStatus status) {
            this.data = data;
            this.firstedge = firstedge;
            this.status = status;
        }
        public VexNode(char data, EdgeNode firstedge) {
            this.data = data;
            this.firstedge = firstedge;
        }
        public VexNode deepClone(){
            if(firstedge!=null)
                return new VexNode(data,firstedge.deepClone(),this.status);
            return new VexNode(data,null,this.status);
        }

        @Override
        public String toString() {
            return "data="+data;
        }
    }


    public class EdgeNode implements Serializable{
        public int adjvex;
        public int weight;
        public EdgeNode nextedge;
        public EdgeNode(){}
        public EdgeNode(int adjvex, int weight, EdgeNode nextedge) {
            this.adjvex = adjvex;
            this.weight = weight;
            this.nextedge = nextedge;
        }
        public EdgeNode deepClone(){
            if(nextedge!=null)
                return new EdgeNode(adjvex,weight,nextedge.deepClone());
            return new EdgeNode(adjvex,weight,null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EdgeNode)) return false;

            EdgeNode edgeNode = (EdgeNode) o;

            if (adjvex != edgeNode.adjvex) return false;
            if (weight != edgeNode.weight) return false;
            if(nextedge!=null)
                return nextedge.equals(edgeNode.nextedge);
            else
                return edgeNode.nextedge==null;

        }

        @Override
        public int hashCode() {
            int result = adjvex;
            result = 31 * result + weight;
            result = 31 * result + nextedge.hashCode();
            return result;
        }

        @Override
        public String toString() {
            if(this.nextedge==null)
                return "";
            else
                return "index="+this.adjvex+" "+adjlist.get(adjvex)+" --> "+"index="+this.nextedge.adjvex+" "+adjlist.get(this.nextedge.adjvex);
        }
    }


    public ALGraph toMFAGraph(){
        LinkedList<Set<Integer>> groups = new LinkedList<>();
        Set<Integer> set1 = new TreeSet<>();
        Set<Integer> set2 = new TreeSet<>();
        for (int i = 0; i < adjlist.size(); i++) {
            VexNode vex = adjlist.get(i);
            if(vex.status==VexStatus.start||vex.status==VexStatus.other){
                set1.add(i);
            }else{
                set2.add(i);
            }
        }
        groups.add(set1);groups.add(set2);

        for(int i = 0; i<groups.size();i++){
            Set<Integer> set = groups.getFirst();
            Map<Integer,Set<Integer>> nomap = new HashMap<>();
            boolean divide = false;
            if(set.size()==1){
                groups.addLast(groups.removeFirst());
                continue;
            }
            for(int w:weights){
                nomap.clear();
                for(int vexi:set){
                    VexNode vex = adjlist.get(vexi);
                    EdgeNode e = vex.firstedge;
                    boolean found = false;
                    while (e!=null){
                        if(e.weight==w){
                            found = true;
                            int no = getGroupIndex(groups, e.adjvex);
                            //System.out.println(adjlist.get(vexi).data+"->"+(char)e.weight+"->"+adjlist.get(e.adjvex).data+" "+no);
                            Set<Integer> l = nomap.get(no);
                            if(l==null) {
                                l =  new TreeSet<>();l.add(vexi);
                                nomap.put(no,l);
                            }
                            else
                                l.add(vexi);
                        }
                        e = e.nextedge;
                    }
                    if(!found){
                        int no = -1;
                        Set<Integer> l = nomap.get(no);
                        if(l==null) {
                            l =  new TreeSet<>();l.add(vexi);
                            nomap.put(no,l);
                        }
                        else
                            l.add(vexi);
                    }
                }
                if(nomap.size()>1){
                    groups.removeFirst();
                    for(Map.Entry entry:nomap.entrySet()) {
                        Set<Integer> l = (Set<Integer>)entry.getValue();
                        groups.addLast(l);
                    }
                    i=-1;
                    //System.out.println(groups.size());
                    divide = true;
                    break;
                }
            }
            if(!divide){
                groups.addLast(groups.removeFirst());
            }
        }

        ALGraph g = this.deepClone();
        g.garbage = new LinkedList<>();
        for (Set<Integer> group:groups){
            if(group.size()==1) continue;
            Iterator<Integer> it = group.iterator();
            if(it.hasNext()) {
                int star = it.next();
                for (int i = 0; i < g.adjlist.size(); i++) {
                    if (i!=star&&group.contains(i)) continue;
                    VexNode vex = g.adjlist.get(i);
                    EdgeNode e = vex.firstedge;
                    while (e != null) {
                        if (e.adjvex != star && group.contains(e.adjvex)) {
                            e.adjvex = star;
                        }
                        e = e.nextedge;
                    }
                }
            }
            while (it.hasNext()){
                g.garbage.add(it.next());
            }
        }
        g.topoList = null;
        g.title = "MFA";

        return g;
    }
    private int getGroupIndex(LinkedList<Set<Integer>> groups,int v){
        for (int i = 0; i < groups.size(); i++) {
            if(groups.get(i).contains(v))
                return i;
        }
        return -1;
    }
    List<Integer> garbage;
}



