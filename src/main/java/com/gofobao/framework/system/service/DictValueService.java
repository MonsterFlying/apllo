package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.DictValue;

import java.util.List;

/**
 * Created by Max on 17/6/6.
 */
public interface DictValueService {


    DictValue findTopByItemIdAndValue02(Long itemId, String bankName);

    DictValue findTopByItemIdAndValue01(Long itemId, String bankName);

    void save(DictValue dictValue);

    List<DictValue> findByItemId(Long id);

}
