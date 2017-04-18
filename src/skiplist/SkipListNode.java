/******************************************************************************
 * SkipListNode                                                                *
 *                                                                             *
 * Basic SkipListNode for SkipList					                           *
 ******************************************************************************/

import java.util.*;

public class SkipListNode<E> {
	private E value;
	
	public SkipListNode<E> left;
	public SkipListNode<E> right;
	public SkipListNode<E> up;
	public SkipListNode<E> down;

	public SkipListNode(E value) {
		this.value = value;
		left = null;
		right = null;
		up = null;
		down = null;
	}
		
	public E getValue() {
		return value;
	}

}