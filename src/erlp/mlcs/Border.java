package erlp.mlcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Border {
  /** all outliers */
  public final Map<Location, LevelInfo> nodes;

  /** In the next stage, the optimal path */
  public final ArrayList<Location> optimalInsides;

  /** within the boundaries of the next phase, alternative routes */
  public final ArrayList<Location> otherInsides;

  /** Outside the next stage */
  public final Map<Location, LevelInfo> outsides;

  public Border(Map<Location, LevelInfo> nodes, ArrayList<Location> optimalInsides,
      ArrayList<Location> otherInsides, Map<Location, LevelInfo> outsides) {
    super();
    this.nodes = nodes;
    this.optimalInsides = optimalInsides;
    this.otherInsides = otherInsides;
    this.outsides = outsides;
  }

  public static class LevelInfo {
    public short level;
    public List<Location> predecessors;

    public LevelInfo(short level, List<Location> predecessors) {
      super();
      this.level = level;
      this.predecessors = predecessors;
    }
  }

}
