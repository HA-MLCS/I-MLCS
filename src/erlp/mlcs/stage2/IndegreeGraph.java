package erlp.mlcs.stage2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import erlp.mlcs.Border;
import erlp.mlcs.Graph;
import erlp.mlcs.InNode;
import erlp.mlcs.Location;
import erlp.mlcs.Mlcs;
import erlp.mlcs.SubGraph;
import erlp.mlcs.util.LogWriter;
import erlp.mlcs.util.Queues;
import erlp.mlcs.util.Seilize;
import erlp.mlcs.util.Stopwatch;

/**
 * By marking the individual graph of each node with degree of entry, the hierarchy of all nodes is set by topological sorting
 */
public class IndegreeGraph extends Graph {
    private List<SubGraph> subgraphs = new ArrayList<SubGraph>();

    public Set<Location> keyLocs; //

    private BigDecimal matchedCount = new BigDecimal(0); // Number of matched results
    private BigDecimal keyNodeCount = new BigDecimal(0);  //Number of key node

    private int serializeIndex = 0;

    private byte[] className;

    public IndegreeGraph(Mlcs mlcs) {
        super(mlcs);
    }

    /**
     * Find the current point from the graph, deserialize if it does not exist
     */
    public InNode find(Location loc) throws Exception {
        InNode node = null;
        for (SubGraph sg : subgraphs) {
            node = sg.location2Nodes.get(loc);
            if (null != node)
                return node;
        }

        if (node == null) {
            while (serializeIndex > 0 && node == null) {
                SubGraph map = (SubGraph) Seilize.deserializeObject(className,
                        serializeIndex);
//				System.out.println("serializeIndex" + serializeIndex);
                subgraphs.add(map);
                serializeIndex -= 1;
                node = map.location2Nodes.get(loc);
//				if(node != null)
//				System.out.println(serializeIndex + ":loc:" + node.loc);
            }
        }
        return node;
    }

    private void removeUnusedGraph(short level) {
        if (level < 1)
            return;
        List<SubGraph> removed = new ArrayList<SubGraph>();
        for (SubGraph graph : subgraphs) {
            if (graph.minLevel > level)
                removed.add(graph);
        }
        if (removed.size() > 0) {
//			LogWriter.log("removed " + removed + " on level " + level);
            subgraphs.removeAll(removed);
            if (subgraphs.isEmpty()) {
                SubGraph sg = new SubGraph(0, (short) 0, (short) 0,
                        new HashMap<Location, InNode>());
                sg.location2Nodes.put(start.loc, start);
                subgraphs.add(sg);
            }
        }
    }

    public boolean addEdge(Location start, Location to, short nextLevel) {
        if (to == mlcs.end) {
            end.updateLevel(start, nextLevel, this);
            return false;
        } else {
            InNode n = location2Nodes.get(to);
            if (null == n) {
                n = newNode(to);
                n.level = nextLevel;
                n.predecessors.add(start);
                return true;
            } else {
                n.updateLevel(start, nextLevel, this);
                return false;
            }
        }
    }

    /**
     * Add a point to the graph
     */
    public boolean addEdge(Location start, Location to) {
        InNode n = location2Nodes.get(to);
        if (null == n) {

//			System.out.println(to);
            n = newNode(to);
            n.inc();

            return true;
        } else {
            n.inc();
            return false;
        }
    }

    public InNode addLocation(Location loc) {
        InNode n = location2Nodes.get(loc);
        if (null == n) {
            n = newNode(loc);
            n.indegree = 0;
        }
        return n;
    }

    public boolean exists(Location loc) {
        return location2Nodes.containsKey(loc);
    }

    /**
     * Topological Sorting Tasks for Each Node
     */
    @SuppressWarnings("serial")
    static class LevelCrawler extends RecursiveTask<Map<Short, List<Location>>> {
        IndegreeGraph graph;
        ArrayList<Location> locations;
        int from, to, level;

        public LevelCrawler(IndegreeGraph graph, int level,
                            ArrayList<Location> locations, int from, int to) {
            super();
            this.graph = graph;
            this.level = level;
            this.locations = locations;
            this.from = from;
            this.to = to;
        }

