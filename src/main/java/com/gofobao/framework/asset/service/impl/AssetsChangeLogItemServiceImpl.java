package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.AssetsChangeLogItem;
import com.gofobao.framework.asset.repository.AssetsChangeLogItemRepository;
import com.gofobao.framework.asset.service.AssetsChangeLogItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/7/8 0008.
 */
@Service
public class AssetsChangeLogItemServiceImpl implements AssetsChangeLogItemService {

    @Autowired
    AssetsChangeLogItemRepository assetsChangeLogItemRepository ;

    @Override
    public void save(AssetsChangeLogItem changeLogItem) {
        assetsChangeLogItemRepository.save(changeLogItem) ;
    }
}
