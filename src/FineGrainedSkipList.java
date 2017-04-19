/**
 * Implementation of a concurrent SkipList using a fine-grained approach.
 */

import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class FineGrainedSkipList<T extends Comparable<T>> implements SkipList<T> {
    
    private static final int maxHeight = 32;
    
    SkipListNode<T> head; 

    private class SkipListNode<T> {
        T value;
        Semaphore lock;
        ArrayList<SkipListNode<T>> nexts;
        boolean marked;
        boolean fullyLinked;
        int topLayer;

        SkipListNode(T val, int topLayer) {
            value = val;
            lock = new Semaphore(1);
            nexts = new ArrayList<SkipListNode<T>>(maxHeight);
            marked = false;
            fullyLinked = false;
            this.topLayer = topLayer;
        }
    }

    /**
     * Public functions
     */
    
    public FineGrainedSkipList() {
        head = new SkipListNode<T>(null, maxHeight);
        SkipListNode<T> tail = new SkipListNode<T>(null, maxHeight);
        for (int i = 0; i < maxHeight; i++) {
            head.nexts.set(i, tail);
        }
    }

    public boolean add(T val) {
        return false;
    }
    
    public boolean remove(T val) {
        return false;
    }

    public boolean contains(T val) {
        return false;
    }

    /**
     * Helper (internal) functions
     */
    
    /**
     * @return the highest layer at which value exists or -1 if value doesn't exist
     */
    private int findNode(T val, ArrayList<SkipListNode<T>> preds, ArrayList<SkipListNode<T>> succs) {
        int lFound = - 1;
        SkipListNode<T> pred = head;
        for (int layer = maxHeight - 1; layer >= 0; layer--) {
            SkipListNode<T> curr = pred.nexts.get(layer);
            
            while (curr.value != null && greaterThan(val, curr.value)) {
                pred = curr; curr = pred.nexts.get(layer);
            }

            if (lFound == -1 && equalTo(val, curr.value)) {
                lFound = layer;
            }
            preds.set(layer, pred);
            succs.set(layer, curr);
        }
        return lFound;
    }

	/******************************************************************************
	* Utility Functions                                                           *
	******************************************************************************/

	private boolean lessThan(E a, E b) {
		return a.compareTo(b) < 0;
	}

	private boolean equalTo(E a, E b) {
		if (a == null || b == null) { return false; }
		return a.compareTo(b) == 0;
	}

	private boolean greaterThan(E a, E b) {
		if (a == null) { return false; }
		if (b == null) { return true; }
		return a.compareTo(b) > 0;
	}

}
