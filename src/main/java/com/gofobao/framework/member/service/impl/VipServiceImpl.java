package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Vip;
import com.gofobao.framework.member.repository.VipRepository;
import com.gofobao.framework.member.service.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Service
public class VipServiceImpl implements VipService {

    @Autowired
    VipRepository vipRepository;


    @Override
    public Vip findTopByUserIdAndStatus(Long userId, int status) {
        return vipRepository.findTopByUserIdAndStatus(userId, status);
    }
}
