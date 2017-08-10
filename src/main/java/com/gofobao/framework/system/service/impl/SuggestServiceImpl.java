package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Suggest;
import com.gofobao.framework.system.repository.SuggestRepository;
import com.gofobao.framework.system.service.SuggestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by admin on 2017/8/10.
 */
@Component
public class SuggestServiceImpl implements SuggestService {

    @Autowired
    private SuggestRepository suggestRepository;

    @Override
    public Boolean save(Suggest suggest) {
        try {
            suggestRepository.save(suggest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
