package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.repayment.vo.response.VoBuildThirdRepayResp;
import com.gofobao.framework.repayment.vo.response.VoViewRepayCollectionLogWarpRes;
import com.gofobao.framework.repayment.vo.response.VoViewRepaymentOrderDetailWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewCollectionWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewOrderListWarpRes;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/5.
 */
public interface RepaymentBiz {

    /**
     * 还款计划列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    ResponseEntity<VoViewCollectionOrderListWarpResp> repaymentList(VoCollectionOrderReq voCollectionOrderReq);

    /**
     * PC：还款计划列表
     *
     * @param listReq
     * @return
     */
    ResponseEntity<VoViewOrderListWarpRes> pcRepaymentList(VoOrderListReq listReq);


    /**
     * PC：还款计划列表导出excel
     *
     * @param listReq
     * @return
     */
    void toExcel(HttpServletResponse response,VoOrderListReq listReq);

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    ResponseEntity<VoViewRepaymentOrderDetailWarpRes> detail(VoInfoReq voInfoReq);

    /**
     * pc：未还款详情列表
     *
     * @param collectionListReq
     * @return
     */
    ResponseEntity<VoViewCollectionWarpRes> orderList(VoCollectionListReq collectionListReq);

    /**
     * 标还款记录
     *
     * @param borrowId
     * @return
     */
    ResponseEntity<VoViewRepayCollectionLogWarpRes> logs(Long borrowId);

    /**
     * 发起还款
     *
     * @param voRepayReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> newRepay(VoRepayReq voRepayReq) throws Exception;

    /**
     * 还款处理
     *
     * @param voRepayReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> repayDeal(VoRepayReq voRepayReq) throws Exception;

    /**
     * 当前应还款日期
     *
     * @param userId
     * @param time
     * @return
     */
    ResponseEntity<VoViewCollectionDaysWarpRes> days(Long userId, String time);

    /**
     * 垫付
     *
     * @param voAdvanceReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> advance(VoAdvanceReq voAdvanceReq) throws Exception;

    /**
     * 垫付处理
     *
     * @param voAdvanceCall
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> advanceDeal(VoAdvanceCall voAdvanceCall) throws Exception;

    /**
     * pc 垫付
     *
     * @param voPcAdvanceReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> pcAdvance(VoPcAdvanceReq voPcAdvanceReq) throws Exception;

    /**
     * pc 立即还款
     *
     * @param voPcInstantlyRepaymentReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> pcRepay(VoPcInstantlyRepaymentReq voPcInstantlyRepaymentReq) throws Exception;

    /**
     * 批次融资人还担保账户垫款
     *
     * @param voRepayReq
     */
    ResponseEntity<VoBaseResp> thirdBatchRepayBail(VoRepayReq voRepayReq) throws Exception;
}