        @Override
        public Map<Short, List<Location>> compute() {
            Map<Short, List<Location>> nexts = new HashMap<Short, List<Location>>();
            for (int i = from; i < to; i++) {
                Location fromLoc = locations.get(i);
                List<Location> successors = graph.mlcs.nextLocations(fromLoc);
                if (successors.isEmpty()) {
                    graph.end.addPredecessor(fromLoc, level);
                } else {
                    for (Location to : successors) {
                        InNode toNode = graph.get(to);
                        if (toNode.addPredecessor(fromLoc, level)) {          //After adding the precursor, the indegree decreases by one, and whether the indegree is 0 or not
                            List<Location> levelNexts = nexts.get(toNode.level);
                            if (null == levelNexts) {
                                levelNexts = new ArrayList<Location>();
                                nexts.put(toNode.level, levelNexts);
                            }
                            levelNexts.add(to);
                        }
                    }
                }
            }
            return nexts;
        }
    }


    /**
     * Save the necessary information for serialization
     */

    public void delete5(Map<Location, Border.LevelInfo> nodes) throws Exception {
        long startTime = System.nanoTime();


        Set<Location> keyloction = new HashSet<>();
        for (Map.Entry<Location, Border.LevelInfo> entry : nodes.entrySet()) {
            Border.LevelInfo l = entry.getValue();

            for (int i = 0; i < l.predecessors.size(); i++) {
                Location location = l.predecessors.get(i);

                keyloction.add(location);

            }
        }

        preLocations = boundLocations;
        boundLocations = new HashSet<>();
        boundLocations.addAll(keyloction);


        if (logger.isDebugEnabled()) {
            logger.debug("find key location using "
                    + Stopwatch.format(System.nanoTime() - startTime));
        }
    }


    public InNode find2(Location loc) throws Exception {
        InNode node = location2Nodes.get(loc);


        return node;
    }

    /**
     * According to the indegree of node, 1) setting the hierarchy, 2) setting the successors of nodes.
     */
    public short[] setLevelByTopsort(Border border) {
        Map<Short, List<Location>> heads = findTopsortStarts(border);
        if (heads.isEmpty())
            return new short[]{0, 0};

        short nextLevel = Short.MAX_VALUE;
        long startTime = System.nanoTime();
        short maxLevel = 0;
        for (Short level : heads.keySet()) {
            if (level > maxLevel) {
                maxLevel = level;
            }
            if (level < nextLevel)
                nextLevel = level;
        }
        final short minLevel = nextLevel;

        ForkJoinPool pool = null;
        if (mlcs.maxThread > 0)
            pool = new ForkJoinPool(mlcs.maxThread);
        else
            pool = new ForkJoinPool();
        int parallelism = pool.getParallelism();

        ArrayList<Location> levelQueue = (ArrayList<Location>) heads
                .get(nextLevel);
        while (!levelQueue.isEmpty() || nextLevel <= maxLevel) {
            //LogWriter.log(nextLevel + "正:" + levelQueue.size());
            nextLevel += 1;
            List<int[]> segs = Queues.split(levelQueue.size(), parallelism);
            LinkedList<ForkJoinTask<Map<Short, List<Location>>>> tasks = new java.util.LinkedList<ForkJoinTask<Map<Short, List<Location>>>>();
            for (int[] seg : segs) {
                tasks.add(pool.submit(new LevelCrawler(this, nextLevel,
                        levelQueue, seg[0], seg[1])));
            }
            levelQueue = new ArrayList<Location>();
            if (heads.containsKey(nextLevel))
                levelQueue.addAll(heads.get(nextLevel));

            for (ForkJoinTask<Map<Short, List<Location>>> task : tasks) {
                Map<Short, List<Location>> nexts = task.join();
                // Some of the nodes with 0 indegree after each traversal are just used next time, and some are only used later.
                for (Map.Entry<Short, List<Location>> e : nexts.entrySet()) {
                    if (e.getKey() > maxLevel) {
                        maxLevel = e.getKey();
                    }
                    if (e.getKey().shortValue() == nextLevel) {
                        levelQueue.addAll(e.getValue());
                    } else {
                        List<Location> otherLevelLocs = heads.get(e.getKey());
                        if (null == otherLevelLocs) {
                            if (e.getValue() instanceof ArrayList<?>) {
                                heads.put(e.getKey(), e.getValue());
                            } else {
                                heads.put(e.getKey(),
                                        new ArrayList<Location>(e.getValue()));
                            }
                        } else {
                            otherLevelLocs.addAll(e.getValue());
                        }
                    }
                }
            }
        }
        pool.shutdown();
        if (logger.isDebugEnabled()) {
            logger.debug("set level using "
                    + Stopwatch.format(System.nanoTime() - startTime));
        }

        return new short[]{minLevel, (short) (end.level - 1)};
    }

