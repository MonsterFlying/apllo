package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.system.biz.SuggestBiz;
import com.gofobao.framework.system.entity.Suggest;
import com.gofobao.framework.system.service.SuggestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2017/8/10.
 */
@Service
public class SuggestBizImpl implements SuggestBiz {

    @Autowired
    private SuggestService suggestService;

    @Override
    public ResponseEntity<Boolean> save(Suggest suggest) {
        return ResponseEntity.ok(suggestService.save(suggest));
    }
}
