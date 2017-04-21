/**
 * SkipList interface. These are the 3 most important functions we should implement.
 */

public interface SkipList<T extends Comparable<T>> {
    public boolean add(T value);
    public boolean remove(T value);
    public boolean contains(T value);
}
