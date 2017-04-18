/******************************************************************************
 * SkipList		                                                               *
 *                                                                             *
 * Implementation of Basic SkipList without cuncurrency	                       *
 * Does not support duplicates 												   *
 ******************************************************************************/

import java.util.*;

public class SkipList<E> {
	private SkipListNode<E> head;
	private int size;
	
	public SkipList() {
		head = new SkipListNode<E>(null);
		size = 0;
	}
	
	
	public boolean contains(E value) {
		
	}
	
	public boolean add (E value) {
		
	}
	
	public boolean remove(E value) {
		
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