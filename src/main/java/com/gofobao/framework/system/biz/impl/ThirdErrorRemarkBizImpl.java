package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayRunResp;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestRunCall;
import com.gofobao.framework.api.model.batch_lend_pay.BatchLendPayRunResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.system.biz.ThirdErrorRemarkBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdErrorRemark;
import com.gofobao.framework.system.service.ThirdErrorRemarkService;
import com.gofobao.framework.system.vo.request.VoDealThirdErrorReq;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Zeke on 2017/8/25.
 */
@Service
public class ThirdErrorRemarkBizImpl implements ThirdErrorRemarkBiz {

    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdErrorRemarkService thirdErrorRemarkService;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;

    /**
     * 处理失败批次
     *
     * @return
     */
    public ResponseEntity<VoBaseResp> dealThirdError(VoDealThirdErrorReq voDealThirdErrorReq) {
        String paramStr = voDealThirdErrorReq.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voDealThirdErrorReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        long remarkId = NumberHelper.toLong(paramMap.get("remarkId"));
        /* 第三方错误备注记录 */
        ThirdErrorRemark thirdErrorRemark = thirdErrorRemarkService.findByIdLock(remarkId);
        // 重试错误操作
        switch (thirdErrorRemark.getType()) {
            case ThirdBatchLogContants.BATCH_CREDIT_INVEST: // 投资人批次购买债权
                // 批次债权转让结果处理
                tenderThirdBiz.dealBatchCreditInvest(GSON.fromJson(thirdErrorRemark.getThirdRespStr(), new TypeToken<BatchCreditInvestRunCall>() {
                }.getType()));
                break;
            case ThirdBatchLogContants.BATCH_LEND_REPAY: // 即信批次放款
                // 即信批次放款结果处理
                borrowRepaymentThirdBiz.dealBatchLendRepay(GSON.fromJson(thirdErrorRemark.getThirdRespStr(), new TypeToken<BatchLendPayRunResp>() {
                }.getType()));
                break;
            case ThirdBatchLogContants.BATCH_REPAY: //即信批次还款
                // 即信批次还款结果处理
                borrowRepaymentThirdBiz.dealBatchRepay(GSON.fromJson(thirdErrorRemark.getThirdRespStr(), new TypeToken<BatchRepayRunResp>() {
                }.getType()));
                break;
            case ThirdBatchLogContants.BATCH_BAIL_REPAY: //名义借款人垫付
                // 即信批次名义借款人垫付处理
                borrowRepaymentThirdBiz.dealBatchAdvance(GSON.fromJson(thirdErrorRemark.getThirdRespStr(), new TypeToken<BatchBailRepayRunResp>() {
                }.getType()));
                break;
            case ThirdBatchLogContants.BATCH_CREDIT_END: //批次结束债权
                // 批次结束债权
                break;
            case ThirdBatchLogContants.BATCH_REPAY_ALL: //提前结清批次还款
                // 提前结清批次还款

                break;
            default:
        }

        return ResponseEntity.ok(VoBaseResp.ok("处理成功!"));
    }
}
