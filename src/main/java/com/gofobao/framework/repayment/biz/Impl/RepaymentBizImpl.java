package com.gofobao.framework.repayment.biz.Impl;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2017/6/6.
 */
@Service
public class RepaymentBizImpl implements RepaymentBiz {
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    /**
     * 还款计划
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
        try {
            VoViewCollectionOrderList voViewCollectionOrderListRes = borrowRepaymentService.repaymentList(voCollectionOrderReq);
            VoViewCollectionOrderListResWarpRes voViewCollectionOrderListResWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionOrderListResWarpRes.class);
            voViewCollectionOrderListResWarpRes.setListRes(voViewCollectionOrderListRes);
            return ResponseEntity.ok(voViewCollectionOrderListResWarpRes);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionOrderListResWarpRes.class));
        }
    }

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderDetailWarpRes> info(VoInfoReq voInfoReq) {
        try {
            VoViewOrderDetailRes voViewOrderDetailRes = borrowRepaymentService.info(voInfoReq);
            VoViewOrderDetailWarpRes voViewOrderDetailWarpRes = VoBaseResp.ok("查询成功", VoViewOrderDetailWarpRes.class);
            voViewOrderDetailWarpRes.setDetailWarpRes(voViewOrderDetailRes);
            return ResponseEntity.ok(voViewOrderDetailWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewOrderDetailWarpRes.class));
        }
    }
}
