package com.trackify.util;

import com.trackify.model.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortUtil {

    public static <T> void mergeSort(List<T> list, Comparator<T> c) {
        if (list == null || list.size() < 2)
            return;
        mergeSort(list, 0, list.size() - 1, c);
    }

    private static <T> void mergeSort(List<T> list, int left, int right, Comparator<T> c) {
        if (left >= right)
            return;
        int mid = left + (right - left) / 2;
        mergeSort(list, left, mid, c);
        mergeSort(list, mid + 1, right, c);
        merge(list, left, mid, right, c);
    }

    private static <T> void merge(List<T> list, int left, int mid, int right, Comparator<T> c) {
        List<T> temp = new ArrayList<>(right - left + 1);
        int i = left;
        int j = mid + 1;

        while (i <= mid && j <= right) {
            
            if (c.compare(list.get(i), list.get(j)) <= 0)
                temp.add(list.get(i++));
            else
                temp.add(list.get(j++));
        }
        while (i <= mid)
            temp.add(list.get(i++));
        while (j <= right)
            temp.add(list.get(j++));

        for (int k = 0; k < temp.size(); k++)
            list.set(left + k, temp.get(k));
    }

    public static Comparator<Transaction> byDate(boolean ascending) {
        return (a, b) -> {
            int result = a.getDate().compareTo(b.getDate());
            return ascending ? result : -result;
        };
    }

    public static Comparator<Transaction> byAmount(boolean ascending) {
        return (a, b) -> {
            int result = Double.compare(a.getAmount(), b.getAmount());
            return ascending ? result : -result;
        };
    }
}