    /**
     * Finding Key Nodes（exclude start node and end node）
     *
     * @throws Exception
     */
    private void findKeyLocs() throws Exception {
        long startTime = System.nanoTime();
        keyLocs = new java.util.HashSet<Location>();
        int count = 0;
        Map<Location, InNode> keyLocation2Nodes = new java.util.HashMap<Location, InNode>();
        LinkedList<Location> queue = new java.util.LinkedList<Location>();
        short tlevel = 0;
        // The number of alternative paths from the virtual endpoint to the node, with an initial endpoint of 1
        Map<Location, BigDecimal> routeCounts = new java.util.HashMap<Location, BigDecimal>();
        routeCounts.put(end.loc, new BigDecimal(1));

        Location queueEnd = mlcs.end;
        queue.addLast(mlcs.end);

        short maxLevel = (short) (end.level - 1);
        while (!queue.isEmpty()) {
            Location loc = queue.removeFirst();
            InNode node = find(loc);
//			System.out.println(node.loc);
            for (Location ploc : node.predecessors) {
                InNode p = find(ploc);

                if (p.level + tlevel == maxLevel) {

                    if (keyLocs.contains(ploc)) {
                        routeCounts
                                .put(ploc,
                                        routeCounts.get(ploc).add(
                                                routeCounts.get(loc)));
                    } else {
                        keyNodeCount = keyNodeCount.add(new BigDecimal(1));
                        keyLocs.add(ploc);
                        keyLocation2Nodes.put(ploc, p);
                        routeCounts.put(ploc, routeCounts.get(loc));
                        queue.addLast(ploc);
                        count++;
                    }
                }
//				System.out.println(queue.size());
            }
            if (loc == queueEnd) {
//				LogWriter.log((maxLevel - tlevel) + "反:" + count);
                if (!queue.isEmpty())
                    queueEnd = queue.getLast();
                tlevel += 1;
                count = 0;
                removeUnusedGraph((short) (maxLevel - tlevel + 1));
            }
        }
        keyLocs.remove(start.loc);
        keyLocs.remove(end.loc);
        keyLocation2Nodes.put(start.loc, start);
        keyLocation2Nodes.put(end.loc, end);
        this.location2Nodes = keyLocation2Nodes;
        matchedCount = routeCounts.get(start.loc);

        if (logger.isDebugEnabled()) {
            logger.debug("find key location using "
                    + Stopwatch.format(System.nanoTime() - startTime));
        }
    }

    //update the precursor and minimum Layer of the Starting Point
    protected short registerOrUpdateLevelInfo(Border border) {
        short minLevel = Short.MAX_VALUE;
        for (Location loc : border.optimalInsides) {
            InNode head = get(loc);
            Border.LevelInfo levelInfo = border.nodes.get(loc);
            head.level = levelInfo.level;
            head.predecessors.addAll(levelInfo.predecessors);
            if (head.level < minLevel)
                minLevel = head.level;
        }


        // Set<Location> existed = new HashSet<Location>();
        for (Location loc : border.otherInsides) {
            InNode head = addLocation(loc);
            // if (head.indegree > 0) existed.add(loc);
            Border.LevelInfo levelInfo = border.nodes.get(loc);
            head.level = levelInfo.level;
            head.predecessors.addAll(levelInfo.predecessors);
            if (head.level < minLevel)
                minLevel = head.level;
        }
        // border.otherInsides.removeAll(existed);
        return minLevel;
    }

    /**
     * Find the starting point of topological sorting and select the point with 0 degree as the starting point of topological sorting
     */
    private Map<Short, List<Location>> findTopsortStarts(Border border) {
        Map<Short, List<Location>> headLevels = new HashMap<Short, List<Location>>();
        for (Location loc : border.optimalInsides) {
            InNode head = get(loc);
            short level = head.level;
            if (head.indegree == 0) {
                List<Location> levelLocs = headLevels.get(level);
                if (null == levelLocs) {
                    levelLocs = new ArrayList<Location>();
                    headLevels.put(level, levelLocs);
                }
                levelLocs.add(loc);
            }
        }
        return headLevels;
    }

