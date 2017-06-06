package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.repository.DictValueRepository;
import com.gofobao.framework.system.service.DictValueServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Max on 17/6/6.
 */
@Service
public class DictValueServcieImpl implements DictValueServcie{
    @Autowired
    DictValueRepository dictValueRepository ;

    @Override
    public DictValue findTopByItemIdAndValue02(Long itemId, String bankName) {
         return dictValueRepository.findTopByItemIdAndValue02AndDel(itemId, bankName, 0) ;
    }
}
