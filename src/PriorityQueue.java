package taskChainPlanner;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * A priority queue implementation using a linked list.
 * Requires a comparator to be passed in the constructor.
 */
public class PriorityQueue<T> {
    private LinkedList<T> list; // underlying container
    private Comparator<T> comparator; // comparator to use for ordering

    /**
     * Constructor for the priority queue.
     * @param comparator The comparator to use for ordering the elements.
     */
    public PriorityQueue(Comparator<T> comparator) {
        list = new LinkedList<>();
        this.comparator = comparator;
    }

    /**
     * Adds an element to the priority queue.
     *
     * This doesn't return a bool because elements
     * of the wrong type can't be added anyway,
     * the java compiler will catch that it's not of type T.
     *
     * @param input The element to add.
     */
    public void offer(T input) {
        int i = 0;
        // find the correct position to insert the element
        for (T element : list) {
            if (comparator.compare(input, element) < 0) {
                break;
            }
            i++;
        }
        // insert the element at the found position
        list.add(i, input);
    }

    /**
     * Returns the element at the front of the queue without removing it.
     * @return The element at the front of the queue.
     */
    public T peek() {
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    /**
     * Returns the element at the front of the queue and removes it.
     * @return The element at the front of the queue.
     */
    public T poll() {
        if (list.isEmpty()) {
            return null;
        }
        return list.removeFirst();
    }

    /**
     * Removes the specified element from the queue.
     * @param input The element to remove.
     * @return true if the element was removed successfully.
     */
    public boolean remove(T input) {
        return list.remove(input);
    }

    /**
     * Returns the number of elements in the queue.
     * @return The number of elements in the queue.
     */
    public int size() {
        return list.size();
    }

    /**
     * Checks if the queue is empty.
     * @return true if the queue is empty.
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * String representation of the priority queue.
     * @return The string representation of the priority queue.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T element : list) {
            sb.append(element).append("\n");
        }
        return sb.toString();
    }
}
