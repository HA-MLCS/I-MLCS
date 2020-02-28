package arlp.mlcs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import arlp.mlcs.util.Logger;
import arlp.mlcs.util.Queues;
import arlp.mlcs.util.Stopwatch;

/**
 *
 */
public class Graph {

    private Mlcs mlcs;
    // Location -> Node
    public volatile Map<Location, Node> location2Nodes = new ConcurrentHashMap<Location, Node>();
    public Set<Location> keyLocs; //key locations
    private Set<Location> heads = new java.util.HashSet<Location>(4);//

    private int maxLevel = -1;// max length of sequences
    private BigDecimal matchedCount = new BigDecimal(0); // number of result
    public LinkedList<Node> toDelete = new LinkedList<>();

    private Node start = null; // startNode
    private Node end = null; // endNode
    private Logger logger = Logger.getLogger(Graph.class);

    public int getSize() {
        return location2Nodes.size();
    }

    public Graph(Mlcs mlcs) {
        this.mlcs = mlcs;
        start = newNode(mlcs.start);
        end = newNode(mlcs.end);
    }

    synchronized public Node get(Location loc) {
        return location2Nodes.get(loc);
    }

    /**
     *
     */

    public boolean accessHead(Location start, Location to) {
        Node s = get(start);
        s.successors.add(to);

        if (heads.contains(to)) {
            return false;
        } else {
            heads.add(to);
            Node n = location2Nodes.get(to);
            if (null == n) {
                n = newNode(to);
                n.predecessors.add(s);
                n.indegree = 0;
                return true;
            } else {
                n.predecessors.add(s);
                return false;
            }
        }
    }


    public int size() {
        return location2Nodes.size();
    }

    /**
     * add location to graph if the graph has location to
     *
     * @return true if to has not be visited
     */
    public boolean addEdge(Location start, Location to) {
        Node n = location2Nodes.get(to);
        Node s = location2Nodes.get(start);
        if (null == n) {
            n = newNode(to);
            s.successors.add(to);
            n.predecessors.add(s);
            return true;
        } else {
            n.inc();
            s.successors.add(to);
            n.predecessors.add(s);
            return false;
        }
    }


    public void addLocation(Location loc) {
        if (!location2Nodes.containsKey(loc)) {
            Node n = newNode(loc);
            n.indegree = 0;
        }
    }

    public boolean exists(Location loc) {
        return location2Nodes.containsKey(loc);
    }


    /**
     * topological sorting for all nodes
     */
    @SuppressWarnings("serial")
    static class LevelCrawler extends RecursiveTask<List<Location>> {
        Graph graph;
        ArrayList<Location> locations;
        int from, to, level;

        public LevelCrawler(Graph graph, int level, ArrayList<Location> locations, int from, int to) {
            super();
            this.graph = graph;
            this.level = level;
            this.locations = locations;
            this.from = from;
            this.to = to;
        }

        public List<Location> compute() {

            List<Location> nexts = new LinkedList<Location>();
            for (int i = from; i < to; i++) {
                Location loc = locations.get(i);
                Node fromNode = graph.get(loc);

                //       List<Location> successors = graph.mlcs.nextLocations(loc);
                List<Location> successors = fromNode.successors;
                if (successors.isEmpty()) {
                    graph.end.addEndPredecessor(fromNode, level + 1);
                } else {
                    for (Location to : successors) {
                        Node toNode = graph.get(to);
                        if (null != toNode && toNode.addPredecessor(fromNode, level + 1))
                            nexts.add(to); //if indegree is  0, add to next iteration
                    }
                }
            }
            return nexts;
        }
    }


    /**
     * according to indegree of node，
     * 1）set level
     * 2）add predecessors
     */
    private void setLevel() {
        maxLevel = -1;
        linkKeyNodes();

        System.out.println("heads: "+heads);

        long startTime = System.nanoTime();
        ForkJoinPool pool = null;
        if (mlcs.maxThread > 0) pool = new ForkJoinPool(mlcs.maxThread);
        else pool = new ForkJoinPool();

        ArrayList<Location> levelQueue = new ArrayList<Location>();
        levelQueue.add(mlcs.start);

        int parallelism = pool.getParallelism();
        while (!levelQueue.isEmpty()) {
            maxLevel += 1;
            List<int[]> segs = Queues.split(levelQueue.size(), parallelism);
            LinkedList<ForkJoinTask<List<Location>>> tasks = new java.util.LinkedList<ForkJoinTask<List<Location>>>();
            for (int[] seg : segs) {
                tasks.add(pool.submit(new LevelCrawler(this, maxLevel, levelQueue, seg[0], seg[1])));
            }
            levelQueue = new ArrayList<Location>();
            for (ForkJoinTask<List<Location>> task : tasks) {
                levelQueue.addAll(task.join());
            }

        }
        pool.shutdown();
        if (logger.isDebugEnabled()) {
            logger.debug("set level using " + Stopwatch.format(System.nanoTime() - startTime));
        }
    }

