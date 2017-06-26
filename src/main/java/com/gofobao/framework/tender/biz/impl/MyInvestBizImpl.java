package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.biz.MyInvestBiz;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Slf4j
@Service
public class MyInvestBizImpl implements MyInvestBiz {

    @Autowired
    private InvestService investService;


    @Override
    public ResponseEntity<VoViewBackMoneyListWarpRes> backMoneyList(VoInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BACK_MONEY);
        try {
            List<VoViewBackMoney> backMoneyList = investService.backMoneyList(voInvestListReq);
            VoViewBackMoneyListWarpRes voViewBackMoneyListWarpRes = VoBaseResp.ok("查询成功", VoViewBackMoneyListWarpRes.class);
            voViewBackMoneyListWarpRes.setVoViewBackMonies(backMoneyList);
            return ResponseEntity.ok(voViewBackMoneyListWarpRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewBackMoneyListWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewBiddingListWrapRes> biddingList(VoInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BIDDING);
        try {
            List<VoViewBiddingRes> viewBiddingRes = investService.biddingList(voInvestListReq);
            VoViewBiddingListWrapRes voViewBuddingResListWrapRes = VoBaseResp.ok("查询成功", VoViewBiddingListWrapRes.class);
            voViewBuddingResListWrapRes.setVoViewBiddingRes(viewBiddingRes);
            return ResponseEntity.ok(voViewBuddingResListWrapRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewBiddingListWrapRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewSettleWarpRes> settleList(VoInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.SETTLE);
        try {
            List<VoViewSettleRes> viewBiddingRes = investService.settleList(voInvestListReq);
            VoViewSettleWarpRes viewSettleWarpRes = VoBaseResp.ok("查询成功", VoViewSettleWarpRes.class);
            viewSettleWarpRes.setVoViewSettleRes(viewBiddingRes);
            return ResponseEntity.ok(viewSettleWarpRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewSettleWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewTenderDetailWarpRes> tenderDetail(VoDetailReq voDetailReq) {
        try {
            VoViewTenderDetail viewTenderDetail = investService.tenderDetail(voDetailReq);
            VoViewTenderDetailWarpRes voViewTenderDetailWarpRes = VoBaseResp.ok("查询成功", VoViewTenderDetailWarpRes.class);
            voViewTenderDetailWarpRes.setVoViewTenderDetail(viewTenderDetail);
            return ResponseEntity.ok(voViewTenderDetailWarpRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewTenderDetailWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewReturnMoneyWarpRes> infoList(VoDetailReq voDetailReq) {
        try {
            VoViewReturnedMoney voViewReturnedMoney = investService.infoList(voDetailReq);
            VoViewReturnMoneyWarpRes voViewReturnMoneyWarpRes = VoBaseResp.ok("", VoViewReturnMoneyWarpRes.class);
            voViewReturnMoneyWarpRes.setVoViewReturnedMoney(voViewReturnedMoney);
            return ResponseEntity.ok(voViewReturnMoneyWarpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewReturnMoneyWarpRes.class));
        }
    }
}
