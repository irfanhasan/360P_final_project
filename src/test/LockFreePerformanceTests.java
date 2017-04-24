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

public class LockFreeSkipPTest {
    private int NUM_THREADS;
    int[] values;
   

    //MAIN
    public static void main(String[] args) {	
        String str = args[0];
        int numOperations = Integer.parseInt(args[1]);
        int threads = Integer.parseInt(args[2]);
    	int maxValue = Integer.parseInt(args[3]);

        long seed = new Random().nextLong();
    	LockFreeSkipPTest test = new LockFreeSkipPTest(numOperations, threads, maxValue);
    	if(str.equals("a")){
    		System.out.println("=======================");
    		System.out.println("Our function add on avg: " + test.testSkipListAdd(false) + " ns");
            System.out.println("Java function add on avg: " + test.testSkipListAdd(true) + " ns");
            System.out.println("=======================");
    	}else if(str.equals("c")){
    		System.out.println("=======================");
    		System.out.println("Our function contains on avg: " + test.testSkipListContains(false) + " ns");
            System.out.println("Java function contains on avg: " + test.testSkipListContains(true) + " ns");
            System.out.println("=======================");
    	}else if(str.equals("r")){
    		System.out.println("=======================");
    		System.out.println("Our function remove on avg: " + test.testSkipListRemove(false) + " ns");
            System.out.println("Java function remove on avg: " + test.testSkipListRemove(true) + " ns");
            System.out.println("=======================");
    	} else if (str.equals("m")) {
    		System.out.println("=======================");
    		System.out.println("Our function's total throughput: " + test.testSkipListMixed(false, seed) + " ms");
            System.out.println("Java function's total throughput: " + test.testSkipListMixed(true, seed) + " ms");
            System.out.println("=======================");
        } else{
    		System.out.println("Invalid Input, System exiting.");
    	}
    }
    
    public LockFreeSkipPTest(int size, int threads, int maxValue) {
        initArrayWithValues(size, maxValue);
        NUM_THREADS = threads;
    }
    

    public long testSkipListMixed(boolean useJava, long seed) {
        ConcurrentSkipListMap<Integer,String> map = new ConcurrentSkipListMap<Integer, String>();
        LockFreeSkipList<Integer> list = new LockFreeSkipList<Integer>();
        ExecutorService es = Executors.newCachedThreadPool();
        LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
        int start = 0;
        int valuesPerThread = values.length/NUM_THREADS;
        Random r = new Random(seed);
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
        return sum/1000000;
    }

