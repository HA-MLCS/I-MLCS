package erlp.mlcs.util;

import java.util.ArrayList;
import java.util.List;

public class StepGenerator {
//Set the maximum subscript for each segment according to the length of the segment
  public static int[] generate(int step, int max) {
    int level = Math.min(step, max);
    List<Integer> result = new ArrayList<Integer>();
    
    while (level <= max) {
      result.add(level);
      if (level == max) break;
      level += step;
      if (level > max) level = max;
    }
    
    int[] intarray = new int[result.size()];
    for (int i = 0; i < result.size(); i++) {
      intarray[i] = result.get(i);
    }
    return intarray;
  }
}
