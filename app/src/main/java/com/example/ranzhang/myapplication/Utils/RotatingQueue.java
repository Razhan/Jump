package com.example.ranzhang.myapplication.Utils;

/**
 * Created by ran.zhang on 9/17/15.
 */

import java.util.LinkedList;
import java.util.List;

public class RotatingQueue<T extends Comparable<T>> {

    public RotatingQueue(int capacity) {
        size = capacity;
        queue = new LinkedList<T>();
    }

    public void insertElement(T element) {

        if(queue.size() >= size ) {
            queue.remove(0);
        }
        queue.add(element);
    }

    public T getElement(int index) {
        return queue.get(index);
    }

    public int size() {
        return size;
    }

    public boolean existPeak() {
        if (queue == null || queue.size() < size) {
            return false;
        }

        int mid = size / 2;

        if ((mid - 1) < 0 || (mid + 1) >= size) {
            return false;
        }

        if (queue.get(mid).compareTo(queue.get(mid - 1)) > 0 && queue.get(mid).compareTo(queue.get(mid + 1)) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        queue.clear();
    }

    private List<T> queue;
    private int size;
}
