package be.sysa.quartz.initializer.support;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class SetUtils {
    public static <T>  Set<T> intersection(Collection<T> existingJobs, Collection<T> keySet) {
        return existingJobs.stream().filter(keySet::contains).collect(Collectors.toSet());
    }
    public static <T> Set<T> minus(Collection<T> set1, Collection<T> set2) {
        return set1.stream().filter(k->!set2.contains(k)).collect(Collectors.toSet());
    }
}
