package erlp.mlcs;

import java.util.List;

/**
 * The path from the from point to the inner and outer points of the boundary
 * 
 */
public class BoundedPaths {
  public final Location from;
  public final List<Location> insides;
  public final List<Location> outsides;

  public boolean onBoundary() {
    return !outsides.isEmpty();
  }

  public BoundedPaths(Location from, List<Location> insides, List<Location> outsides) {
    super();
    this.from = from;
    this.insides = insides;
    this.outsides = outsides;
  }

  public String toString() {
    return from + "->" + insides + " outside " + outsides;
  }
}
