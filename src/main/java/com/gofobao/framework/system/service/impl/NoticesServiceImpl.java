package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.repository.NoticesRepository;
import com.gofobao.framework.system.service.NoticesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Max on 17/6/5.
 */
@Service
public class NoticesServiceImpl implements NoticesService {

    @Autowired
    NoticesRepository noticesRepository;

    @Override
    public void save(Notices notices) {
        noticesRepository.save(notices) ;
    }
}
