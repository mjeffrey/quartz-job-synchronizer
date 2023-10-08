package be.sysa.quartz.initializer.support;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides utility methods for performing set operations.
 */
public class SetUtils {

    /**
     * Returns the intersection of two collections.
     *
     * @param set1 the first collection
     * @param set2 the second collection
     * @param <T> the type of elements in the collections
     * @return a set containing the common elements of set1 and set2
     */
    public static <T>  Set<T> intersection(Collection<T> set1, Collection<T> set2) {
        return set1.stream().filter(set2::contains).collect(Collectors.toSet());
    }
    /**
     * Returns the set difference of two collections.
     *
     * @param set1 the first collection
     * @param set2 the second collection
     * @param <T> the type of the elements in the collections
     * @return a set containing the elements that are present in the first collection but not in the second collection
     */
    public static <T> Set<T> minus(Collection<T> set1, Collection<T> set2) {
        return set1.stream().filter(k->!set2.contains(k)).collect(Collectors.toSet());
    }
}
