package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.repository.DictValueRepository;
import com.gofobao.framework.system.service.DictValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Max on 17/6/6.
 */
@Service
public class DictValueServiceImpl implements DictValueService {
    @Autowired
    DictValueRepository dictValueRepository ;

    @Override
    public DictValue findTopByItemIdAndValue02(Long itemId, String bankName) {
         return dictValueRepository.findTopByItemIdAndValue02AndDel(itemId, bankName, 0) ;
    }

    @Override
    public DictValue findTopByItemIdAndValue01(Long itemId, String bankName) {
        return dictValueRepository.findTopByItemIdAndValue01AndDel(itemId, bankName, 0) ;
    }

    public void save(DictValue dictValue){
        dictValueRepository.save(dictValue);
    }

    @Override
    public List<DictValue> findByItemId(Long id) {
        List<DictValue> dictValues = dictValueRepository.findByItemId(id);
        Optional< List<DictValue>> res = Optional.ofNullable(dictValues) ;
        return res.orElse(Collections.EMPTY_LIST) ;
    }
}
