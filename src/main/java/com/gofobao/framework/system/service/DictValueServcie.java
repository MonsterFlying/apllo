package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.DictValue;

/**
 * Created by Max on 17/6/6.
 */
public interface DictValueServcie {


    DictValue findTopByItemIdAndValue02(Long itemId, String bankName);

    DictValue findTopByItemIdAndValue01(Long itemId, String bankName);

    void save(DictValue dictValue);
}
