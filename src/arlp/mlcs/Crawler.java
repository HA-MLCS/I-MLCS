package arlp.mlcs;

import arlp.mlcs.Graph.Node;
import arlp.mlcs.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;


public class Crawler {

	public static LinkedList<Location> toDelete = new LinkedList<>();

	public static double P = 0.8;   //proportion of reserved points 
	public static void main(String[] args,double Pre) throws IOException {

		if (args.length == 0) {
			System.out.println(
					"Usage:HorizontalCrawler /path/to/your/data/file -Dmlcs.max-thread=2 -Dmlcs.auto-shrink=true -Dmlcs.optimal-only=false");
			return;
		}
		P=Pre;

		for (String arg : args) {
			List<String> fileNames = findFiles("./file", arg);
			for (String file : fileNames) {

				StringBuilder prompts = new StringBuilder();
				Mlcs mlcs = Mlcs.loadFromFile("./file/" + file); //loading data from a file
				LogWriter.getLogWriter("log" + file);
				String maxThread = System.getProperty("mlcs.max-thread");
				
				if (null != maxThread) {
					mlcs.maxThread = Integer.parseInt(maxThread);
					prompts.append(" max-thread=" + maxThread);
				}

				LogWriter.log("Starting search " + file + prompts);
				for (int i = 0; i < 1; i++) {
					System.gc();
					long start = System.nanoTime();
					Graph graph = new Graph(mlcs);
					Mlcs.Result result = Crawler.bfs(mlcs, graph);
					LogWriter.log(result.count + " mlcs(max length " + result.length
							+ ") found, using " + Stopwatch.format(System.nanoTime() - start));

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
			}
		}
	}
/**
 * 
 * find all files based on the prefix name of the file
 */

	private static List<String> findFiles(String path, String name) {
		List<String> fileNmesList = new ArrayList<String>();
		File file = new File(path);
		File[] files = file.listFiles();
		// String[] fileNames = file.list();

		for (File f : files) {
			if (f.isFile() && f.getName().startsWith(name)) {
				fileNmesList.add(f.getName());
			}
		}

		return fileNmesList;
	}

	private static Logger logger = Logger.getLogger(Crawler.class);

/**
 * 
 *results are obtained by build graph, forward topology and reverse topology.
 */
	public static Mlcs.Result bfs(Mlcs mlcs, Graph graph) {
		Location start = mlcs.start;
		List<Location> heads = new ArrayList<Location>();
		for (Location loc : mlcs.nextSortedLocations(start)) {
			if (graph.accessHead(start, loc))
				heads.add(loc);
		}
		search(mlcs, graph, heads);
		return graph.stat();
	}
	
	/**
	 * Parallel build graph, adding points to the graph, calculating the indegree value of each point
	 */
	private static void search(Mlcs mlcs, Graph graph, List<Location> starts) {
		long startTime = System.nanoTime();
		ArrayList<Location> locations = new ArrayList<Location>();
		for (Location start : starts) {
			for (Location to : mlcs.nextLocations(start)) {
				if (graph.addEdge(start, to))
					locations.add(to);
			}
		}
		ForkJoinPool pool = null;
		if (mlcs.maxThread > 0)
			pool = new ForkJoinPool(mlcs.maxThread);
		else
			pool = new ForkJoinPool();

		int parallelism = pool.getParallelism();
		int parallelThreshhold = parallelism * 7;

		int i = 0;
		int threshold = 0;			//After the  number of points in current iteration layer exceeds this threshold, the deletion strategy is adopted.
		boolean delete = false;

		while (!locations.isEmpty()) {
			i++;
			List<int[]> segs = null;
			if (locations.size() > threshold)
				delete = true;
			else {
				delete = false;
			}
			//data segmentation
			if (locations.size() < parallelThreshhold) {
				segs = Queues.split(locations.size(), locations.size());
			} else {
				segs = Queues.split(locations.size(), parallelism);
			}

			LinkedList<ForkJoinTask<List<Location>>> tasks = new LinkedList<ForkJoinTask<List<Location>>>();
			for (int[] seg : segs) {
				tasks.addLast(pool.submit(new BFSCrawler(mlcs, locations, seg[0], seg[1])));
			}

			
			Set<Location> newLocationsList = new HashSet<>();
			List<Location>[] levelLocation = new ArrayList[mlcs.currentLevel + 1];  //be used to sort
			long sum = 0;
			for (ForkJoinTask<List<Location>> task : tasks) {
				List<Location> nextLocations = task.join();
				if (delete) {
					//According to the score of each point, rank by counting from small to large
					for (int j = 0; j < nextLocations.size(); j++) {
						sum++;
						Location l = nextLocations.get(j);
						if (levelLocation[l.getMax()] == null) {
							levelLocation[l.getMax()] = new ArrayList<>();
							levelLocation[l.getMax()].add(l);
						} else
							levelLocation[l.getMax()].add(l);
					}
				} else {
					newLocationsList.addAll(nextLocations);
				}
			}
			if (delete) {
				
				//reserved point in proportion p
				long remainNum = (long) (sum * P);
				for (int k = 0; k < levelLocation.length; k++) {
					if (levelLocation[k] != null) {
						if (remainNum > levelLocation[k].size()) {
							remainNum = remainNum - levelLocation[k].size();
							newLocationsList.addAll(levelLocation[k]);
						} else {
							newLocationsList.addAll(levelLocation[k].subList(0, (int) remainNum));
							break;
						}
					}
				}

				//dominant point sorting and retention of dominant point frontier
				if (!newLocationsList.isEmpty()) {
					newLocationsList = RadixSortingbeta2.sort(newLocationsList, i, 0.5);
				}

			}

			List<Location> l = locations;
			locations = new ArrayList<Location>();
			for (Location s : l) {
				List<Location> next = mlcs.nextLocations(s);
				boolean hasNext = false;

				for (Location n : next) {
					if (newLocationsList.contains(n)) {
						hasNext = true;
						if (graph.addEdge(s, n))    //Add points to the graph, and if new points are added, join the queue for the next iteration
							locations.add(n);
					}
				}
				if (next.isEmpty()) {
					hasNext = true;
				}

				if (!hasNext) {
					// System.out.println(s);
					toDelete.add(s);
				}

			}
			// deleteNode(graph);
		}
		pool.shutdown();
		StringBuilder sb = new StringBuilder();
		for (Location start : starts) {
			sb.append(mlcs.charAt(start) + "@" + start);
			sb.append(',');
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		logger.info("build graph(" + sb + ") using " + Stopwatch.format(System.nanoTime() - startTime));
	}
/**
 * 
 * delete useless nodes that has useless successors from graph
 */
	public static void deleteNode(Graph graph) {
		while (!toDelete.isEmpty()) {
			Location node = toDelete.removeFirst();
			for (Node n : graph.location2Nodes.get(node).predecessors) {
				n.successors.remove(node);
			}
			graph.location2Nodes.remove(node);
		}
	}

	/**
	 * get successors of  locations[from,to)
	 *  
	 */
	@SuppressWarnings("serial")
	static class BFSCrawler extends RecursiveTask<List<Location>> {
		Mlcs mlcs;
		ArrayList<Location> locations;
		int from, to;

		public BFSCrawler(Mlcs mlcs, ArrayList<Location> locations, int from, int to) {
			this.mlcs = mlcs;
			this.locations = locations;
			this.from = from;
			this.to = to;
		}

		public List<Location> compute() {
			LinkedList<Location> nextLocations = new LinkedList<Location>();
			for (int i = from; i < to; i++) {
				Location current = locations.get(i);
				nextLocations.addAll(mlcs.nextLocations(current));
			}
			return nextLocations;
		}
	}
}
