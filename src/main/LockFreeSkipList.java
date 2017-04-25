import java.lang.reflect.Array;
import java.util.concurrent.atomic.*;
import java.util.ArrayList;

public class LockFreeSkipList<T extends Comparable<T>> implements SkipList<T> {
	   
    private static final int MaxHeight = 32;
    
    Node head; 

    private class Node {
        T value;
        NodeArrayMarkable nexts;
        AtomicBoolean marked;
        boolean fullyLinked;
        int topLevel;

        Node(T val, int topLevel) {
            value = val;
            nexts = new NodeArrayMarkable(MaxHeight);
            marked = new AtomicBoolean(false);
            fullyLinked = false;
            this.topLevel = topLevel;
        }  
    }

    private class NodeArrayMarkable {
        private ArrayList<AtomicMarkableReference<Node>> arr;
        
        NodeArrayMarkable(int n) {
            arr = new ArrayList<AtomicMarkableReference<Node>>();
            for (int i = 0; i < n; i++) {
                arr.add(new AtomicMarkableReference<Node>(null, false));
            }
        }

        Node get(int i) {
            return arr.get(i).getReference();
        }

        AtomicMarkableReference<Node> getAtomic(int i) {
            return arr.get(i);
        }

        void set(int i, Node node) {
            arr.get(i).set(node, false);
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
    
    public LockFreeSkipList() {
        head = new Node(null, MaxHeight);
        Node tail = new Node(null, MaxHeight);
        for (int i = 0; i < MaxHeight; i++) {
            head.nexts.set(i, tail);
        }
    }


	/**
	  * Adds an element into skip list
      *
	  * @param val element to be added
	  * @return boolean true or false depending on whether element was sucessfully added
	  */
    public boolean add(T val) {
        int topLayer = randomLevel(MaxHeight);
        NodeArray preds = new NodeArray(MaxHeight);
        NodeArray succs = new NodeArray(MaxHeight);
        
        Node newNode = new Node(val, topLayer);
        int layer = 0;
        boolean inProgress = false;
        while(true){
            int lFound = findNode(val, preds, succs);
            if(!inProgress && lFound!=-1){
                Node nodeFound = succs.get(lFound);
                if(!nodeFound.marked.get()){ // Element already is being added by another process
                    while(!nodeFound.fullyLinked); // wait for other thread to complete
                    return false;
                }
                continue;
            }
            
            inProgress = true;
            boolean valid = true;
            for(; layer<=topLayer; layer++){
                newNode.nexts.set(layer, succs.get(layer)); // Updated newNode.next to succ
                
                AtomicMarkableReference<Node> atomicNode = preds.get(layer).nexts.getAtomic(layer);
                if (!atomicNode.compareAndSet(succs.get(layer), newNode, false, false)) { // Atomically update pred.next to newNode
                    valid = false;
                    break;
                }
            }
            if (!valid) continue; // retry addition from the location where you left off
            
            newNode.fullyLinked = true;
            return true;
        }
    }
    
	/**
	  * Deletes an element into skip list
      *
	  * @param val element to be Deleted
	  * @return boolean true or false depending on whether element was sucessfully deleted
	  */
    public boolean remove(T val) {
        Node nodeToDelete = null;
        boolean isMarked = false;
        int topLevel = -1;
        NodeArray preds = new NodeArray(MaxHeight);
        NodeArray succs = new NodeArray(MaxHeight);
        int i = topLevel;
        while(true) {
            int found = findNode(val, preds, succs);
            if (isMarked || (found != -1 && okToDelete(succs.get(found), found))) {
                if(!isMarked) {
                    nodeToDelete = succs.get(found);
                    topLevel = nodeToDelete.topLevel;
                    i = topLevel;
                    if(!nodeToDelete.marked.compareAndSet(false, true)) { // set marked bit for logical deletion
                        return false;
                    }
                    isMarked = true;
                }

                boolean valid = true;
                for (; i >= 0; i--) {
                    AtomicMarkableReference<Node> atomicNode = preds.get(i).nexts.getAtomic(i);
                    if(!atomicNode.compareAndSet(succs.get(i), nodeToDelete.nexts.get(i), false, false)) { // update pred.next to newNode.next
                        valid = false;
                        break;
                    }
                }
                if (!valid) { // retry from where you left off
                    continue;
                }
                return true;
            } else {
                return false;
            }
        }
    }
    
	/**
	  * Checks if element is in skip list
      *
	  * @param val val to check for
	  * @return true if exists false otherwise
	  */
    public boolean contains(T val) {
        NodeArray preds = new NodeArray(MaxHeight);
        NodeArray succs = new NodeArray(MaxHeight);
        int lFound = findNode(val, preds, succs);
        return  (lFound!=-1) 
                && (succs.get(lFound).fullyLinked) 
                && (!succs.get(lFound).marked.get());
    }
    
    /**
     * Helper (internal) functions
     */
    
    /**
     * Searches for element in list
     * @param val element to find
     * @param pred will hold array of preds upon function return
     * @param succs will hold array of succs upon function return
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
        return node.fullyLinked && !node.marked.get() && node.topLevel == level;
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
