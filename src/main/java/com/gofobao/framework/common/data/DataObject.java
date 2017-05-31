package com.gofobao.framework.common.data;

import lombok.Data;

/**
 * Created by Zeke on 2017/5/31.
 */
@Data
public class DataObject<T extends Comparable<T>> {
    private T val;

    public DataObject(T val) {
        this.val = val;
    }
}
