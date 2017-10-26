package com.gofobao.framework.finance.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.MyFinanceInvestBiz;
import com.gofobao.framework.finance.service.FinanceInvestService;
import com.gofobao.framework.finance.vo.response.VoViewFinanceTenderDetailWarpRes;
import com.gofobao.framework.system.vo.request.VoFinanceDetailReq;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnMoneyWarpRes;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnedMoney;
import com.gofobao.framework.system.vo.response.VoViewFinanceTenderDetail;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoFinanceInvestListReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/6.
 */
@Service
@Slf4j
public class MyFinanceInvestBizImpl implements MyFinanceInvestBiz {

    @Autowired
    private FinanceInvestService financeInvestService;


    @Override
    public ResponseEntity<VoViewFinanceBackMoneyListWarpRes> backMoneyList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BACK_MONEY);
        try {
            Map<String, Object> resultMaps = financeInvestService.backMoneyList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewFinanceBackMoney> backMoneyList = (List<VoViewFinanceBackMoney>) resultMaps.get("backMoneyList");
            VoViewFinanceBackMoneyListWarpRes voViewBackMoneyListWarpRes = VoBaseResp.ok("查询成功", VoViewFinanceBackMoneyListWarpRes.class);
            voViewBackMoneyListWarpRes.setVoViewBackMonies(backMoneyList);
            voViewBackMoneyListWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewBackMoneyListWarpRes);
        } catch (Throwable e) {
            log.error("查询异常:",e);
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceBackMoneyListWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceBiddingListWrapRes> biddingList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.BIDDING);
        try {
            Map<String, Object> resultMaps = financeInvestService.biddingList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewFinanceBiddingRes> biddingRes = (List<VoViewFinanceBiddingRes>) resultMaps.get("biddingResList");
            VoViewFinanceBiddingListWrapRes wrapResRes = VoBaseResp.ok("查询成功", VoViewFinanceBiddingListWrapRes.class);
            wrapResRes.setVoViewBiddingRes(biddingRes);
            wrapResRes.setTotalCount(totalCount);
            return ResponseEntity.ok(wrapResRes);
        } catch (Throwable e) {
            log.error("查询异常:",e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceBiddingListWrapRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceSettleWarpRes> settleList(VoFinanceInvestListReq voInvestListReq) {
        voInvestListReq.setType(TenderConstans.SETTLE);
        try {
            Map<String, Object> resultMaps = financeInvestService.settleList(voInvestListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewFinanceSettleRes> settleList = (List<VoViewFinanceSettleRes>) resultMaps.get("settleResList");

            VoViewFinanceSettleWarpRes viewSettleWarpRes = VoBaseResp.ok("查询成功", VoViewFinanceSettleWarpRes.class);
            viewSettleWarpRes.setTotalCount(totalCount);
            viewSettleWarpRes.setVoViewSettleRes(settleList);
            return ResponseEntity.ok(viewSettleWarpRes);
        } catch (Throwable e) {
            log.error("查询异常:",e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceSettleWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceTenderDetailWarpRes> tenderDetail(VoFinanceDetailReq voDetailReq) {
        try {
            VoViewFinanceTenderDetail viewTenderDetail = financeInvestService.tenderDetail(voDetailReq);
            VoViewFinanceTenderDetailWarpRes voViewTenderDetailWarpRes = VoBaseResp.ok("查询成功", VoViewFinanceTenderDetailWarpRes.class);
            voViewTenderDetailWarpRes.setVoViewTenderDetail(viewTenderDetail);
            return ResponseEntity.ok(voViewTenderDetailWarpRes);
        } catch (Throwable e) {
            log.error("查询异常:",e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceTenderDetailWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewFinanceReturnMoneyWarpRes> infoList(VoFinanceDetailReq voDetailReq) {
        try {
            VoViewFinanceReturnedMoney voViewReturnedMoney = financeInvestService.infoList(voDetailReq);
            VoViewFinanceReturnMoneyWarpRes voViewReturnMoneyWarpRes = VoBaseResp.ok("", VoViewFinanceReturnMoneyWarpRes.class);
            voViewReturnMoneyWarpRes.setVoViewReturnedMoney(voViewReturnedMoney);
            return ResponseEntity.ok(voViewReturnMoneyWarpRes);
        } catch (Throwable e) {
            log.error("查询异常:",e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFinanceReturnMoneyWarpRes.class));
        }
    }
}
