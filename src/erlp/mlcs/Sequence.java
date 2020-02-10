package erlp.mlcs;

import java.util.Set;

public class Sequence {

    char[] chars;

    public Sequence(char[] chars) {
        super();
        this.chars = chars;
    }

    public Set<Character> charsets() {
        Set<Character> cs = new java.util.HashSet<Character>();
        int i = 1;
        while (i < chars.length) {
            cs.add(chars[i]);
            i += 1;
        }
        return cs;
    }

    public char charAt(int index) {
        return chars[index];
    }

    public int length() {
        return chars.length;
    }

    public static Sequence build(String s) {
        char[] c = new char[s.length() + 1];
        c[0] = '.';
        s.getChars(0, s.length(), c, 1);
        return new Sequence(c);
    }

    /**
     * Find the successors of the current point with respect to a character
     */
    public short[] buildSuccessors(char c) {

        int length = chars.length;
        short[] successors = new short[length];
        int i = 0;
        int j = i + 1;
        while (i < length) {
            if (j == i) j += 1;
            while (j < length && chars[j] != c) {
                j += 1;
            }
            if (j == length) successors[i] = -1;
            else successors[i] = (short) j;
            i += 1;
        }
        return successors;
    }


}
