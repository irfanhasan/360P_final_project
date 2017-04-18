/******************************************************************************
 * SkipList		                                                               *
 *                                                                             *
 * Implementation of Basic SkipList without cuncurrency	                       *
 * Does not support duplicates 												   *
 ******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkipList<E> {
	private SkipListNode<E> head;
	private int size;
	
	public SkipList() {
		head = new SkipListNode<E>(null);
		size = 0;
	}
	
	public SkipListNode<E> find(E value) {
		SkipListNode<E> current = head;
		while (current != null) {
			if (current.getValue() == value) {
				return current;
			}
			
			if (current.right == null || current.right.getValue() > current.getValue()) {
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
			if ((current.getValue() == null || current.getValue() < value) && (current.right == null || current.right.getValue() > value) && current.down == null) {
				return current;
			}
			
			if (current.right == null || current.right.getValue() > current.getValue()) {
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
		
		ArrayList<SkipListNode<E>> addedNodes = new ArrayList<SkipListNode<E>>();
		for (int i = 0; i < count; i++) {
			// Insert in row (modify right and left nodes)
			next = current.right;
			newNode = new SkipListNode<E>(value);
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
				if (current.left == null) {
					current.up = new SkipListNode<E>(null);
					break;
				}
				current = current.left;
			}
			current = current.up;	
		}
		
		return true;
	}
	
	public boolean remove(E value) {
		
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

}