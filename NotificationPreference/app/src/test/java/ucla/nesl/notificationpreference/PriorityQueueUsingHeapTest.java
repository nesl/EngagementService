package ucla.nesl.notificationpreference;

import org.junit.Test;

import java.util.Comparator;

import ucla.nesl.notificationpreference.utils.PriorityQueueUsingHeap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by timestring on 6/12/18.
 */

public class PriorityQueueUsingHeapTest {

    @Test
    public void queueOperation_isCorrect() throws Exception {
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Long.compare(a, b);
            }
        };

        PriorityQueueUsingHeap queue = new PriorityQueueUsingHeap(comparator);
        assertNull(queue.peek());
        assertNull(queue.poll());

        queue.offer(3);
        queue.offer(5);
        queue.offer(1);
        assertEquals(queue.peek(), 5);
        assertEquals(queue.size(), 3);

        assertEquals(queue.poll(), 5);
        assertEquals(queue.size(), 2);

        assertEquals(queue.poll(), 3);
        assertEquals(queue.size(), 1);

        assertEquals(queue.poll(), 1);
        assertEquals(queue.size(), 0);

        assertNull(queue.poll());

        queue.offer(3);
        queue.offer(5);
        queue.offer(6);
        assertEquals(queue.peek(), 6);
        assertEquals(queue.size(), 3);

        queue.offer(1);
        queue.offer(9);
        queue.offer(4);
        assertEquals(queue.peek(), 9);
        assertEquals(queue.size(), 6);

        queue.offer(8);
        queue.offer(2);
        queue.offer(7);
        assertEquals(queue.peek(), 9);
        assertEquals(queue.size(), 9);

        assertEquals(queue.poll(), 9);
        assertEquals(queue.size(), 8);

        assertEquals(queue.poll(), 8);
        assertEquals(queue.size(), 7);

        assertEquals(queue.poll(), 7);
        assertEquals(queue.size(), 6);

        assertEquals(queue.poll(), 6);
        assertEquals(queue.size(), 5);

        assertEquals(queue.poll(), 5);
        assertEquals(queue.size(), 4);

        assertEquals(queue.poll(), 4);
        assertEquals(queue.size(), 3);

        assertEquals(queue.poll(), 3);
        assertEquals(queue.size(), 2);

        assertEquals(queue.poll(), 2);
        assertEquals(queue.size(), 1);

        assertEquals(queue.poll(), 1);
        assertEquals(queue.size(), 0);

        assertNull(queue.poll());
        assertNull(queue.poll());
        assertNull(queue.poll());
    }
}
