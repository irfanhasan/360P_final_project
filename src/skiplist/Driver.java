/******************************************************************************
 * Driver		                                                               *
 *                                                                             *
 * Driver to test SkipList Implementation				                       *
 ******************************************************************************/

import java.util.*;

public class Driver {
	public static void main(String[] args) {
		SkipList<Integer> testList = new SkipList<Integer>();
		testList.add(10);
		testList.add(12);
		testList.add(14);
		testList.add(8);
		testList.printList();
		System.out.println(testList.contains(14));
		System.out.println(testList.size());
		testList.remove(12);
		testList.printList();
	}
}