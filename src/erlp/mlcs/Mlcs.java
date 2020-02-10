package erlp.mlcs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * MLCS problem model
 * 
 */
public class Mlcs {
  Set<Character> charset; // the character that All strings of this problem consist of
  public final List<Sequence> seqs; // Primitive character sequence

  public final Location start; //startNode
  public final Location end; // endNode

  short[][][] successorTable; // char->seq->table

  // maximum current subscript set
  public int currentLevel = 0;

 
  public int maxThread = 0;

  public final Location.Sorter sorter = new Location.Sorter();

  /**
   * building successor tables
   */
  
  public Mlcs(Set<Character> charset, List<Sequence> seqs) {
    this.charset = charset;
    this.seqs = seqs;
    start = Mlcs.buildStart(seqs.size());
    end = Mlcs.buildEnd(seqs);
    successorTable = new short[charset.size()][][];
    List<Character> charList = new ArrayList<Character>(charset);
    
    for (int i = 0; i < charList.size(); i++) {
      successorTable[i] = new short[seqs.size()][];
      for (int j = 0; j < seqs.size(); j++) {
        Sequence seq = seqs.get(j);
        successorTable[i][j] = seq.buildSuccessors(charList.get(i).charValue());
      }
    }
    
    // The default is set to the maximum number of layers, and then set when searching in segments.
    currentLevel = seqs.get(0).length() - 1;
  }

  public char charAt(Location location) {
    return seqs.get(0).charAt(location.index[0]);
  }

  /**
   * All successors after the current node are sorted according to the size of subscripts, with the smallest at the front
   */
  public List<Location> nextSortedLocations(Location current) {
    List<Location> locations = nextLocations(current);
    Collections.sort(locations, sorter);
    return locations;
  }

  /**
   * All successors after the current node
   */
  
  public List<Location> nextLocations(Location current) {
    List<Location> nexts = new ArrayList<Location>(charset.size());
    for (int i = 0; i < successorTable.length; i++) {
      int snum = seqs.size();
      short[] tmp = new short[snum];
      for (int j = 0; j < seqs.size(); j++) {
        short successor = successorTable[i][j][current.index[j]];
        if (successor < 0 || successor > currentLevel) break;
        else tmp[j] = successor;
      }
      if (tmp[tmp.length - 1] > 0) nexts.add(new Location(tmp));
    }
    return nexts;
  }

  public BoundedPaths nextPaths(Location current) {
    List<Location> insides = new ArrayList<Location>();
    List<Location> outsides = new ArrayList<Location>();
    for (int i = 0; i < successorTable.length; i++) {
      int snum = seqs.size();
      short[] tmp = new short[snum];
      boolean outofboundary = false;
      for (int j = 0; j < seqs.size(); j++) {
        short successor = successorTable[i][j][current.index[j]];
        if (successor < 0) break;
        if (successor > currentLevel) outofboundary = true;
        tmp[j] = successor;
      }
      if (tmp[tmp.length - 1] > 0) {
        if (outofboundary) outsides.add(new Location(tmp));
        else insides.add(new Location(tmp));
      }
    }
    return new BoundedPaths(current, insides, outsides);
  }

  /**
   * build start node
   */
  public static Location buildStart(int length) {
    return new Location(new short[length]);
  }

  /**
   * build end node
   */
  public static Location buildEnd(List<Sequence> seqs) {
    short[] index = new short[seqs.size()];
    int i = 0;
    for (Sequence seq : seqs) {
      index[i] = (short) seq.length();
      i += 1;
    }
    return new Location(index);
  }
//Read the file to get all strings and character sets
  public static Mlcs loadFromFile(String file) throws IOException {
    List<Sequence> seqs = new ArrayList<Sequence>();  
    BufferedReader in = new BufferedReader(new FileReader(file));
    String str = in.readLine();
    Set<Character> charsets = new java.util.HashSet<Character>(); 
    while (str != null) {
      if (str.length() > 0) {
        Sequence s = Sequence.build(str);
        seqs.add(s);
        charsets.addAll(s.charsets());
      }
      str = in.readLine();
    }
    in.close();
    return new Mlcs(charsets, seqs);
  }

  /**
   * statistical results
   */
  public static class Result {

	    public final BigDecimal count; // number of matched results
	    public final int length; // length of matching results
	    public final BigDecimal nodeCount;

	    public Result(BigDecimal count,BigDecimal nodeCount, int length) {
	      this.count = count;
	      this.length = length;
	      this.nodeCount=nodeCount;
	    }

	  }

}
