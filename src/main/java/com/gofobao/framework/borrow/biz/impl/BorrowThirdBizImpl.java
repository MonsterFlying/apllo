package com.gofobao.framework.borrow.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.debt_register.DebtRegisterRequest;
import com.gofobao.framework.api.model.debt_register.DebtRegisterResponse;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.vo.request.VoCreateThirdBorrowReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
public class BorrowThirdBizImpl implements BorrowThirdBiz{

    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;

    public ResponseEntity<VoBaseResp> createThirdBorrow( VoCreateThirdBorrowReq voCreateThirdBorrowReq){

        Long userId = voCreateThirdBorrowReq.getUserId();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        UserThirdAccount bailUserThirdAccount = userThirdAccountService.findByUserId(voCreateThirdBorrowReq.getBailUserId());
        UserThirdAccount nominalUserThirdAccount = userThirdAccountService.findByUserId(voCreateThirdBorrowReq.getNominalUserId());

        DebtRegisterRequest request = new DebtRegisterRequest();
        request.setAccountId(userThirdAccount.getAccountId());
        request.setProductId(StringHelper.toString(voCreateThirdBorrowReq.getProductId()));
        request.setProductDesc(voCreateThirdBorrowReq.getProductDesc());
        request.setRaiseDate(voCreateThirdBorrowReq.getRaiseDate());
        request.setRaiseEndDate(voCreateThirdBorrowReq.getRaiseEndDate());
        request.setIntType(voCreateThirdBorrowReq.getIntType());
        request.setIntPayDay(voCreateThirdBorrowReq.getIntPayDay());
        request.setDuration(voCreateThirdBorrowReq.getDuration());
        request.setTxAmount(voCreateThirdBorrowReq.getTxAmount());
        request.setRate(voCreateThirdBorrowReq.getRate());
        request.setTxFee(voCreateThirdBorrowReq.getTxFee());
        if (!ObjectUtils.isEmpty(bailUserThirdAccount)) {
            request.setBailAccountId(bailUserThirdAccount.getAccountId());
        }
        if (!ObjectUtils.isEmpty(nominalUserThirdAccount)) {
            request.setNominalAccountId(nominalUserThirdAccount.getAccountId());
        }
        request.setAcqRes(voCreateThirdBorrowReq.getAcqRes());
        request.setChannel(ChannelContant.HTML);

        DebtRegisterResponse response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER, request, DebtRegisterResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }
        return null;
    }
}
