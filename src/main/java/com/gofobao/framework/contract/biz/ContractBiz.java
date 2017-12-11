package com.gofobao.framework.contract.biz;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.contract.vo.request.*;
import com.gofobao.framework.contract.vo.response.ApplyForContractRes;
import com.gofobao.framework.contract.vo.response.BorrowContractListWarpRes;
import com.gofobao.framework.contract.vo.response.ContractDetails;
import com.gofobao.framework.contract.vo.response.ContractIdsRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * @author master
 * @date 2017/11/14
 */
public interface ContractBiz {

    /**
     * 发送短信短信验证码
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoBaseResp> sendSms(Long userId, String srvTxCode);

    /**
     * 申请签署委托协议
     *
     * @param userId
     * @return
     */
    ResponseEntity<ApplyForContractRes> applyForSignContract(Long userId, HttpServletRequest request);

    /**
     * 确认委托授权
     *
     * @param entrustEnterReq
     * @return
     */
    ResponseEntity<VoBaseResp> entrustEnter(EntrustEnterReq entrustEnterReq, HttpServletRequest httpServletRequest);


    /**
     * 用户实名
     *
     * @param entrustAuthReq
     * @return
     */
    ResponseEntity<VoBaseResp> realNameAuth(EntrustAuthReq entrustAuthReq, HttpServletRequest request);

    /**
     * 标的号绑定合同模板
     *
     * @param bindBorrow
     * @return
     */
    ResponseEntity<VoBaseResp> debtTemplate(BindBorrow bindBorrow);

    /**
     * 3.3标的号绑定合同模板查询接口
     *
     * @param templateQueryReq
     * @return
     */
    ResponseEntity<VoBaseResp> templateQuery(TemplateQueryReq templateQueryReq);


    /**
     * 3.5获取合同ID列表
     *
     * @param contractIdsReq
     * @return
     */
    ResponseEntity<ContractIdsRes> contractIds(ContractIdsReq contractIdsReq);


    /**
     * 3.6查看合同内容
     *
     * @param contractDetailsReq
     * @return
     */
    ResponseEntity<ContractDetails> contractContent(ContractDetailsReq contractDetailsReq);


    /**
     *
     * @param contractBorrowIds
     * @return
     */
    ResponseEntity<BorrowContractListWarpRes> borrowContractList(ContractBorrowIds contractBorrowIds, Page page);

}