    public long testSkipListAdd(boolean useJava) {
        ConcurrentSkipListMap<Integer,String> map = new ConcurrentSkipListMap<Integer, String>();
        LockFreeSkipList<Integer> list = new LockFreeSkipList<Integer>();
        ExecutorService es = Executors.newCachedThreadPool();
        LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
        int start = 0;
        int valuesPerThread = values.length/NUM_THREADS;
        while (start < values.length) {
            int end = start + valuesPerThread;
            if (end > values.length - 1) {
                end = values.length - 1;
            }

            if(useJava) {
                futures.add(es.submit(new SkipListAddTimer(map, null, values, start, end)));
            } else {
                futures.add(es.submit(new SkipListAddTimer(null, list, values, start, end)));
            }

            start = end + 1;
        }

        long sum = 0;
        for (Future<Long[]> future : futures) {
            try {
                sum += sum(future.get());
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        es.shutdown();
        return sum/ (long) values.length;
    }
    
    public long testSkipListRemove(boolean useJava){
    	ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<Integer, String>();
    	LockFreeSkipList<Integer> list = new LockFreeSkipList<Integer>();
    	for(int v : values){
    		map.put(v, "");
    		list.add(v);
    	}
    	ExecutorService es = Executors.newCachedThreadPool();
    	LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
    	for(int i=0; i<NUM_THREADS; i++){
    		if(useJava){
    			futures.add(es.submit(new SkipListRemoveTimer(map, null, i+1)));
    		}else{
    			futures.add(es.submit(new SkipListRemoveTimer(null, list, i+1)));
    		}
    	}
    	
    	long sum=0;
    	for(Future<Long[]> future : futures){
    		try{
    			sum += sum(future.get());
    		}catch(InterruptedException | ExecutionException e){}
    	}
    	es.shutdown();
    	return sum/(long) values.length;
    }
    
    public long testSkipListContains(boolean useJava){
    	ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<Integer, String>();
    	LockFreeSkipList<Integer> list = new LockFreeSkipList<Integer>();
    	for(int v : values){
    		map.put(v, "");
    		list.add(v);
    	}
    	ExecutorService es = Executors.newCachedThreadPool();
    	LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
    	for(int i=0; i<NUM_THREADS; i++){
    		if(useJava){
    			futures.add(es.submit(new SkipListContainsTimer(map, null, i+1)));
    		}else{
    			futures.add(es.submit(new SkipListContainsTimer(null, list, i+1)));
    		}
    	}
    	
    	long sum=0;
    	for(Future<Long[]> future : futures){
    		try{
    			sum += sum(future.get());
    		}catch(InterruptedException | ExecutionException e){}
    	}
    	es.shutdown();
    	return sum/(long) values.length;
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
        LockFreeSkipList<Integer> ourSkipList;
        
        int[] values;
        int start;
        int end;
        Random rand;


        SkipListMixedTimer(ConcurrentSkipListMap<Integer, String> list1, LockFreeSkipList<Integer> list2, 
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
					    javaSkipList.containsKey(values[i]);
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

    class SkipListAddTimer implements Callable<Long[]> {
        ConcurrentSkipListMap<Integer, String> javaSkipList;
        LockFreeSkipList<Integer> ourSkipList;
        
        int[] values;
        int start;
        int end;

        SkipListAddTimer(ConcurrentSkipListMap<Integer, String> list1, LockFreeSkipList<Integer> list2, 
                int[] values, int start, int end) {
            javaSkipList = list1;
            ourSkipList = list2;
            this.values = values;
            this.start = start;
            this.end = end;
        }

        public Long[] call() { 
            Long[] results = new Long[end - start + 1];
            int index = 0; 
            long mstart, mend;
            for (int i = start; i <= end; i++) {
                if(javaSkipList != null) {
                	mstart = System.nanoTime();
                    javaSkipList.put(values[i], "");
                    mend = System.nanoTime();
                } else {
                	mstart = System.nanoTime();
                    ourSkipList.add(values[i]);
                    mend = System.nanoTime();
                }
                results[index++] = mend - mstart;
            }
            return results;
        }
    }
    
    class SkipListContainsTimer implements Callable<Long[]>{
    	ConcurrentSkipListMap<Integer, String> javaSkipList;
    	LockFreeSkipList<Integer> ourSkipList;
    	
    	int multiplier;
    	
    	SkipListContainsTimer(ConcurrentSkipListMap<Integer, String> list1, LockFreeSkipList<Integer> list2, int m){
    		javaSkipList = list1;
    		ourSkipList = list2;
    		multiplier = m;
    	}
    
		public Long[] call() throws Exception {
			Long[] results = new Long[values.length];
			long start, end;
			for(int i=0; i<values.length; i++){
				if(javaSkipList!=null){
					start = System.nanoTime();
					javaSkipList.containsKey(i*multiplier);
					end = System.nanoTime();
				}else{
					start = System.nanoTime();
					ourSkipList.contains(i*multiplier);
					end = System.nanoTime();
				}
				results[i] = end-start;
			}
			return results;
		}
	}
    
    class SkipListRemoveTimer implements Callable<Long[]>{
    	ConcurrentSkipListMap<Integer, String> javaSkipList;
    	LockFreeSkipList<Integer> ourSkipList;
    	
    	int multiplier;
    	
    	SkipListRemoveTimer(ConcurrentSkipListMap<Integer, String> list1, LockFreeSkipList<Integer> list2, int m){
    		javaSkipList = list1;
    		ourSkipList = list2;
    		multiplier = m;
    	}
    
		public Long[] call() throws Exception {
			Long[] results = new Long[values.length];
			long start, end;
			for(int i=0; i<values.length; i++){
				if(javaSkipList!=null){
					start = System.nanoTime();
					javaSkipList.remove(i*multiplier);
					end = System.nanoTime();
				}else{
					start = System.nanoTime();
					ourSkipList.remove(i*multiplier);
					end = System.nanoTime();
				}
				results[i] = end-start;
			}
			return results;
		}
	}
}
