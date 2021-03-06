package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.service.VirtualService;
import com.gofobao.framework.award.vo.request.VoVirtualReq;
import com.gofobao.framework.award.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/8.
 */
@Slf4j
@Service
public class VirtualBizImpl implements VirtualBiz {

    @Autowired
    private VirtualService virtualService;

    @Override
    public ResponseEntity<VoViewVirtualStatisticsWarpRes> query(Long userId) {
        try {
            VirtualStatistics virtualStatistics = virtualService.statistics(userId);
            VoViewVirtualStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewVirtualStatisticsWarpRes.class);
            warpRes.setVirtualStatistics(virtualStatistics);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            log.info("VirtualBiz query fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewVirtualStatisticsWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewVirtualTenderResWarpRes> userTenderList(Long userId) {
        try {
            List<VirtualTenderRes> resList = virtualService.userTenderList(userId);
            VoViewVirtualTenderResWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewVirtualTenderResWarpRes.class);
            warpRes.setResList(resList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            log.info("VirtualBiz userTenderList fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewVirtualTenderResWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewVirtualBorrowResWarpRes> list() {
        try {
            List<VirtualBorrowRes> resList = virtualService.list();
            VoViewVirtualBorrowResWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewVirtualBorrowResWarpRes.class);
            warpRes.setResList(resList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            log.info("VirtualBiz list fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewVirtualBorrowResWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoBaseResp> createTender(VoVirtualReq voVirtualReq) {
        try {
            boolean flag = virtualService.tenderCreate(voVirtualReq);
            if (flag) {
                return ResponseEntity.ok(VoBaseResp.ok("投标成功"));
            } else {
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "投标失败"));
            }
        } catch (Throwable e) {
            log.info("VirtualBizImpl createTender fail", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标失败"));
        }
    }

    /**
     * 奖励统计
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<VoViewAwardStatisticsWarpRes> statistics(Long userId) {
        VoViewAwardStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAwardStatisticsWarpRes.class);
        try {
            AwardStatistics awardStatistics = virtualService.query(userId);
            warpRes.setAwardStatistics(awardStatistics);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(
                        VoBaseResp.error(
                                VoBaseResp.ERROR,
                                "查询失败",
                                VoViewAwardStatisticsWarpRes.class
                            ));
        }
    }
}
