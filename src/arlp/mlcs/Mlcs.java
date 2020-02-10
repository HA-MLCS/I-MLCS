package arlp.mlcs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class Mlcs {
  Set<Character> charset;  //the set contains all the characters that sequences have
  List<Sequence> seqs; // all the initial sequences

  public Location start = null; // start location
  Location end = null; // end location

  int[][][] successorTable; // char->seq->table


  int currentLevel = 0;

  int maxThread = 1;
  int length = 0;
  Location.Sorter sorter = new Location.Sorter();

/**
 * 
 * building successors tables
 */
  public Mlcs(Set<Character> charset, List<Sequence> seqs) {
    this.charset = charset;
    this.seqs = seqs;
    start = Mlcs.buildStart(seqs.size());
    end = Mlcs.buildEnd(seqs);
    successorTable = new int[charset.size()][][];
    List<Character> charList = new ArrayList<Character>(charset);
    for (int i = 0; i < charList.size(); i++) {
      successorTable[i] = new int[seqs.size()][];
      for (int j = 0; j < seqs.size(); j++) {
        Sequence seq = seqs.get(j);
        successorTable[i][j] = seq.buildSuccessors(charList.get(i).charValue());
      }
    }

    currentLevel = seqs.get(0).length() - 1;
    length = seqs.get(0).length() - 1;
  }

  public char charAt(Location location) {
    return seqs.get(0).charAt(location.index[0]);
  }

  /**
   * get all successors of current ,and sort by value from small to large
   * 
   */
  public List<Location> nextSortedLocations(Location current) {
    List<Location> locations = nextLocations(current);
    Collections.sort(locations, sorter);
    return locations;
  }

  /**
   * get all successors of current
   */
  public List<Location> nextLocations(Location current) {
    List<Location> nexts = new ArrayList<Location>(charset.size());
    for (int i = 0; i < successorTable.length; i++) {
      int snum = seqs.size();
      int[] tmp = new int[snum];
      for (int j = 0; j < seqs.size(); j++) {
        int successor = successorTable[i][j][current.index[j]];
        if (successor < 0 || successor > currentLevel) break;
        else tmp[j] = successor;
      }
      if (tmp[tmp.length - 1] > 0) nexts.add(new Location(tmp));
    }
    return nexts;
  }

  
/**
 * 
 */


  /**
   * build start
   */
  public static Location buildStart(int length) {
    return new Location(new int[length]);
  }

  /**
   * build end
   */
  public static Location buildEnd(List<Sequence> seqs) {
    int[] index = new int[seqs.size()];
    int i = 0;
    for (Sequence seq : seqs) {
      index[i] = (int) seq.length();
      i += 1;
    }
    return new Location(index);
  }

  /**
   * load data from file 
   */
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
   * statistical result of MLCS
   */
  public static class Result {

    BigDecimal count; //  the number of result
    int length; // the length of result

    public Result(BigDecimal count, int length) {
      this.count = count;
      this.length = length;
    }

  }

}
