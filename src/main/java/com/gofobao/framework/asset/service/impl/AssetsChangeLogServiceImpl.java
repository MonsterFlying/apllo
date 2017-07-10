package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.AssetsChangeLog;
import com.gofobao.framework.asset.repository.AssetsChangeLogRepository;
import com.gofobao.framework.asset.service.AssetsChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/7/8 0008.
 */
@Service
public class AssetsChangeLogServiceImpl implements AssetsChangeLogService {

    @Autowired
    AssetsChangeLogRepository assetsChangeLogRepository ;

    @Override
    public AssetsChangeLog save(AssetsChangeLog changeLog) {
        return assetsChangeLogRepository.save(changeLog) ;
    }
}
