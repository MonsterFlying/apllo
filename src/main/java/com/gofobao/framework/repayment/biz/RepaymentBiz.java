package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.borrow.vo.request.VoRepayAllReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.repayment.vo.response.VoViewRepayCollectionLogWarpRes;
import com.gofobao.framework.repayment.vo.response.VoViewRepaymentOrderDetailWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewCollectionWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewOrderListWarpRes;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/5.
 */
public interface RepaymentBiz {

    /**
     * 提前结清处理
     *
     * @param borrowId
     */
    ResponseEntity<VoBaseResp> repayAllDeal(long borrowId,long batchNo) throws Exception;

    /**
     * pc提前结清
     *
     * @param voRepayAllReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> pcRepayAll(VoRepayAllReq voRepayAllReq) throws Exception;

    /**
     * 提前结清
     *
     * @param borrowId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseEntity<VoBaseResp> repayAll(long borrowId) throws Exception;


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
    void toExcel(HttpServletResponse response, VoOrderListReq listReq);

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
     * 新还款处理
     *
     * @param repaymentId
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> newRepayDeal(long repaymentId, long batchNo) throws Exception;

    /**
     * 当前应还款日期
     *
     * @param userId
     * @param time
     * @return
     */
    ResponseEntity<VoViewCollectionDaysWarpRes> days(Long userId, String time);

    /**
     * pc 垫付
     *
     * @param voPcAdvanceReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> pcAdvance(VoPcAdvanceReq voPcAdvanceReq) throws Exception;

    /**
     * 新版垫付处理
     *
     * @param repaymentId
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> newAdvanceDeal(long repaymentId, long batchNo) throws Exception;

    /**
     * 新版垫付
     *
     * @param voAdvanceReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> newAdvance(VoAdvanceReq voAdvanceReq) throws Exception;


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
