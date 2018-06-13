package ucla.nesl.notificationpreference.utils;

/*
 * The `PriorityQueueUsingHeap` class is copied from wihoho's GitHub repository. Here is the link:
 * https://gist.github.com/wihoho/5651772
 *
 * I made the following changes:
 *     (1) I made the class be able to accept a comparator, and
 *     (2) I converted the array into an `ArrayList`
 *
 * Note it is a max priority queue, that is, plugging the native comparator makes it a max priority
 * queue.
 */

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Gong Li
 * Date: 5/26/13
 * Time: 12:23 PM
 * Implement a priority queue using a heap
 * The heap is implemented using an array indexed from 1
 */
public class PriorityQueueUsingHeap<T> {
    private ArrayList<T> arr;
    private int N;
    private Comparator<T> comparator;

    public PriorityQueueUsingHeap(Comparator<T> comp) {
        comparator = comp;

        // initialize arr and N
        clear();
    }

    public void offer(T t) {
        arr.add(t);
        N++;
        swim(N);
    }

    public T peek() {
        if (isEmpty())
            return null;
        return arr.get(1);
    }

    public T poll() {
        if (isEmpty())
            return null;

        T t = arr.get(1);
        exch(1, N);
        arr.remove(N);
        N--;
        sink(1);

        return t;
    }

    public int size() {
        return N;
    }

    public void clear() {
        arr = new ArrayList<>();
        arr.add(null);
        N = 0;
    }

    public void toArray(T[] a) {
        for (int i = 1; i <= N; i++)
            a[i-1] = arr.get(i);
    }

    //helper methods
    public String toString(){
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= N; i++)
            sb.append(arr.get(i).toString() + " ");
        return sb.toString();
    }

    private boolean isEmpty() {
        return N == 0;
    }

    private void swim(int k) {
        while (k > 1 && less(k / 2, k)) {
            exch(k / 2, k);
            k = k / 2;
        }
    }

    private void sink(int k) {
        while (2 * k <= N){
            int j = 2 * k;
            if (j < N && less(j, j + 1))
                j = j + 1;
            if (less(j, k))
                break;
            exch(k, j);
            k = j;
        }
    }

    private boolean less(int i, int j) {
        return comparator.compare(arr.get(i), arr.get(j)) < 0;
    }

    private void exch(int i, int j) {
        T temp = arr.get(i);
        arr.set(i, arr.get(j));
        arr.set(j, temp);
    }
}