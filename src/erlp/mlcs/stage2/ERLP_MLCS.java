package erlp.mlcs.stage2;

import java.io.File;
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
import erlp.mlcs.BoundedPaths;
import erlp.mlcs.InNode;
import erlp.mlcs.Location;
import erlp.mlcs.Mlcs;
import erlp.mlcs.util.LogWriter;
import erlp.mlcs.util.Logger;
import erlp.mlcs.util.Queues;
import erlp.mlcs.util.StepGenerator;
import erlp.mlcs.util.Stopwatch;
import erlp.mlcs.util.TestDrawTree;


public class ERLP_MLCS {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out
                    .println("Usage:StagedCrawler /path/to/your/data/file -Dmlcs.max-thread=2 -Dmlcs.step=10");
            return;
        }
        StringBuilder prompts = new StringBuilder();

        int maxThread = 1;
        String maxThreadProperty = System.getProperty("mlcs.max-thread");
        if (null != maxThreadProperty) {
            maxThread = Integer.parseInt(maxThreadProperty);
            prompts.append(" max-thread=" + maxThread);
        }

        int step = 0;
//		String stepProperty = System.getProperty("mlcs.step");
//		if (null != stepProperty)
//			step = Integer.parseInt(stepProperty);


//		prompts.append(" step=" + step);

        System.out.println("Starting search " + args + prompts);
        for (String arg : args) {
            List<String> fileNames = findFiles("./file", arg);
            for (String file : fileNames) {
                System.gc();
                multipleTest(file, maxThread, step);
            }
        }
    }

    /**
     * can run multiple files
     */
    private static void multipleTest(String fileName, int maxThread, int step)
            throws Exception {
        LogWriter.getLogWriter("ERLP_log" + fileName);
        Mlcs mlcs = Mlcs.loadFromFile("./file/" + fileName);
        if (step == 0)
            //		step = (mlcs.seqs.get(0).length() - 1) / 2;
            step = mlcs.seqs.get(0).length();
        long start = System.nanoTime();
        IndegreeGraph graph = new IndegreeGraph(mlcs);
        Mlcs.Result result = ERLP_MLCS.bfs(mlcs, graph, step);
        LogWriter.log("nodeCount" + result.nodeCount + "\t" + result.count + " mlcs(max length " + result.length
                + ") found, using "
                + Stopwatch.format(System.nanoTime() - start));

        if (null != result.count) {
            List<List<Location>> paths = new ArrayList<>();
            int total = result.count.intValue();

            //If the number of results is within a certain range, visualize the results
            if (total > 0 && total <= 10000) {
                paths = graph.printPathResult();
                TestDrawTree testDrawTree = new TestDrawTree();
                long s = System.nanoTime();
                testDrawTree.visualize(paths, true, 70 * paths.get(0).size(), mlcs);   //visualize the result

            }

        }


        LogWriter.close();
    }

    /**
     * find all files prefixed with name under the specified path
     */
    private static List<String> findFiles(String path, String name) {
        List<String> fileNmesList = new ArrayList<String>();
        File file = new File(path);
        File[] files = file.listFiles();
//		String[] fileNames = file.list();
        for (File f : files) {
            if (f.isFile() && f.getName().startsWith(name)) {
                fileNmesList.add(f.getName());
            }
        }
        return fileNmesList;
    }

    private static Logger logger = Logger.getLogger(ERLP_MLCS.class);

    /**
     * breadth search
     */
    public static Mlcs.Result bfs(Mlcs mlcs, IndegreeGraph graph, int step)
            throws Exception {
        int maxLevel = mlcs.seqs.get(0).length() - 1;
        Map<Location, Border.LevelInfo> nodes = new HashMap<Location, Border.LevelInfo>();
        nodes.put(mlcs.start, new Border.LevelInfo((short) 0,
                new ArrayList<Location>()));


        int[] steps = StepGenerator.generate(step, maxLevel);


        for (final int level : steps) {
            mlcs.currentLevel = level;

            // Firstly, the nodes are filtered to distinguish 1) interior points (optimal and bypass) and 2) exterior points, and all points are preserved
            Border border = filter(mlcs, graph, nodes);

            // Optimal Path Search
            Map<Location, List<Location>> newLocs1 = searchOptimal(mlcs, graph,
                    border.optimalInsides);
            gc();

            // After the search results come out, the hierarchy and precursor list of all starting points in the boundaries will be set to prepare for the topological sorting.
            short minLevel = graph.registerOrUpdateLevelInfo(border);

            // Topological ranking of optimal paths is carried out to find the minimum and maximum levels of subgraphs participating in the ranking.
            short[] levePair = graph.setLevelByTopsort(border);

            // By-pass search
            Map<Location, List<Location>> newLocs2 = searchOther(mlcs, graph,
                    border.otherInsides);

            // Combine the boundary points of the next boundary that appear in the two-way search to determine their level and precursor
            nodes = border.outsides;
            addNewLocs(mlcs, graph, nodes, merge(newLocs1, newLocs2));


            levePair[0] = levePair[0] > minLevel ? minLevel : levePair[0];
            levePair[1] = (short) (graph.end.level - 1);

            if (level < maxLevel) {
                graph.delete5(nodes);
            }
            //serialize if segmented
            graph.shrink(levePair, level < maxLevel, level == maxLevel);

        }

        return graph.stat();

    }


    public static void gc() {
        double freeM = Runtime.getRuntime().freeMemory() / 1024 / 1024 / 1024;
        if (freeM < 2) {
            System.gc();
        }
    }


    /**
     * For newLocs, determine their hierarchy and trend, and add them to the starting point of the next search.
     */
    private static void addNewLocs(Mlcs mlcs, IndegreeGraph graph,
                                   Map<Location, Border.LevelInfo> nodes,
                                   Map<Location, List<Location>> newLocs) {
        long startTime = System.nanoTime();
        for (Map.Entry<Location, List<Location>> entry : newLocs.entrySet()) {
            short level = 0;
            List<Location> predecessorLocs = new ArrayList<Location>();
            for (Location predecessorLoc : entry.getValue()) {
                InNode predecessor = graph.get(predecessorLoc);
                if (predecessor.level + 1 > level) {
                    level = (short) (predecessor.level + 1);
                    predecessorLocs = new ArrayList<Location>();
                    predecessorLocs.add(predecessor.loc);
                } else if (predecessor.level + 1 == level) {
                    predecessorLocs.add(predecessor.loc);
                }
            }
            Border.LevelInfo node = nodes.get(entry.getKey());
            if (null == node) {
                node = new Border.LevelInfo(level, predecessorLocs);
                nodes.put(entry.getKey(), node);
            } else {
                if (level > node.level) {
                    node.predecessors = predecessorLocs;
                    node.level = level;
                } else if (level == node.level) {

                    node.predecessors.addAll(predecessorLocs);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            if (newLocs.size() > 0) {
                logger.debug("addNewLocs " + newLocs.size() + " nodes,using "
                        + Stopwatch.format(System.nanoTime() - startTime));
            }
        }
    }


    private static Map<Location, List<Location>> merge(
            Map<Location, List<Location>> loc1,
            Map<Location, List<Location>> loc2) {
        for (Map.Entry<Location, List<Location>> entry : loc2.entrySet()) {
            List<Location> l = loc1.get(entry.getKey());
            if (null == l) {
                loc1.put(entry.getKey(), entry.getValue());
            } else {
                Set<Location> merged = new HashSet<Location>();
                merged.addAll(l);
                merged.addAll(entry.getValue());
                loc1.put(entry.getKey(), new ArrayList<Location>(merged));
            }
        }
        return loc1;
    }


    /**
     * All starting points are filtered to determine which points can be traversed as starting points at this stage. The best advantage of the boundary is that it is first added to the graph.
     */

    private static Border filter(Mlcs mlcs, IndegreeGraph graph,
                                 Map<Location, Border.LevelInfo> nodes) {
        Map<Location, Border.LevelInfo> outsides = new HashMap<Location, Border.LevelInfo>();
        ArrayList<Location> insides = new ArrayList<Location>();

        for (Map.Entry<Location, Border.LevelInfo> entry : nodes.entrySet()) {
            boolean keyOutofside = false;
            // Check if the starting point is outside the boundaries
            short[] me = entry.getKey().index;
            for (int i = 0; i < me.length; i++) {
                if (me[i] > mlcs.currentLevel) {
                    keyOutofside = true;
                    break;
                }
            }
            // If out of boundaries, next visit
            if (keyOutofside) {
                outsides.put(entry.getKey(), entry.getValue());
            } else {
                // graph.addLocation(entry.getKey());
                insides.add(entry.getKey());
            }
        }
        // Add Optimal Path Points to the Graph
        ArrayList<Location>[] rs = Location.split(insides, mlcs,
                (int) Math.ceil((insides.size() * 0.3)));

        for (Location optimal : rs[0]) {
            graph.addLocation(optimal);
        }

        return new Border(nodes, rs[0], rs[1], outsides);
    }

    /**
     * Concurrent search for specified location
     */
    private static Map<Location, List<Location>> searchOptimal(Mlcs mlcs,
                                                               IndegreeGraph graph, ArrayList<Location> locations) {
        long startTime = System.nanoTime();
        Map<Location, List<Location>> nextLocations = new HashMap<Location, List<Location>>();
        ForkJoinPool pool = null;
        if (mlcs.maxThread > 0)
            pool = new ForkJoinPool(mlcs.maxThread);
        else
            pool = new ForkJoinPool();

        int parallelism = pool.getParallelism();
        int parallelThreshhold = parallelism * 3;
        while (!locations.isEmpty()) {
            List<int[]> segs = Queues.split(locations, parallelism,
                    parallelThreshhold);
            LinkedList<ForkJoinTask<List<BoundedPaths>>> tasks = new LinkedList<ForkJoinTask<List<BoundedPaths>>>();
            for (int[] seg : segs) {
                tasks.addLast(pool.submit(new BFSCrawler(mlcs, locations,
                        seg[0], seg[1])));
            }
            locations = new ArrayList<Location>();
            for (ForkJoinTask<List<BoundedPaths>> task : tasks) {
                List<BoundedPaths> nextPathes = task.join();
                for (BoundedPaths bp : nextPathes) {
                    if (bp.onBoundary()) {
                        for (Location loc : bp.outsides) {
                            List<Location> predecessors = nextLocations
                                    .get(loc);
                            if (null == predecessors) {
                                predecessors = new LinkedList<Location>();
                                nextLocations.put(loc, predecessors);
                            }
                            predecessors.add(bp.from);
                        }
                    }
                    for (Location l : bp.insides) {
                        if (graph.addEdge(bp.from, l))     //If this point is a newly added point, join the queue for the next search
                            locations.add(l);
                    }
                }
            }
        }
        pool.shutdown();
//		logger.info("build optimal graph(boundary " + mlcs.currentLevel
//				+ " with outside " + nextLocations.size() + ") using "
//				+ Stopwatch.format(System.nanoTime() - startTime));
        return nextLocations;
    }

    /**
     * breadth-first search
     */
    @SuppressWarnings("serial")
    static class BFSCrawler extends RecursiveTask<List<BoundedPaths>> {
        Mlcs mlcs;
        ArrayList<Location> locations;
        int from, to;

        public BFSCrawler(Mlcs mlcs, ArrayList<Location> locations, int from,
                          int to) {
            this.mlcs = mlcs;
            this.locations = locations;
            this.from = from;
            this.to = to;
        }

        @Override
        public List<BoundedPaths> compute() {
            LinkedList<BoundedPaths> nextPaths = new LinkedList<BoundedPaths>();
            for (int i = from; i < to; i++) {
                Location current = locations.get(i);
                nextPaths.add(mlcs.nextPaths(current));
            }
            return nextPaths;
        }
    }

    /**
     * Searching for non-optimal paths
     */
    private static Map<Location, List<Location>> searchOther(Mlcs mlcs,
                                                             IndegreeGraph graph, ArrayList<Location> locations) {
        if (locations.isEmpty())
            return new HashMap<Location, List<Location>>();
        long startTime = System.nanoTime();
        Map<Location, List<Location>> nextLocations = new HashMap<Location, List<Location>>();
        ForkJoinPool pool = null;
        if (mlcs.maxThread > 0)
            pool = new ForkJoinPool(mlcs.maxThread);
        else
            pool = new ForkJoinPool();
        int parallelism = pool.getParallelism();
        int parallelThreshhold = parallelism * 3;
        while (!locations.isEmpty()) {
            List<int[]> segs = Queues.split(locations, parallelism,
                    parallelThreshhold);
            LinkedList<ForkJoinTask<List<BoundedPaths>>> tasks = new LinkedList<ForkJoinTask<List<BoundedPaths>>>();
            for (int[] seg : segs) {
                tasks.addLast(pool.submit(new BFSCrawler(mlcs, locations,
                        seg[0], seg[1])));
            }
            locations = new ArrayList<Location>();
            for (ForkJoinTask<List<BoundedPaths>> task : tasks) {
                List<BoundedPaths> nextPathes = task.join();

                for (BoundedPaths bp : nextPathes) {
                    Location from = bp.from;
                    if (bp.onBoundary()) {
                        for (Location loc : bp.outsides) {
                            List<Location> predecessors = nextLocations
                                    .get(loc);
                            if (null == predecessors) {
                                predecessors = new LinkedList<Location>();
                                nextLocations.put(loc, predecessors);
                            }
                            predecessors.add(bp.from);
                        }
                    }
                    short nextLevel = (short) (graph.get(from).level + 1);
                    if (bp.insides.isEmpty()) {
                        graph.addEdge(from, mlcs.end, nextLevel);
                    } else {
                        for (Location l : bp.insides) {
                            if (graph.addEdge(from, l, nextLevel))
                                locations.add(l);
                        }
                    }
                }
            }
        }
        pool.shutdown();
//		logger.info("build non optimal graph(boundary " + mlcs.currentLevel
//				+ " with outside " + nextLocations.size() + ") using "
//				+ Stopwatch.format(System.nanoTime() - startTime));
        return nextLocations;
    }

}
