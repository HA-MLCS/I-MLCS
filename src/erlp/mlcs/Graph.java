package erlp.mlcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import erlp.mlcs.util.LogWriter;
import erlp.mlcs.util.Logger;

public abstract class Graph {
  protected Mlcs mlcs;
  public final InNode start; // startnode
  public final InNode end; // endnode
  protected Logger logger = Logger.getLogger(Graph.class);
  // Location -> Node
  protected Map<Location, InNode> location2Nodes = new java.util.HashMap<Location, InNode>();

  protected Set<Location> preLocations = new HashSet<>();  //the precursor of the current subgraph
  protected Set<Location> boundLocations = new HashSet<>();  //The last point of the current graph
  
  public Graph(Mlcs mlcs) {
    super();
    this.mlcs = mlcs;
    start = newNode(mlcs.start);
    end = newEndNode(mlcs.end);
  }

  protected InNode newNode(Location to) {
    InNode n = new InNode(to);
//    LogWriter.log(to.toString());
    location2Nodes.put(to, n);
    return n;
  }

  protected EndNode newEndNode(Location to) {
    EndNode n = new EndNode(to);
    location2Nodes.put(to, n);
    return n;
  }

  public InNode get(Location loc) {
    return location2Nodes.get(loc);
  }

  public int size() {
    return location2Nodes.size();
  }

  public void printState() {
    List<Location> locations = new ArrayList<Location>(location2Nodes.keySet());
    Collections.sort(locations, mlcs.sorter);
    for (Location loc : locations) {
      InNode node = location2Nodes.get(loc);
      LogWriter.log(node.toString());
    }
  }
}
