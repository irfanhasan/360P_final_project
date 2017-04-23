/**
 * A class to test the performance of our Skip List against the one found in the Java libraries.
 */

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


import java.util.LinkedList;

import java.util.Random;
import java.util.Scanner;

public class FineGrainMixedTest {
    private int NUM_THREADS;
    int[] values;
   

    //MAIN
    public static void main(String[] args) {	
        int numOperations = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
    	int maxValue = Integer.parseInt(args[2]);

    	FineGrainMixedTest test = new FineGrainMixedTest(numOperations, threads, maxValue);
    		System.out.println("=======================");
    		System.out.println("Our function's total throughput: " + test.testSkipListMixed(false) + " ms");
            System.out.println("Java function's total throughput: " + test.testSkipListMixed(true) + " ms");
            System.out.println("=======================");
    }
    
    public FineGrainMixedTest(int size, int threads, int maxValue) {
        initArrayWithValues(size, maxValue);
        NUM_THREADS = threads;
    }
    

    public long testSkipListMixed(boolean useJava) {
        ConcurrentSkipListMap<Integer,String> map = new ConcurrentSkipListMap<Integer, String>();
        FineGrainedSkipList<Integer> list = new FineGrainedSkipList<Integer>();
        ExecutorService es = Executors.newCachedThreadPool();
        LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
        int start = 0;
        int valuesPerThread = values.length/NUM_THREADS;
        Random r = new Random();
        while (start < values.length) {
            int end = start + valuesPerThread;
            if (end > values.length - 1) {
                end = values.length - 1;
            }

            if(useJava) {
                futures.add(es.submit(new SkipListMixedTimer(map, null, values, start, end, r)));
            } else {
                futures.add(es.submit(new SkipListMixedTimer(null, list, values, start, end, r)));
            }

            start = end + 1;
        }

        long sum = 0;
        for (Future<Long[]> future : futures) {
            try {
                sum += sum(future.get());
            } catch (InterruptedException | ExecutionException e) {
                //do nothing
            }
        }
        es.shutdown();
        return sum/1000000; //return in ms
    }


    /**
     * Internal/helper functions
     */

    private long sum(Long[] arr) {
        long sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }
    private void initArrayWithValues(int size, int maxValue) {
        values = new int[size];
        Random r = new Random();
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt(maxValue);
        }
    }



    /**
     * Inner runnable classes used to access skip lists concurrently.
     */
    
        public enum Function {
            ADD, CONTAINS, REMOVE
        }

    class SkipListMixedTimer implements Callable<Long[]> {
        ConcurrentSkipListMap<Integer, String> javaSkipList;
        FineGrainedSkipList<Integer> ourSkipList;
        
        int[] values;
        int start;
        int end;
        Random rand;


        SkipListMixedTimer(ConcurrentSkipListMap<Integer, String> list1, FineGrainedSkipList<Integer> list2, 
                int[] values, int start, int end, Random rand) {
            javaSkipList = list1;
            ourSkipList = list2;
            this.values = values;
            this.start = start;
            this.end = end;
            this.rand = rand;
        }

        Function getFunction() {
            int r = rand.nextInt(10);
               
            if (r == 0 || r == 1) { //20% add
                return Function.ADD;
            } else if (r == 2) {  //10% remove
                return Function.REMOVE;
            } else {
                return Function.CONTAINS;
            }
        }

        public Long[] call() { 
            Long[] results = new Long[end - start + 1];
            int index = 0; 
            long mstart, mend;
            for (int i = start; i <= end; i++) {
                Function action = getFunction();
                if (action == Function.ADD) {
                    if(javaSkipList != null) {
                	    mstart = System.nanoTime();
                        javaSkipList.put(values[i], "");
                        mend = System.nanoTime();
                    } else {
                	    mstart = System.nanoTime();
                        ourSkipList.add(values[i]);
                        mend = System.nanoTime();
                    }
                } else if (action == Function.REMOVE) {
				    if(javaSkipList!=null){
					    mstart = System.nanoTime();
					    javaSkipList.remove(values[i]);
					    mend = System.nanoTime();
				    } else {
					    mstart = System.nanoTime();
					    ourSkipList.remove(values[i]);
					    mend = System.nanoTime();
                    }
				} else {
			    	if(javaSkipList!=null){
					    mstart = System.nanoTime();
					    javaSkipList.containsValue(values[i]);
					    mend = System.nanoTime();
				    } else {
					    mstart = System.nanoTime();
					    ourSkipList.contains(values[i]);
					    mend = System.nanoTime();
				    }
                }
                results[index++] = mend - mstart;
            }
            return results;
        }
    }

}