    /**
     * find key locations(exclude startNode and endNode）
     */
    private void findKeyLocs() {
        setLevel();

        //System.out.println(location2Nodes.values());

        long startTime = System.nanoTime();
        keyLocs = new java.util.HashSet<Location>();
        LinkedList<Location> queue = new java.util.LinkedList<Location>();
        int tlevel = 0;
        // the numbers of paths from endNode to  the node, the value of  endNode is 1
        Map<Location, BigDecimal> routeCounts = new java.util.HashMap<Location, BigDecimal>();
        routeCounts.put(end.loc, new BigDecimal(1));

        Location queueEnd = mlcs.end;
        Location nextQuenEnd = null;
        queue.addLast(mlcs.end);

        while (!queue.isEmpty()) {
            Location loc = queue.removeFirst();
            Node node = location2Nodes.get(loc);
            for (Node p : node.predecessors) {
                Location ploc = p.loc;
                if (p.level + tlevel == maxLevel) {
                    if (keyLocs.contains(ploc)) {
                        routeCounts.put(ploc, routeCounts.get(ploc).add(routeCounts.get(loc)));
                    } else {
                        keyLocs.add(ploc);
                        nextQuenEnd = ploc;
                        routeCounts.put(ploc, routeCounts.get(loc));
                        queue.addLast(nextQuenEnd);
                    }
                }
            }
            if (loc == queueEnd) {
                queueEnd = nextQuenEnd;
                tlevel += 1;
            }
        }


        keyLocs.remove(start.loc);
        keyLocs.remove(end.loc);
        matchedCount = routeCounts.get(start.loc);
        if (logger.isDebugEnabled()) {
            logger.debug("find key location using " + Stopwatch.format(System.nanoTime() - startTime));
        }
    }


    /**
     * statistics result
     */
    public Mlcs.Result stat() {
        findKeyLocs();
        return new Mlcs.Result(matchedCount, maxLevel);
    }


