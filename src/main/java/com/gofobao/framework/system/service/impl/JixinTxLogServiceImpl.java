package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.JixinTxLog;
import com.gofobao.framework.system.repository.JixinTxLogRepository;
import com.gofobao.framework.system.service.JixinTxLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/7/13 0013.
 */
@Service
public class JixinTxLogServiceImpl implements JixinTxLogService {

    @Autowired
    JixinTxLogRepository jixinTxLogRepository ;

    @Override
    public void save(JixinTxLog jixinTxLog) {
        jixinTxLogRepository.save(jixinTxLog) ;
    }
}
