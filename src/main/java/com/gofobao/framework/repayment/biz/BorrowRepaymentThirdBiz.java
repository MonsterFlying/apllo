package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.api.model.batch_bail_repay.BailRepay;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.batch_repay_bail.RepayBail;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.entity.AdvanceAssetChange;
import com.gofobao.framework.repayment.entity.RepayAssetChange;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
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
     * @return
     */
    ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception;

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 批次担保账户代偿参数检查回调
     */
    ResponseEntity<String> thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次担保账户代偿业务处理回调
     */
    ResponseEntity<String> thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次融资人还担保账户垫款参数检查回调
     *
     * @param request
     * @param response
     */
    ResponseEntity<String> thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次融资人还担保账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    ResponseEntity<String> thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取存管 收到还款 数据集合
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param borrowAccountId 借款方即信存管账户id
     * @param lateDays
     * @param lateInterest
     * @return
     * @throws Exception
     */
    void receivedRepay(List<Repay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, long lateInterest) throws Exception;

/**
     * 生成担保人代偿记录
     *
     * @param borrow
     * @param order
     * @param advanceAssetChanges
     */
    List<BailRepay> calculateAdvancePlan(Borrow borrow, int order, List<AdvanceAssetChange> advanceAssetChanges,int lateDays,long lateInterest) throws Exception;

    /**
     * 新版生成还款计划
     *
     * @param borrow
     * @param repayAccountId
     * @param order
     * @param lateDays
     * @param lateInterest
     * @param interestPercent
     * @param repayAssetChanges
     * @throws Exception
     */
    List<Repay> calculateRepayPlan(Borrow borrow, String repayAccountId, int order, int lateDays, long lateInterest,double interestPercent, List<RepayAssetChange> repayAssetChanges) throws Exception;

    /**
     * 生成借款人偿还担保人计划
     *
     * @param borrow
     * @param repayAccountId
     * @param lateDays
     * @param order
     * @param lateInterest
     * @return
     */
    List<RepayBail> calculateRepayBailPlan(Borrow borrow, String repayAccountId, int lateDays, Integer order, long lateInterest) throws Exception;
}
