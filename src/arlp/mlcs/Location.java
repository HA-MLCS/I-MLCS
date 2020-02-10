package arlp.mlcs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;


public class Location implements Serializable, Comparable<Location> {
	private static final long serialVersionUID = -1561971028727693347L;

	public int id;

	public int[] index;   //subscript of a point

	public int maxIndex = Integer.MIN_VALUE;
	
	private int maxDimValue;     //score of the location

	public int indexSum;

	private char name;

	private int hashCode;

	public Location() {
	}

	@Override
	public String toString() {
		String str = Arrays.toString(index);
		str = str.replaceAll(" ", "");
		return "(" + str.substring(1, str.length() - 1) + ")";
	}

	public Location(int id, int[] index) {
		this.id = id;
		this.index = index;
		this.hashCode = buildHashCode();
		
		for (int i : index) {
			indexSum += i;
			if (i > maxIndex)
				maxIndex = i;
		}
	}

	public Location(int[] index) {
		this.index = index;
		this.hashCode = buildHashCode();
		this.maxDimValue = getMaxDimValue();
		for (int i : index) {
			indexSum += i;
			if (i > maxIndex)
				maxIndex = i;
		}
	}

	private int buildHashCode() {
		int rs = 1;
		int i = 0;
		while (i < index.length) {
			rs = 31 * rs + index[i];
			i += 1;
		}
		return rs;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		Location bl = (Location) o;
		if (bl == this)
			return true;
		int[] b = bl.index;
		int[] a = index;
		int i = 0;
		boolean equals = true;
		while (i < a.length && equals) {
			if (a[i] != b[i]) {
				equals = false;
			}
			i += 1;
		}
		return equals;
	}
	
	
	
	/**
	 * 
	 * caculate score of the location,and delete the locations that has high score
	 * score = max /2 + sum/(2*index.length)
	 */
	  public int getMaxDimValue(){
		  int max = 0;
		  int sum = 0;
		  for(int i = 0;i < index.length;i++){
			  if(index[i] > max)
				  max = index[i];
			  
			  sum += index[i];
		  }

		  int v = max /2 + sum/(2*index.length);
		  return (int)v;
	  }
	/**
	 *   
	 * get score of location 
	 */
	  public synchronized int getMax(){
		  return maxDimValue;
	  }

	@Override
	public int compareTo(Location other) {
		boolean dominate = true;
		boolean isDominated = true;
		for (int i = 0; i < index.length; i++) {
			if (this.index[i] < other.index[i]) {
				dominate &= true;
				isDominated &= false;
			} else if (this.index[i] == other.index[i]) {
				dominate &= true;
				isDominated &= true;
			} else {
				dominate &= false;
				isDominated &= true;
			}
		}
		if (dominate) {
			return -1;
		} else if (isDominated) {
			return 1;
		} else {
			return 0;
		}
	}

	public char getName() {
		return name;
	}

	public void setName(char name) {
		this.name = name;
	}
	
	public static class Sorter implements Comparator<Location> {

	    @Override
	    public int compare(Location o1, Location o2) {
	      int i = 0;
	      int[] a = o1.index;
	      int[] b = o2.index;
	      int less = 0;
	      int greate = 0;
	      int lessIndex = 0;
	      int oLessIndex = 0;
	      while (i < a.length) {
	        if (a[i] < b[i]) {
	          less += 1;
	          lessIndex += i;
	        } else if (a[i] > b[i]) {
	          greate += 1;
	          oLessIndex += i;
	        }
	        i += 1;
	      }
	      if (less > greate) return -1;
	      else if (less < greate) return 1;
	      else return lessIndex - oLessIndex;
	    }

	  }
}


  


