package net.pcal.mobfilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Determines whether a given item is contained in a list.  Optimizes for the common case where the
 * list size is small or 1.
 */
public interface Matcher<T> {

    boolean isMatch(final T value);

    static <T> Matcher<T> of(final T[] matchItems) {
        final int HASH_CUTOFF = 3; // arrays bigger than this will go in a hashset
        if (matchItems.length == 0) {
            return new Matcher<T>() {
                @Override
                public boolean isMatch(final T s) {
                    return false;
                }
                @Override
                public String toString() {
                    return "[]";
                }
            };
        } else if (matchItems.length == 1) {
            return new Matcher<T>() {
                @Override
                public boolean isMatch(final T s) {
                    return s != null && s.equals(matchItems[0]);
                }
                @Override
                public String toString() {
                    return "[" + matchItems[0] + "]";
                }
            };
        } else if (matchItems.length <= HASH_CUTOFF) {
            return new Matcher<T>() {
                @Override
                public boolean isMatch(final T s) {
                    if (s == null) return false;
                    for (T a : matchItems) if (Objects.equals(a, s)) return true;
                    return false;
                }
                @Override
                public String toString() {
                    return Arrays.toString(matchItems);
                }
            };
        } else {
            final Set<T> set = new HashSet<>(Arrays.asList(matchItems));
            return new Matcher<T>() {
                @Override
                public boolean isMatch(final T s) {
                    if (s == null) return false;
                    return set.contains(s);
                }
                @Override
                public String toString() {
                    return Arrays.toString(matchItems);
                }
            };
        }
    }
}
