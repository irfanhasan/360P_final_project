/**
 * Implementation of a concurrent SkipList using a fine-grained approach.
 */

import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class FineGrainedSkipList<T extends Comparable<T>> implements SkipList<T> {
    
    private static final int maxHeight = 32;
    
    private class SkipListNode<T> {
        T value;
        Semaphore lock;
        ArrayList<SkipListNode<T>> nexts;
        boolean marked;
        boolean fullyLinked;

        SkipListNode(T val) {
            value = val;
            lock = new Semaphore(1);
            nexts = new ArrayList<SkipListNode<T>>(maxHeight);
            marked = false;
            fullyLinked = false;
        }
    }
    /**
     * Public functions
     */
    public boolean add(T val) {
        return false;
    }
    
    public boolean remove(T val) {
        return false;
    }

    public boolean contains(T val) {
        return false;
    }
}
