package com.gofobao.framework.contract.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.contract.biz.ContractBiz;
import com.gofobao.framework.contract.contants.ContractContants;
import com.gofobao.framework.contract.vo.request.*;
import com.gofobao.framework.contract.vo.response.ApplyForContractRes;
import com.gofobao.framework.contract.vo.response.BorrowContractListWarpRes;
import com.gofobao.framework.contract.vo.response.ContractDetails;
import com.gofobao.framework.contract.vo.response.ContractIdsRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

/**
 * @author master
 * @date 2017/11/14
 */

@Api(description = "电子合同接口")
@RestController
@RequestMapping("/contract")
public class ContractController {

    @Autowired
    private ContractBiz contractBiz;

    /**
     * 委托短信
     *
     * @param userId
     * @return
     */
    @ApiOperation("发送委托短信")
    @RequestMapping(value = "/entrust/sendSms", method = RequestMethod.POST)
    public ResponseEntity<VoBaseResp> authorizeSendSms(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return contractBiz.sendSms(userId, ContractContants.SIGNCONTRACT);
    }

    /**
     * 实名短信
     *
     * @param userId
     * @return
     */
    @ApiOperation("发送实名短信")
    @RequestMapping(value = "/realName/sendSms", method = RequestMethod.POST)
    public ResponseEntity<VoBaseResp> realNameSendSms(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return contractBiz.sendSms(userId, ContractContants.AUTHENTICATION);
    }


    /**
     * 申请签署委托授权
     *
     * @param userId
     * @return
     */
    @ApiOperation("申请签署委托授权")
    @RequestMapping(value = "/applyFor/signEntrust", method = RequestMethod.GET)
    public ResponseEntity<ApplyForContractRes> entrustAuth(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                           HttpServletRequest request) {
        return contractBiz.applyForSignContract(userId, request);
    }


    @RequestMapping(value = "enter/auth", method = RequestMethod.POST)
    @ApiOperation("确认委托授权")
    public ResponseEntity<VoBaseResp> enterAuth(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                HttpServletRequest request,
                                                EntrustEnterReq entrustEnterReq) {
        entrustEnterReq.setUserId(userId);
        return contractBiz.entrustEnter(entrustEnterReq, request);
    }

    /**
     * 实名
     *
     * @param userId
     * @param request
     * @param entrustAuth
     * @return
     */
    @ApiOperation("实名")
    @RequestMapping(value = "/realName/auth", method = RequestMethod.POST)
    public ResponseEntity<VoBaseResp> realName(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                               HttpServletRequest request,
                                               EntrustAuthReq entrustAuth) {
        entrustAuth.setUserId(userId);
        return contractBiz.realNameAuth(entrustAuth, request);
    }

    /**
     * 合同列表
     *
     * @param
     * @param contractIdsReq
     * @return
     */
    @RequestMapping(value = "/ids", method = RequestMethod.GET)
    public ResponseEntity<ContractIdsRes> contractIds(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                      ContractIdsReq contractIdsReq) {
        contractIdsReq.setTempUserId(userId);
        return contractBiz.contractIds(contractIdsReq);
    }

    /**
     * 查看合同
     *
     * @param userId
     * @param contractDetailsReq
     * @return
     */
    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ResponseEntity<ContractDetails> contractDetails(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                           ContractDetailsReq contractDetailsReq) {
        contractDetailsReq.setUserId(userId);
        return contractBiz.contractContent(contractDetailsReq);
    }

    /**
     * @param userId
     * @param contractBorrowIds
     * @return
     */
    @RequestMapping(value = "borrow/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    public ResponseEntity<BorrowContractListWarpRes> contractBorrowList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                        @PathVariable("pageIndex")Integer pageIndex,
                                                                        @PathVariable("pageSize")Integer pageSize,
                                                                        ContractBorrowIds contractBorrowIds) {
        Page page=new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        contractBorrowIds.setUserId(userId);
        return contractBiz.borrowContractList(contractBorrowIds,page);

    }

}
