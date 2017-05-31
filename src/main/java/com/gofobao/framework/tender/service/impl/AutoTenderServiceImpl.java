package com.gofobao.framework.tender.service.impl;

import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderServiceImpl implements AutoTenderService {

    @Autowired
    private AutoTenderRepository autoTenderRepository;

    public boolean insert(AutoTender autoTender) {
        if (ObjectUtils.isEmpty(autoTender)) {
            return false;
        }
        autoTender.setId(null);
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

    public boolean updateById(AutoTender autoTender) {
        if (ObjectUtils.isEmpty(autoTender) || ObjectUtils.isEmpty(autoTender.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

    public boolean updateByExample(AutoTender autoTender, Example<AutoTender> example) {
        if (ObjectUtils.isEmpty(autoTender) || ObjectUtils.isEmpty(example.getProbe())) {
            return false;
        }

        List<AutoTender> autoTenderList = autoTenderRepository.findAll(example);
        List<AutoTender> updAutoTenders = new ArrayList<>();

        Optional<List<AutoTender>> autoTenderOptions = Optional.ofNullable(autoTenderList);
        autoTenderOptions.ifPresent(o -> o.forEach(temp -> {
            BeanHelper.copyParamter(autoTender, temp, true);
            updAutoTenders.add(temp);//更新对象
        }));
        return CollectionUtils.isEmpty(autoTenderRepository.save(updAutoTenders));
    }
}
