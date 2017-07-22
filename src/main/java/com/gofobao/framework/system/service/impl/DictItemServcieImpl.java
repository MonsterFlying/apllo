package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.repository.DictItemRepository;
import com.gofobao.framework.system.service.DictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Max on 17/6/6.
 */
@Service
public class DictItemServcieImpl implements DictItemService {
    @Autowired
    DictItemRepository dictItemRepository ;


    @Override
    public DictItem findTopByAliasCodeAndDel(String aliasCode, int del) {

        return dictItemRepository.findTopByAliasCodeAndDel(aliasCode, del) ;
    }
}


