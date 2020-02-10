package arlp.mlcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class RadixSorting {

	public static ForkJoinPool pool;

	public static Set<Location> sort(Set<Location> locs, int level, double ratio) {
		Set<Location> result = new HashSet<Location>();
		List<SingleDeminsionSortedResult> sortedList = locationSort(locs);
		int n = 0;
		while (n < level) {
			Set<Location> nondominatedSet = new HashSet<Location>();

			Integer min = Integer.MAX_VALUE;
			Location minloc = null;

			for (SingleDeminsionSortedResult sds : sortedList) {
				Set<Location> tempSet = new HashSet<Location>(sds.getFirst(2));
				Location tempLocation = nondominate(tempSet);
				if (tempLocation.maxIndex + tempLocation.indexSum < min) {
					minloc = tempLocation;
				}
				nondominatedSet.addAll(tempSet);
			}

			Set<Location> spareSet = new HashSet<Location>();

//			for (SingleDeminsionSortedResult sds : sortedList) {
//				spareSet.addAll(sds.partialSet(minloc.index[sds.getDimension()]));
//			}

			// Iterator<Location> iter = spareSet.iterator();
			// while (iter.hasNext()) {
			// Location loc = iter.next();
			// if (minloc.compareTo(loc) < 0) {
			// iter.remove();
			// }
			// }

			List<Location> tempList = new LinkedList<Location>(spareSet);
			spareSet.clear();
			spareSet.addAll(tempList.subList(0, (int) (tempList.size() * Math.pow(ratio, n))));
			for (Location loc : spareSet) {
				if (isAdded(loc, nondominatedSet)) {
					nondominatedSet.add(loc);
				}
			}

			result.addAll(nondominatedSet);
			n++;
		}
		return result;
	}

	public static boolean isAdded(Location loc, Set<Location> nondominatedSet) {
		boolean state = true;
		Iterator<Location> iter = nondominatedSet.iterator();
		while (iter.hasNext()) {
			Location l = iter.next();
			if (l.compareTo(loc) == -1) {
				state = false;
				break;
			} else if (l.compareTo(loc) == 1) {
				iter.remove();
			}
		}
		return state;
	}

	public static Location nondominate(Set<Location> nondominatedSet) {
		Set<Location> result = new HashSet<Location>(nondominatedSet);
		Iterator<Location> iter = nondominatedSet.iterator();
		while (iter.hasNext()) {
			Location loc = iter.next();
			result.remove(loc);
			for (Location l : result) {
				if (loc.compareTo(l) > 0) {
					iter.remove();
					break;
				}
			}
			result.add(loc);
		}
		Integer min = Integer.MAX_VALUE;
		Location minloc = null;
		for (Location loc : nondominatedSet) {
			if (loc.maxIndex + loc.indexSum < min) {
				minloc = loc;
			}
		}

		return minloc;
	}

	public static List<SingleDeminsionSortedResult> locationSort(Set<Location> locs) {
		List<SingleDeminsionSortedResult> result = new LinkedList<SingleDeminsionSortedResult>();
		int deminsion = locs.iterator().next().index.length;
		int n = 0;
		while (n < deminsion) {
			Map<Integer, Set<Location>> keyLocMap = new HashMap<Integer, Set<Location>>();
			for (Location loc : locs) {
				int i = loc.index[n];
				if (!keyLocMap.containsKey(i)) {
					keyLocMap.put(i, new HashSet<Location>());
				}
				keyLocMap.get(i).add(loc);
			}

			Object[] key = keyLocMap.keySet().toArray();
			sort(key, 10);
			SingleDeminsionSortedResult sortedResult = new SingleDeminsionSortedResult(n);
			for (int i = 0; i < key.length; i++) {
				sortedResult.add((Integer) key[i], keyLocMap.get(key[i]));
			}
			result.add(sortedResult);
			n++;
		}
		return result;
	}

	/// 基数排序函数
	// a表示要排序的数组
	// d表示每一位数字的范围（这里是10进制数，有0~9一共10种情况）
	public static void sort(Object[] a, int d) {
		// n用来表示当前排序的是第几位
		int n = 1;
		// hasNum用来表示数组中是否有至少一个数字存在第n位
		boolean hasNum = false;
		// 二维数组temp用来保存当前排序的数字
		// 第一维d表示一共有d个桶
		// 第二维a.length表示每个桶最多可能存放a.length个数字
		int[][] temp = new int[d][a.length];
		int[] order = new int[d];
		while (true) {
			// 判断是否所有元素均无比更高位，因为第一遍一定要先排序一次，所以有n!=1的判断
			if (n != 1 && !hasNum) {
				break;
			}
			hasNum = false;
			// 遍历要排序的数组，将其存入temp数组中（按照第n位上的数字将数字放入桶中）
			for (int i = 0; i < a.length; i++) {
				int x = (int) a[i] / (n * 10);
				if (x != 0) {
					hasNum = true;
				}
				int lsd = (x % 10);
				temp[lsd][order[lsd]] = (int) a[i];
				order[lsd]++;
			}
			// k用来将排序好的temp数组存入data数组（将桶中的数字倒出）
			int k = 0;
			for (int i = 0; i < d; i++) {
				if (order[i] != 0) {
					for (int j = 0; j < order[i]; j++) {
						a[k] = temp[i][j];
						k++;
					}
				}
				order[i] = 0;
			}
			n++;
		}
	}

	public static List<int[]> split(int total, int n) {
		List<int[]> buffer = new ArrayList<int[]>();
		if (total <= n) {
			buffer.add(new int[] { 0, total });
		} else {
			int sliceCount = (total / n);
			int i = 0;
			while (i < n - 1) {
				buffer.add(new int[] { i * sliceCount, (i + 1) * sliceCount });
				i += 1;
			}
			buffer.add(new int[] { (n - 1) * sliceCount, total });
		}
		return buffer;
	}

	@SuppressWarnings("unchecked")
	public static <A> LinkedList<A>[] split(LinkedList<A> data, int n) {
		if (data.size() <= n) {
			LinkedList<A> rs = new LinkedList<A>(data);
			data.clear();
			return new LinkedList[] { rs };
		} else {
			LinkedList<A>[] datas = new LinkedList[n];
			for (int i = 0; i < n; i++) {
				datas[i] = new LinkedList<A>();
			}
			int total = data.size();
			for (int i = 0; i < total; i++) {
				datas[i % n].addLast(data.removeFirst());
			}
			return datas;
		}
	}

	@SuppressWarnings("serial")
	static class Crawler extends RecursiveTask<AddedResult> {
		Location loc;
		ArrayList<Location> locations;
		int from, to;

		public Crawler(Location loc, ArrayList<Location> locations, int from, int to) {
			this.loc = loc;
			this.locations = locations;
			this.from = from;
			this.to = to;
		}

		public AddedResult compute() {
			ArrayList<Location> removeLocations = new ArrayList<Location>();
			for (int i = from; i < to; i++) {
				Location current = locations.get(i);
				if (current.compareTo(loc) == -1) {
					return new AddedResult(false, removeLocations);
				} else if (current.compareTo(loc) == 1) {
					removeLocations.add(current);
				}
			}
			return new AddedResult(true, removeLocations);
		}
	}

	@SuppressWarnings("serial")
	static class Crawler2 extends RecursiveTask<AddedResult> {
		Location loc;
		Location current;

		public Crawler2(Location loc, Location current) {
			this.loc = loc;
			this.current = current;
		}

		public AddedResult compute() {
			ArrayList<Location> removeLocations = new ArrayList<Location>();
			if (current.compareTo(loc) == -1) {
				return new AddedResult(false, removeLocations);
			} else if (current.compareTo(loc) == 1) {
				removeLocations.add(current);
			}
			return new AddedResult(true, removeLocations);
		}
	}

	public static boolean isAddedParallelly(Location loc, Set<Location> nondominatedSet) {
		pool = new ForkJoinPool();
		boolean state = true;
		ArrayList<Location> locations = new ArrayList<Location>(nondominatedSet);
		int parallelism = pool.getParallelism();
		int parallelThreshhold = parallelism * 7;
		List<int[]> segs = null;
		if (locations.size() < parallelThreshhold) {
			segs = split(locations.size(), locations.size());
		} else {
			segs = split(locations.size(), parallelism);
		}

		LinkedList<ForkJoinTask<AddedResult>> tasks = new LinkedList<ForkJoinTask<AddedResult>>();
		for (int[] seg : segs) {
			tasks.addLast(pool.submit(new Crawler(loc, locations, seg[0], seg[1])));
		}

		// for (Location current : nondominatedSet) {
		// tasks.addLast(pool.submit(new Crawler2(loc, current)));
		// }

		for (ForkJoinTask<AddedResult> task : tasks) {
			AddedResult addedResult = task.join();
			if (addedResult.isAdd) {
				for (Location l : addedResult.removeLocations) {
					nondominatedSet.remove(l);
				}
			} else {
				state = false;
				pool.shutdown();
				break;
			}
		}
		return state;
	}

	static class AddedResult {
		boolean isAdd;
		ArrayList<Location> removeLocations;

		public AddedResult(boolean isAdd, ArrayList<Location> removeLocations) {
			super();
			this.isAdd = isAdd;
			this.removeLocations = removeLocations;
		}

		public boolean isAdd() {
			return isAdd;
		}

		public ArrayList<Location> getRemoveLocations() {
			return removeLocations;
		}

	}
}