    private void linkKeyNodes() {
        long startTime = System.nanoTime();
        start.indegree = 0;

        if (!heads.isEmpty() || null != keyLocs && !keyLocs.contains(mlcs.start)) {
            for (Location head : mlcs.nextLocations(mlcs.start)) {
                if (heads.contains(head)) {
//                    heads.add(head);
                    location2Nodes.get(head).inc();
                }
            }
        }

        if (null == keyLocs) return;
        for (Location loc : keyLocs) {
            for (Location next : mlcs.nextLocations(loc)) {
                Node node = location2Nodes.get(next);
                if (null != node) node.inc();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("merge map(" + keyLocs.size() + " locations) using "
                    + Stopwatch.format(System.nanoTime() - startTime));
        }
    }

    @SuppressWarnings("unused")
    private void printState() {
        List<Location> locations = new ArrayList<Location>(location2Nodes.keySet());
        Collections.sort(locations, mlcs.sorter);
        for (Location loc : locations) {
            Node node = location2Nodes.get(loc);
            System.out.println(loc + " indgre:" + node.indegree);
        }
    }

    /**
     * print paths
     */
    public List<List<Location>> printPathResult() {
        List<List<Location>> paths = findKeyPaths();
        for (List<Location> path : paths) {

            for (Location l : path) {
//			  System.out.print(mlcs.charAt(l) + "(" + l + ")");
                l.setName(mlcs.charAt(l));
            }
//		  System.out.println();
        }
        return paths;
    }

    public List<List<Location>> savePath() {
        return findKeyPaths();
    }


    public List<List<Location>> findKeyPaths() {
        LinkedList<List<Location>> results = new LinkedList<List<Location>>();
        collectPath(end, new LinkedList<Location>(), results);
        return results;
    }

    /**
     * collect result
     */
    private void collectPath(Node n, List<Location> suffix, LinkedList<List<Location>> result) {
        int maxLevel = n.level - 1;
        List<Node> predecessors = new ArrayList<Node>();
        for (Node p : n.predecessors) {
            if (p.level == maxLevel) predecessors.add(p);
        }
        for (Node e : predecessors) {
            List<Location> newPaths = new ArrayList<Location>();
            newPaths.add(e.loc);
            newPaths.addAll(suffix);
            if (!e.predecessors.isEmpty()) {
                if (e.predecessors.contains(start)) {
                    result.add(newPaths);
                } else {
                    collectPath(e, newPaths, result);
                }
            }
        }
    }

    public List<List<Location>> findKeyPartialPaths() {
        LinkedList<List<Location>> results = new LinkedList<List<Location>>();
        collectPartialPath(end, new LinkedList<Location>(), results);
        return results;
    }

    private void collectPartialPath(Node n, List<Location> suffix, LinkedList<List<Location>> result) {
        if (result.size() >= 30)
            return;
        int maxLevel = n.level - 1;
        List<Node> predecessors = new ArrayList<Node>();
        for (Node p : n.predecessors) {
            if (p.level == maxLevel) predecessors.add(p);
        }
        for (Node e : predecessors) {
            List<Location> newPaths = new ArrayList<Location>();
            newPaths.add(e.loc);
            newPaths.addAll(suffix);
            if (!e.predecessors.isEmpty()) {
                if (e.predecessors.contains(start)) {
                    result.add(newPaths);
                } else {
                    collectPartialPath(e, newPaths, result);
                }
            }
            if (result.size() >= 30)
                break;
        }
    }

//    private void collectPartialPath() {
//        LinkedList<NodePair> stack = new java.util.LinkedList<NodePair>();
//        int num = 0;
//        stack.addLast(new NodePair(end, 0));
//        while (num < 30) {
//            NodePair np = stack.getLast();
//            Node node = null;
//            while (np.predeNum < np.node.predecessors.size()) {
//                node = np.node.predecessors.get(np.predeNum);
//                if (keyLocs.contains(node.loc)) {
//
//                }
//            }
//
//
//            num++;
//        }
//
//        Map<Location, BigDecimal> routeCounts = new java.util.HashMap<Location, BigDecimal>();
//        routeCounts.put(end.loc, new BigDecimal(1));
//
//        Location queueEnd = mlcs.end;
//        Location nextQuenEnd = null;
//        queue.addLast(mlcs.end);
//
//        while (!queue.isEmpty()) {
//            Location loc = queue.removeFirst();
//            Node node = location2Nodes.get(loc);
//            for (Node p : node.predecessors) {
//                Location ploc = p.loc;
//                if (p.level + tlevel == maxLevel) {
//                    if (keyLocs.contains(ploc)) {
//                        routeCounts.put(ploc, routeCounts.get(ploc).add(routeCounts.get(loc)));
//                    } else {
//                        keyLocs.add(ploc);
//                        nextQuenEnd = ploc;
//                        routeCounts.put(ploc, routeCounts.get(loc));
//                        queue.addLast(nextQuenEnd);
//                    }
//                }
//            }
//            if (loc == queueEnd) {
//                queueEnd = nextQuenEnd;
//                tlevel += 1;
//            }
//        }
//
//
//        keyLocs.remove(start.loc);
//        keyLocs.remove(end.loc);
//        matchedCount = routeCounts.get(start.loc);
//        if (logger.isDebugEnabled()) {
//            logger.debug("find key location using " + Stopwatch.format(System.nanoTime() - startTime));
//        }
//    }

    static class NodePair {
        public Node node;
        public int predeNum;

        public NodePair(Node node, int predeNum) {
            this.node = node;
            this.predeNum = predeNum;
        }
    }


    public synchronized Node newNode(Location to) {
        Node n = new Node(to);
        location2Nodes.put(to, n);
        if (!location2Nodes.containsKey(to)) {
            System.out.println("ssssssssss");
        }
        return n;
    }


    static class Node {
        public Location loc;   //location
        public short indegree;
        public short level;
        public short outdegree;
        public boolean toExpand;

        public List<Node> predecessors = new ArrayList<Node>();

        public List<Location> successors = new ArrayList<Location>();


        public Node(Location loc) {
            this.indegree = 1;
            this.loc = loc;
            this.outdegree = 0;
        }

        public Node reset() {
            this.predecessors = new ArrayList<Node>();
            this.indegree = 0;
            this.level = 0;
            return this;
        }

        public void inc() {
            indegree = (short) (indegree + 1);
        }

        synchronized public void outdegreeInc() {
            outdegree = (short) (outdegree + 1);
        }

        synchronized public boolean outdegreeDes() {
            outdegree--;
            return outdegree == 0;
        }

        /**
         * add predecessor for endNode
         */
        public synchronized void addEndPredecessor(Node predecessor, int newLevel) {
            if (this.level < newLevel) {
                this.level = (short) newLevel;
                predecessors = new ArrayList<Node>();
            }
            predecessors.add(predecessor);
        }

        /**
         * add predecessor
         */
        public synchronized boolean addPredecessor(Node predecessor, int newLevel) {
            if (this.level < newLevel) {
                this.level = (short) newLevel;
                predecessors = new ArrayList<Node>();
            }
            predecessors.add(predecessor);
            indegree -= 1;
            return indegree == 0;
        }


        @Override
        public String toString() {
            return loc + " " + indegree + ":" + level + " p(" + predecessors.size() + ")";
        }

    }
}
