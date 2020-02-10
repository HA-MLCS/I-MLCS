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

public class RadixSortingbeta2 {

	public static ForkJoinPool pool;
/**
 * dominate sort and get locations of first layer
 */
	public static Set<Location> sort(Set<Location> locs, int level, double ratio) {

		List<SingleDeminsionSortedResult> sortedList = locationSort(locs);

		Set<Location> nondominatedSet = new HashSet<Location>();

		for (SingleDeminsionSortedResult sds : sortedList) {
			Set<Location> tempSet = new HashSet<Location>(sds.getFirst(1));
			nondominatedSet.addAll(tempSet);
		}

		Location minloc = nondominatedSet.iterator().next();

		for (SingleDeminsionSortedResult sds : sortedList) {
			nondominatedSet.addAll(sds.partialSet(minloc.index[sds.getDimension()]));
		}

		return MyBestOrderSort.sortforSet(new ArrayList<Location>(nondominatedSet));
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
				int x = (Integer) a[i] / (n * 10);
				if (x != 0) {
					hasNum = true;
				}
				int lsd = (x % 10);
				temp[lsd][order[lsd]] = (Integer) a[i];
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
}
