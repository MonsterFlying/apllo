package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
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
    public ResponseEntity<VoBaseResp> save(Suggest suggest) {
        try {
            suggestService.save(suggest);
            return ResponseEntity.ok(VoBaseResp.ok("反馈成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "反馈异常"));
        }
    }
}
