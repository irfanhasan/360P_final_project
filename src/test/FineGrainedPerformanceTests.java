/**
 * A class to test the performance of our Skip List against the one found in the Java libraries.
 */

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import java.util.LinkedList;

import java.util.Random;

public class FineGrainedPerformanceTests {
    private static final int NUM_THREADS = 20;
    int[] values;
    
    public FineGrainedPerformanceTests(int size) {
        initArrayWithValues(size);
    }

    public long testJavaSkipListAdd() {
        ConcurrentSkipListMap<Integer,String> map = new ConcurrentSkipListMap<Integer, String>();
        ExecutorService es = Executors.newCachedThreadPool();
        LinkedList<Future<Long[]>> futures = new LinkedList<Future<Long[]>>();
        return 0;
    }

    /**
     * Internal/helper functions
     */
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
                long start = System.currentTimeMillis();
                if(javaSkipList != null) { 
                    javaSkipList.put(values[i], "");
                } else {
                    ourSkipList.add(values[i]);
                }
                long end = System.currentTimeMillis();
                results[index++] = end - start;
            }
            return results;
        }

    }
}
