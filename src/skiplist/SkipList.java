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