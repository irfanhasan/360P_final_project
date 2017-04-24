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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LockFreePerformanceTests {
    private int NUM_THREADS;
    int[] values;
   

    //MAIN
    public static void main(String[] args) {	
        String str = args[0];
        int numOperations = Integer.parseInt(args[1]);
        int threads = Integer.parseInt(args[2]);
    	int maxValue = Integer.parseInt(args[3]);

        long seed = new Random().nextLong();
    	LockFreePerformanceTests test = new LockFreePerformanceTests(numOperations, threads, maxValue);
    	if(str.equals("a")){
            String output = "=======================\n";
            output += "numOperations: " + numOperations + " || threads: " + threads + " || maxValue: " + maxValue + "\n";
            output += "Our function add on avg: " + test.testSkipListAdd(false) + " ns\n";
            output += "Java function add on avg: " + test.testSkipListAdd(true) + " ns\n";
            output = "=======================\n\n";
            
            String filename = "FineGrained_ADD.txt";
            logOutput(output, filename);
    	}else if(str.equals("c")){
            String output = "=======================\n";
            output += "numOperations: " + numOperations + " || threads: " + threads + " || maxValue: " + maxValue + "\n";
            output += "Our function contains on avg: " + test.testSkipListContains(false) + " ns\n";
            output += "Java function contains on avg: " + test.testSkipListContains(true) + " ns\n";
            output = "=======================\n\n";
            
            String filename = "FineGrained_CONTAINS.txt";
            logOutput(output, filename);
    	}else if(str.equals("r")){
            String output = "=======================\n";
            output += "numOperations: " + numOperations + " || threads: " + threads + " || maxValue: " + maxValue + "\n";
            output += "Our function remove on avg: " + test.testSkipListRemove(false) + " ns\n";
            output += "Java function remove on avg: " + test.testSkipListRemove(true) + " ns\n";
            output = "=======================\n\n";
            
            String filename = "FineGrained_REMOVE.txt";
            logOutput(output, filename);
    	} else if (str.equals("m")) {
            String output = "=======================\n";
            output += "numOperations: " + numOperations + " || threads: " + threads + " || maxValue: " + maxValue + "\n";
            output += "Our function's mixed performance on avg: " + test.testSkipListMixed(false, seed) + " ns\n";
            output += "Java function's mixed performance on avg: " + test.testSkipListMixed(true, seed) + " ns\n";
            output = "=======================\n\n";
            
            String filename = "FineGrained_MIXED.txt";
            logOutput(output, filename);
        } else{
    		System.out.println("Invalid Input, System exiting.");
    	}
    }
    
    private static void logOutput(String output, String filename) {
        System.out.print(output);
        try {
            File yourFile = new File(filename);
            yourFile.createNewFile();
            Files.write(Paths.get(filename), output.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            System.exit(-1);
        }
    }
    
    public LockFreePerformanceTests(int size, int threads, int maxValue) {
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
            int r = rand.nextInt(9);
               
            if (r < 3) { //33% Add
                return Function.ADD;
            } else if (r < 6) {  //33% Remove
                return Function.REMOVE;
            } else { // 33% Contains
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
