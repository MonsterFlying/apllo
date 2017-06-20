package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.VoBatchBailRepayReq;
import com.gofobao.framework.repayment.vo.request.VoBatchRepayBailReq;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Zeke on 2017/6/8.
 */
public interface BorrowRepaymentThirdBiz {
    /**
     * 即信批次还款
     *
     * @param voThirdBatchRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchRepay(VoThirdBatchRepay voThirdBatchRepay) throws Exception;

    /**
     * 即信批次还款
     *
     * @return
     */
    void thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    void thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    void thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    void thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次担保账户代偿
     *
     * @param voBatchBailRepayReq
     */
    ResponseEntity<VoBaseResp> thirdBatchBailRepay(VoBatchBailRepayReq voBatchBailRepayReq) throws Exception;

    /**
     * 批次担保账户代偿参数检查回调
     */
    void thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次担保账户代偿业务处理回调
     */
    void thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次融资人还担保账户垫款
     *
     * @param voBatchRepayBailReq
     */
    ResponseEntity<VoBaseResp> thirdBatchRepayBail(VoBatchRepayBailReq voBatchRepayBailReq);

    /**
     * 批次融资人还担保账户垫款参数检查回调
     *
     * @param request
     * @param response
     */
    void thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次融资人还担保账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    void thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取即信还款集合
     *
     * @param voThirdBatchRepay
     * @return
     * @throws Exception
     */
    List<Repay> getRepayList(VoThirdBatchRepay voThirdBatchRepay) throws Exception;

}
