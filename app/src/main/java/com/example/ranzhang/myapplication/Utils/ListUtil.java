package com.example.ranzhang.myapplication.Utils;

import java.util.List;

/**
 * Created by Ran on 9/17/2015.
 */
public class ListUtil {

    public static<L extends Comparable<L>> int findMin(int start, int end, List<L> list, L min) {
        if (list == null || start > end) {
            return -1;
        }

        int position = -1;
        for (int i = start; i <= end && i < list.size(); i++) {
            if (min.compareTo(list.get(i)) > 0) {
                min = list.get(i);
                position = i;
            }
        }
        return position;
    }
}
