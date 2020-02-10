package erlp.mlcs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The location of a single character in all strings
 */
public class Location implements Serializable {
    private static final long serialVersionUID = -1561971028727693347L;

    public short[] index;

    private int hashCode;

    private char name;

    public short[] getIndex() {
        return index;
    }

    public void setIndex(short[] index) {
        this.index = index;
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Location() {
    }

    @Override
    public String toString() {
        String str = Arrays.toString(index);
        str = str.replaceAll(" ", "");
      return "(" + str.substring(1, str.length() - 1) + ")";
    }

    public Location(short[] index) {
        this.index = index;
        this.hashCode = buildHashCode();
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
        if (bl == this) return true;
        short[] b = bl.index;
        short[] a = index;
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

    @SuppressWarnings("unchecked")
    public static ArrayList<Location>[] split(List<Location> locs, Mlcs mlcs, int optimalCount) {
        ArrayList<Location> optimalHeads = new ArrayList<Location>();
        ArrayList<Location> remindedHeads = new ArrayList<Location>();
        Collections.sort(locs, mlcs.sorter);
        for (int i = 0; i < locs.size(); i++) {
            Location head = locs.get(i);
            if (i < optimalCount) {
                optimalHeads.add(head);
            } else {
                remindedHeads.add(head);
            }
        }
        return new ArrayList[]{optimalHeads, remindedHeads};
    }

    /**
     * optimal path selector
     */
    public static class Sorter implements Comparator<Location> {

        public int compare(Location o1, Location o2) {
            int i = 0;
            short[] a = o1.index;
            short[] b = o2.index;
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
