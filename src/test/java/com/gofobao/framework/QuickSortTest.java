package com.gofobao.framework;

import java.util.Arrays;

/**
 * Created by Zeke on 2017/7/3.
 */
public class QuickSortTest {

    public static int partition(Integer[] array, int i, int j) {
        //固定的切分方式
        int key = array[i];
        while (i < j) {
            while (array[j] >= key && j > i) {//从后半部分向前扫描
                j--;
            }
            array[i] = array[j];
            while (array[i] <= key && j > i) {//从前半部分向后扫描
                i++;
            }
            array[j] = array[i];
        }
        array[j] = key;
        return j;
    }

    public static void sort(Integer[] array, int i, int j) {
        if (i >= j) {
            return;
        }
        int index = partition(array, i, j);
        sort(array, i, index - 1);
        sort(array, index + 1, j);
    }


    public static void main(String[] args) {
        Integer[] arr = (Integer[]) (Arrays.asList(6, 1, 2, 7, 9, 3, 4, 5, 10, 8).toArray());
        sort(arr, 0, arr.length - 1);
        System.out.println(arr);
    }

}
