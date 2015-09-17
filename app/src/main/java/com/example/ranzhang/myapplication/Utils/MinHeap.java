package com.example.ranzhang.myapplication.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Created by Ran on 9/17/2015.
 */
public class MinHeap<E extends Comparable<? super E>> {
    private final String TAG = MinHeap.class.getSimpleName();
    private ArrayList<E> values;
    private int size;

    public MinHeap(int capacity) {
        this.values = new ArrayList<E>();
        this.size = capacity;
    }

    public E minValue() {
        if (this.values.size() == 0) {
            throw new NoSuchElementException();
        }
        return this.values.get(0);
    }

    public void add(E newValue) {
        if (values.size() < size) {
            addElement(newValue);
        } else {
            if (minValue().compareTo(newValue) <= 0) {
                remove();
                addElement(newValue);
            }
        }
    }

    private void addElement(E newValue) {
        values.add(newValue);
        int pos = this.values.size()-1;

        while (pos > 0) {
            if (newValue.compareTo(this.values.get((pos-1)/2)) < 0) {
                values.set(pos, this.values.get((pos-1)/2));
                pos = (pos-1)/2;
            }
            else {
                break;
            }
        }
        this.values.set(pos, newValue);
    }

    public void remove() {
        E newValue = this.values.remove(this.values.size()-1);
        int pos = 0;

        if (this.values.size() > 0) {
            while (2*pos+1 < this.values.size()) {
                int minChild = 2*pos+1;
                if (2*pos+2 < this.values.size() &&
                        this.values.get(2*pos+2).compareTo(this.values.get(2*pos+1)) < 0) {
                    minChild = 2*pos+2;
                }

                if (newValue.compareTo(this.values.get(minChild)) > 0) {
                    this.values.set(pos, this.values.get(minChild));
                    pos = minChild;
                }
                else {
                    break;
                }
            }
            this.values.set(pos, newValue);
        }
    }

    public String toString() {
        return values.toString();
    }

    public int getSize() {
        return values.size();
    }

    public void clear() {
        values.clear();
    }

    public ArrayList<E> getValues() {
        return values;
    }
}