/**
 * Implementation of a concurrent SkipList using a fine-grained approach.
 */

import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class FineGrainedSkipList<T extends Comparable<T>> implements SkipList<T> {
   
    /**
     * (Private) data fields/inner classes
     */
    private static final int maxHeight = 32;
    
    Node<T> head; 

    private class Node<T> {
        T value;
        Semaphore lock;
        ArrayList<Node<T>> nexts;
        boolean marked;
        boolean fullyLinked;
        int topLevel;

        Node(T val, int topLevel) {
            value = val;
            lock = new Semaphore(1);
            nexts = new ArrayList<Node<T>>(maxHeight);
            marked = false;
            fullyLinked = false;
            this.topLevel = topLevel;
        }

        void acquireLock() {
            try {
                lock.acquire();
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Public functions
     */
    
    public FineGrainedSkipList() {
        head = new Node<T>(null, maxHeight);
        Node<T> tail = new Node<T>(null, maxHeight);
        for (int i = 0; i < maxHeight; i++) {
            head.nexts.set(i, tail);
        }
    }

    public boolean add(T val) {
        return false;
    }
    
    public boolean remove(T val) {
       Node<T> nodeToDelete = null;
       boolean isMarked = false;
       int topLevel = -1;
       ArrayList<Node<T>> preds = new ArrayList<Node<T>>(maxHeight);
       ArrayList<Node<T>> succs = new ArrayList<Node<T>>(maxHeight);
       while(true) {
           int found = findNode(val, preds, succs);
           if (isMarked || (found != -1 && okToDelete(succs.get(found), found))) {
               if(!isMarked) {
                   nodeToDelete = succs.get(found);
                   topLevel = nodeToDelete.topLevel;
                   nodeToDelete.acquireLock();
                   
                   if(nodeToDelete.marked) {
                       nodeToDelete.lock.release();
                       return false; //node already 'logically' deleted
                   }
                   nodeToDelete.marked = true;
                   isMarked = true;
               }
               int maxLocked = -1;
               try {
                   Node<T> succ, pred, prevPred = null;
                   boolean valid = true;
                   for (int i = 0; valid && i <= topLevel; i++) {
                       pred = preds.get(i);
                       succ = succs.get(i);
                       if (pred != prevPred) {
                           pred.acquireLock();
                           maxLocked = i;
                           prevPred = pred;
                       }
                       //need to check that predecessor and successor are stil valid elements
                       valid = !pred.marked && pred.nexts.get(i).equals(succ); 
                   }
                   if (!valid) continue; //retry deletion

                   for (int i = topLevel; i >= 0; i--) {
                       preds.get(i).nexts.set(i, nodeToDelete.nexts.get(i));
                   }
                   nodeToDelete.lock.release();
                   return true;
               } finally { unlock(preds, maxLocked); }
           } else {
               return false;
           } //end if
       } //end while
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
    private int findNode(T val, ArrayList<Node<T>> preds, ArrayList<Node<T>> succs) {
        int lFound = - 1;
        Node<T> pred = head;
        for (int level = maxHeight - 1; level >= 0; level--) {
            Node<T> curr = pred.nexts.get(level);
            
            while (curr.value != null && greaterThan(val, curr.value)) {
                pred = curr; curr = pred.nexts.get(level);
            }

            if (lFound == -1 && equalTo(val, curr.value)) {
                lFound = level;
            }
            preds.set(level, pred);
            succs.set(level, curr);
        }
        return lFound;
    }


    /**
     *@return whether the node at the level it was found can be deleted
     */
    private boolean okToDelete(Node<T> node, int level) {
        return node.fullyLinked && !node.marked && node.topLevel == level;
    }

    private void unlock(ArrayList<Node<T>> nodes, int maxLevel) {
        for (int i = maxLevel; i >= 0; i--) {
            nodes.get(i).lock.release();
        }
    }

    /******************************************************************************
    * Utility Functions                                                           *
    ******************************************************************************/

    private boolean lessThan(T a, T b) {
        return a.compareTo(b) < 0;
    }

    private boolean equalTo(T a, T b) {
        if (a == null || b == null) { return false; }
        return a.compareTo(b) == 0;
    }

    private boolean greaterThan(T a, T b) {
        if (a == null) { return false; }
        if (b == null) { return true; }
        return a.compareTo(b) > 0;
    }

}
