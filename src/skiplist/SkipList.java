/******************************************************************************
 * SkipList		                                                               *
 *                                                                             *
 * Implementation of Basic SkipList without cuncurrency	                       *
 * Does not support duplicates 												   *
 ******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkipList<E extends Comparable<E>> {
	private SkipListNode<E> head;
	private int size;
	private static final int MAX_HEIGHT = 32;
	
	public SkipList() {
		head = new SkipListNode<E>(null);
		SkipListNode<E> current = head;
		for (int i = 0; i < MAX_HEIGHT-1; i++) {
			SkipListNode<E> next = new SkipListNode<E>(null);
			current.down = next;
			next.up = current;
			current = next;
		}
		size = 0;
	}
	
	public SkipListNode<E> find(E value) {
		SkipListNode<E> current = head;
		while (current != null) {
			if (current.getValue() == value) {
				while (current.down != null) {
					current = current.down;
				}
				return current;
			}
			
			if (current.right == null || greaterThan(current.right.getValue(), value)) {
				current = current.down;
			} else {
				current = current.right;
			}
		}
		
		return null;
	}
	
	public boolean add(E value) {
		if (contains(value)) { return false; }
		SkipListNode<E> current = head;
		// Find node before insertion
		while (current != null) {
			if ((current.getValue() == null || lessThan(current.getValue(), value)) && (current.right == null || greaterThan(current.right.getValue(), value)) && current.down == null) {
				break;
			}
			
			if (current.right == null || greaterThan(current.right.getValue(), value)) {
				current = current.down;
			} else {
				current = current.right;
			}
		}
		
		// Calculate height
		int count = 1;
		while (true) {
			int r = ThreadLocalRandom.current().nextInt(0, 2);
			if (r == 1) {
				count++;
			} else { 
				break; 
			}
		}
		if (count > MAX_HEIGHT) { count = MAX_HEIGHT; }
		
		ArrayList<SkipListNode<E>> addedNodes = new ArrayList<SkipListNode<E>>();
		for (int i = 0; i < count; i++) {
			// Insert in row (modify right and left nodes)
			SkipListNode<E> next = current.right;
			SkipListNode<E> newNode = new SkipListNode<E>(value);
			newNode.left = current;
			newNode.right = next;
			current.right = newNode;
			if (next != null) {
				next.left = newNode;
			}
			
			// Add up and down nodes
			if (i > 0) {
				newNode.down = addedNodes.get(i-1);
				addedNodes.get(i-1).up = newNode;
			}
			
			addedNodes.add(newNode);
			while (current.up == null) {
				current = current.left;
			}
			current = current.up;
			
		}
		size++;
		return true;
	}
	
	public boolean remove(E value) {
		SkipListNode<E> toBeDeleted = find(value);
		if (toBeDeleted == null || value == null) { return false; }
		
		do {
			SkipListNode<E> prev = toBeDeleted.left;
			SkipListNode<E> next = toBeDeleted.right;
			if (prev != null) {
				prev.right = next;
			}
			if (next != null) {
				next.left = prev;
			}
			toBeDeleted = toBeDeleted.up;
		} while (toBeDeleted != null);
		
		size--;
		return true;
	}
	
	public boolean contains(E value) {
		SkipListNode<E> n = find(value);
		return n != null;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void clear() {
		head = new SkipListNode<E>(null);
		size = 0;
	}
	
	public int size() {
		return size;
	}
	
	public void printList() {
		SkipListNode<E> currentHead = head;
		
		int level = 31;
		while(currentHead != null) {
			System.out.print("[" + level + "]" + " * ");
			
			SkipListNode<E> nodeToPrint = currentHead.right;
			while(nodeToPrint != null) {
				System.out.print(nodeToPrint.getValue() + " ");
				nodeToPrint = nodeToPrint.right;
			}
			
			System.out.print("\n");
			currentHead = currentHead.down;
			level--;
		}
	}
	
	
	/******************************************************************************
	* Utility Functions                                                           *
	******************************************************************************/

	private boolean lessThan(E a, E b) {
		if (a == null) { return true; }
		if (b == null) { return false; }
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