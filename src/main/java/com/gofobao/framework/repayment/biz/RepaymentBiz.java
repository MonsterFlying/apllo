package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.VoAdvanceReq;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.repayment.vo.request.VoInstantlyRepaymentReq;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<VoViewCollectionOrderListResWarpRes> repaymentList(VoCollectionOrderReq voCollectionOrderReq);

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    ResponseEntity<VoViewOrderDetailWarpRes> info(VoInfoReq voInfoReq);

    /**
     * 立即还款
     *
     * @param voInstantlyRepayment
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> instantly(VoInstantlyRepaymentReq voInstantlyRepayment) throws Exception;

    /**
     * 还款
     *
     * @param voRepayReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> repay(VoRepayReq voRepayReq) throws Exception;

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
     * @param voAdvanceReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> advanceDeal(VoAdvanceReq voAdvanceReq)throws Exception;
}
