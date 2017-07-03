package com.gofobao.framework;

import java.util.Arrays;

/**
 * Created by Zeke on 2017/7/3.
 */
public class QuickSortTest {
    public static void sort(Integer[] arr) {
        int i, j, t, temp;
        i = 0;
        j = arr.length - 1;
        temp = arr[0];
        while (i != j) {
            while (arr[j] >= temp && i < j) {
                j--;
            }
            while (arr[i] < temp && i < j) {
                i++;
            }

            if (i < j) {
                t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }
        }
    }

    public static void main(String[] args) {
        Integer[] arr =(Integer[])(Arrays.asList(6,1,2,7,4,5,9,3).toArray());
        sort(arr);
        System.out.println(arr);
    }

}
