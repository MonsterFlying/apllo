package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Vip;
import com.gofobao.framework.member.repository.VipRepository;
import com.gofobao.framework.member.service.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Service
public class VipServiceImpl implements VipService {

    @Autowired
    VipRepository vipRepository;


    @Override
    public Vip findTopByUserIdAndStatus(Long userId, int status) {
        Date nowDate=new Date();
        return vipRepository.findTopByUserIdAndStatusIsAndExpireAtGreaterThan(userId, status,nowDate);
    }

    @Override
    public Boolean save(Vip vip) {
        try {
            vipRepository.save(vip);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
