/**
 * Implementation of a concurrent SkipList using a fine-grained approach.
 */

import java.util.concurrent.Semaphore;
import java.util.ArrayList;

import java.util.Collections;
import java.lang.reflect.Array;

public class FineGrainedSkipList<T extends Comparable<T>> implements SkipList<T> {
   
    /**
     * (Private) data fields/inner classes
     */
    private static final int MaxHeight = 32;
    
    Node head; 
    

    private class Node {
        T value;
        Semaphore lock;
        //ArrayList<Node> nexts;
        NodeArray nexts;
        boolean marked;
        boolean fullyLinked;
        int topLevel;

        Node(T val, int topLevel) {
            value = val;
            lock = new Semaphore(1);
            nexts = new NodeArray(MaxHeight);
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

    private class NodeArray {
        private Node[] arr;

        NodeArray(int n) {
            arr = (Node[]) Array.newInstance(Node.class, n);
        }

        Node get(int i) {
            return arr[i];
        }

        void set(int i, Node node) {
            arr[i] = node;
        }
    }

    /**
     * Public functions
     */
    
    public FineGrainedSkipList() {
        head = new Node(null, MaxHeight);
        Node tail = new Node(null, MaxHeight);
        for (int i = 0; i < MaxHeight; i++) {
            head.nexts.set(i, tail);
        }
    }

    public boolean add(T val) {
        int topLayer = randomLevel(MaxHeight);
        NodeArray preds = new NodeArray(MaxHeight);
        NodeArray succs = new NodeArray(MaxHeight);
        while(true){
            int lFound = findNode(val, preds, succs);
            if(lFound!=-1){
                Node nodeFound = succs.get(lFound);
                if(!nodeFound.marked){
                    while(!nodeFound.fullyLinked);
                    return false;
                }
                continue;
            }
            int highestLocked = -1;
            try{
                Node pred, succ, prevPred = null;
                boolean valid = true;
                for(int layer=0; valid && (layer<=topLayer); layer++){
                    pred = preds.get(layer);
                    succ = succs.get(layer);
                    if(pred!=prevPred){
                        pred.acquireLock();
                        highestLocked = layer;
                        prevPred = pred;
                    }
                    valid = (!pred.marked) && (!succ.marked) && (pred.nexts.get(layer)==succ);
                }
                if(!valid) continue;
                
                Node newNode = new Node(val, topLayer);
                for(int layer=0; layer<=topLayer; layer++){
                    newNode.nexts.set(layer, succs.get(layer));
                    preds.get(layer).nexts.set(layer, newNode);
                }
                
                newNode.fullyLinked = true;
                return true;
            }finally{
                unlock(preds, highestLocked);
            }
        }
    }
    
    public boolean remove(T val) {
       Node nodeToDelete = null;
       boolean isMarked = false;
       int topLevel = -1;
       NodeArray preds = new NodeArray(MaxHeight);
       NodeArray succs = new NodeArray(MaxHeight);
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
                   Node succ, pred, prevPred = null;
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
        NodeArray preds = new NodeArray(MaxHeight);
        NodeArray succs = new NodeArray(MaxHeight);
        int lFound = findNode(val, preds, succs);
        return  (lFound!=-1) 
                && (succs.get(lFound).fullyLinked) 
                && (!succs.get(lFound).marked);
    }
    
    /**
     * prints the list
     */
    public void print(){
        
    }

    /**
     * Helper (internal) functions
     */
    
    /**
     * @return the highest layer at which value exists or -1 if value doesn't exist
     */
    private int findNode(T val, NodeArray preds, NodeArray succs) {
        int lFound = - 1;
        Node pred = head;
        for (int level = MaxHeight - 1; level >= 0; level--) {
            Node curr = pred.nexts.get(level);
            
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
    private boolean okToDelete(Node node, int level) {
        return node.fullyLinked && !node.marked && node.topLevel == level;
    }

    private void unlock(NodeArray nodes, int maxLevel) {
        for (int i = maxLevel; i >= 0; i--) {
            nodes.get(i).lock.release();
        }
    }

    private int randomLevel(int v){
        int lvl = (int)(Math.log(1.-Math.random())/Math.log(1.-0.5));
        return Math.min(lvl, v);
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
