package erlp.mlcs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


public class InNode implements Serializable {
  private static final long serialVersionUID = -426003142019185878L;
  /** location of node */
  public Location loc;
  /** indegree of node */
  public short indegree;
  /** level of node */
  public short level;
  /** predecessors of node */
  public Collection<Location> predecessors = new ArrayList<Location>();
  
  public InNode() {
	  this.loc = null;
	  this.indegree = 0;
	  this.level = 0;
	  this.predecessors = null;
  }
  
  public InNode(Location loc) {
    this.loc = loc;
  }

  public InNode reset() {
    this.predecessors = new ArrayList<Location>();
    this.indegree = 0;
    this.level = 0;
    return this;
  }

  public void inc() {
    indegree = (short) (indegree + 1);
  }

  /**
   * add predecessor
   */
  
  public synchronized boolean addPredecessor(Location predecessorLoc, int newLevel) {
	  
    if (this.level < newLevel) {
      this.level = (short) newLevel;
      predecessors = new ArrayList<Location>();
    }
    predecessors.add(predecessorLoc);
    indegree -= 1;
    if (indegree < 0) System.out.println(loc + " indegree :" + indegree);
    return indegree == 0;
    
  }
  
  
 //update level 
  protected void updateLevelCascade(short level, Graph graph) {
    List<Location> nexts = graph.mlcs.nextLocations(this.loc);
    short nextLevel = (short) (level + 1);
    if (nexts.isEmpty()) {
      graph.end.addPredecessor(this.loc, nextLevel);
      return;
    } else {
      for (Location location : nexts) {
        InNode successor = graph.get(location);
        if (null != successor) successor.updateLevel(this.loc, nextLevel, graph);
      }
    }
  }

  public void updateLevel(Location predecessor, short newLevel, Graph graph) {
    if (newLevel == level) {
      if (!predecessors.contains(predecessor)) this.predecessors.add(predecessor);
    } else if (newLevel > level) {
      predecessors = new ArrayList<Location>();
      predecessors.add(predecessor);
      this.level = newLevel;
      updateLevelCascade(newLevel, graph);
    }
  }

  @Override
  public String toString() {
    return loc + " g:" + indegree + " l:" + level + " p:" + predecessors;
  }
}

/**
 * virtual endpoint 
 * 
 * 
 */
class EndNode extends InNode {
  private static final long serialVersionUID = 1320250813544807923L;

  public EndNode(Location loc) {
    super(loc);
    predecessors = new HashSet<Location>();
  }

  @Override
  public InNode reset() {
    this.predecessors = new HashSet<Location>();
    this.indegree = 0;
    this.level = 0;
    return this;
  }

  @Override
  public void updateLevel(Location predecessor, short newLevel, Graph graph) {
    if (newLevel == level) {
      predecessors.add(predecessor);
    } else if (newLevel > level) {
      predecessors.add(predecessor);
      this.level = newLevel;
    }
  }

  /**
   * add predecessors to virtual endpoint
   */
  @Override
  public synchronized boolean addPredecessor(Location predecessorLoc, int newLevel) {
    if (newLevel > level) {
      this.level = (short) newLevel;
      predecessors = new HashSet<Location>();
      predecessors.add(predecessorLoc);
    } else if (newLevel == level) {
      predecessors.add(predecessorLoc);
    }
    return false;
  }

}