    /**
     * summary statistics
     *
     * @throws Exception
     */
    public Mlcs.Result stat() throws Exception {
        findKeyLocs();
        return new Mlcs.Result(matchedCount, keyNodeCount, end.level - 1);
    }


    /**
     * If segmented, serialize subgraphs
     *
     * @throws Exception
     */
    public void shrink(short[] levelPair, boolean serialize) throws Exception {
        if (serialize) {
            if (location2Nodes.size() > 2) {
                if (location2Nodes.containsKey(end.loc)) {
                    location2Nodes.remove(end.loc);
                }
                int index = ++serializeIndex;
                SubGraph graph = new SubGraph(index, levelPair[0],
                        levelPair[1], location2Nodes, preLocations);
                className = Seilize.serializeObject(graph, index);
//				System.out.println("index:"+ index);
            }
            location2Nodes = new java.util.HashMap<Location, InNode>();
            System.gc();
        } else {
            if (!location2Nodes.containsKey(end.loc)) {
                location2Nodes.put(end.loc, end);
            }
            this.subgraphs.add(new SubGraph(serializeIndex + 1, levelPair[0],
                    levelPair[1], location2Nodes));
            location2Nodes = new java.util.HashMap<Location, InNode>();
        }
    }

    public void shrink(short[] levelPair, boolean serialize, boolean isLastLayer) throws Exception {
        if (serialize) {
            if (location2Nodes.size() > 0) {
                if (location2Nodes.containsKey(end.loc)) {
                    location2Nodes.remove(end.loc);
                }

                int index = ++serializeIndex;
                SubGraph graph = new SubGraph(index, levelPair[0],
                        levelPair[1], location2Nodes, preLocations);
                className = Seilize.serializeObject(graph, index);
//				System.out.println("index:"+ index);
            }
            location2Nodes = new java.util.HashMap<Location, InNode>();
            System.gc();
        } else {
            if (isLastLayer) {
                if (!location2Nodes.containsKey(end.loc))
                    location2Nodes.put(end.loc, end);
            } else {
                if (location2Nodes.containsKey(end.loc)) {
                    location2Nodes.remove(end.loc);
                }
            }
            this.subgraphs.add(new SubGraph(serializeIndex + 1, levelPair[0],
                    levelPair[1], location2Nodes));
            location2Nodes = new java.util.HashMap<Location, InNode>();
        }
    }


    /**
     * Print path results
     */
    public List<List<Location>> printPathResult() {
        List<List<Location>> paths = findKeyPaths();
        List<String> strPaths = new ArrayList<>();
        for (List<Location> path : paths) {
            StringBuffer stringBuffer = new StringBuffer();
            for (Location l : path) {
//				System.out.print(mlcs.charAt(l));
                l.setName(mlcs.charAt(l));
                stringBuffer.append(mlcs.charAt(l));
            }
            strPaths.add(stringBuffer.toString());
//			System.out.println();
        }
        return paths;
    }

    public List<List<Location>> findKeyPaths() {
        LinkedList<List<Location>> results = new LinkedList<List<Location>>();
        collectPath(end, new LinkedList<Location>(), results);
        return results;
    }

    /**
     * Recursive collection results
     */
    private void collectPath(InNode n, List<Location> suffix,
                             LinkedList<List<Location>> result) {
        int maxLevel = n.level - 1;
        List<InNode> predecessors = new ArrayList<InNode>();
        for (Location loc : n.predecessors) {
            InNode p = get(loc);
            if (null != p && p.level == maxLevel)
                predecessors.add(p);
        }
        for (InNode e : predecessors) {
            List<Location> newPaths = new ArrayList<Location>();
            newPaths.add(e.loc);
            newPaths.addAll(suffix);
            if (!e.predecessors.isEmpty()) {
                if (e.predecessors.contains(start.loc)) {
                    result.add(newPaths);
                } else {
                    collectPath(e, newPaths, result);
                }
            }
        }
    }
}
