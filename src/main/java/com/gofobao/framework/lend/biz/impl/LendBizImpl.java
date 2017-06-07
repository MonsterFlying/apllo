package com.gofobao.framework.lend.biz.impl;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.lend.biz.LendBiz;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.lend.vo.response.VoViewLend;
import com.gofobao.framework.lend.vo.response.VoViewLendListWarpRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Slf4j
@Service

public class LendBizImpl implements LendBiz {

    @Autowired
    private LendService lendService;

    @Override
    public ResponseEntity<VoViewLendListWarpRes> list(Page page) {
        try {
            VoViewLendListWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewLendListWarpRes.class);
            List<VoViewLend> lends = lendService.list(page);
            warpRes.setVoViewLends(lends);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("",e);
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewLendListWarpRes.class));
        }
    }
}
