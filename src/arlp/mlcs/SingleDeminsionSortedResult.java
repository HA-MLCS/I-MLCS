package arlp.mlcs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SingleDeminsionSortedResult {
	private int dimension;
	private LinkedHashMap<Integer, Set<Location>> result;

	public SingleDeminsionSortedResult(int dimension) {
		super();
		this.dimension = dimension;
		this.result = new LinkedHashMap<Integer, Set<Location>>();
	}

	public Set<Location> getFirst() {
		Iterator<Entry<Integer, Set<Location>>> iter = result.entrySet().iterator();
		if (iter.hasNext()) {
			return iter.next().getValue();
		} else {
			return new HashSet<Location>();
		}
	}

	public Set<Location> getFirst(int num) {
		Set<Location> forwardLocations = new HashSet<Location>();
		int n = 0;
		Iterator<Entry<Integer, Set<Location>>> iter = result.entrySet().iterator();
		while (iter.hasNext() && n < num) {
			forwardLocations.addAll(iter.next().getValue());
			n++;
		}
		return forwardLocations;
	}

	public Set<Location> getSecond() {
		Set<Location> forwardLocations = new HashSet<Location>();
		int n = 0;
		Iterator<Entry<Integer, Set<Location>>> iter = result.entrySet().iterator();
		while (iter.hasNext() && n < 2) {
			forwardLocations = iter.next().getValue();
			n++;
		}
		return forwardLocations;
	}

	public void add(Integer key, Set<Location> value) {
		result.put(key, value);
	}

	public Set<Location> partialSet(int limit) {
		Set<Location> part = new HashSet<Location>();
		Iterator<Entry<Integer, Set<Location>>> iter = result.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Set<Location>> entry = iter.next();
			if (entry.getKey() < limit) {
				part.addAll(entry.getValue());
			} else {
				break;
			}
		}
		return part;
	}

	public void delete(Location loc) {
		Integer key = loc.index[dimension];
		if (result.containsKey(key)) {
			result.get(key).remove(loc);
			if (result.get(key).isEmpty())
				result.remove(key);
		}
	}

	public boolean isEmpty() {
		return result.isEmpty();
	}

	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		for (Map.Entry<Integer, Set<Location>> entry : result.entrySet()) {
			for (Location loc : entry.getValue()) {
				strBuffer.append(loc + " ");
			}
			strBuffer.append("\n");
		}
		strBuffer.append("----------------");
		return strBuffer.toString();
	}

	public int getDimension() {
		return dimension;
	}

	// public static class KeyPair {
	// private short key;
	// private Set<Location> locSet;
	//
	// public KeyPair(short key, Set<Location> locSet) {
	// super();
	// this.key = key;
	// this.locSet = locSet;
	// }
	//
	// public short getKey() {
	// return key;
	// }
	//
	// public Set<Location> getLocSet() {
	// return locSet;
	// }
	//
	// public boolean isEmpty() {
	// return locSet.isEmpty();
	// }
	//
	// }

}
