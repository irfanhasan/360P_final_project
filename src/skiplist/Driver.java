/******************************************************************************
 * Driver		                                                               *
 *                                                                             *
 * Driver to test SkipList Implementation				                       *
 ******************************************************************************/

import java.util.*;

public class Driver {
	public static void main(String[] args) {
		SkipList<Integer> testList = new SkipList<Integer>();
		//testList.add(5);
		System.out.println(testList.contains(0));
	}
}