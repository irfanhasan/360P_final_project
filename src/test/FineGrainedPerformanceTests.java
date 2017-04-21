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

public class FineGrainedPerformanceTests {
    private static final int NUM_THREADS = 20;
    int[] values;
   

    //MAIN
    public static void main(String[] args) {
        FineGrainedPerformanceTests test = new FineGrainedPerformanceTests(100);
        System.out.println("Our function add on avg: " + test.testSkipListAdd(false) + " ns");
        System.out.println("Java function add on avg: " + test.testSkipListAdd(true) + " ns");
    }


    public FineGrainedPerformanceTests(int size) {
        initArrayWithValues(size);
    }

    public long testSkipListAdd(boolean useJava) {
        ConcurrentSkipListMap<Integer,String> map = new ConcurrentSkipListMap<Integer, String>();
        FineGrainedSkipList<Integer> list = new FineGrainedSkipList<Integer>();
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
    private void initArrayWithValues(int size) {
        values = new int[size];
        Random r = new Random();
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt();
        }
    }



    /**
     * Inner runnable classes used to access skip lists concurrently.
     */

    class SkipListAddTimer implements Callable<Long[]> {
        ConcurrentSkipListMap<Integer, String> javaSkipList;
        FineGrainedSkipList<Integer> ourSkipList;
        
        int[] values;
        int start;
        int end;

        SkipListAddTimer(ConcurrentSkipListMap<Integer, String> list1, FineGrainedSkipList<Integer> list2, 
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
            for (int i = start; i <= end; i++) {
                long start = System.nanoTime();
                if(javaSkipList != null) { 
                    javaSkipList.put(values[i], "");
                } else {
                    ourSkipList.add(values[i]);
                }
                long end = System.nanoTime();
                results[index++] = end - start;
            }
            return results;
        }

    }
}
