package com.gofobao.framework.tender.service.impl;

import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderServiceImpl implements AutoTenderService{

    @Autowired
    private AutoTenderRepository autoTenderRepository;

    public boolean insert(AutoTender autoTender){
        if (ObjectUtils.isEmpty(autoTender)){
            return false;
        }
        autoTender.setId(null);
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

    public boolean update(AutoTender autoTender){
        if (ObjectUtils.isEmpty(autoTender) || ObjectUtils.isEmpty(autoTender.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

}
