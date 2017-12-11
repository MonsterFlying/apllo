package com.gofobao.framework.contract.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.ContractResultContants;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.model.contract_auth_realname.RealNameAuthRequest;
import com.gofobao.framework.api.model.contract_auth_realname.RealNameAuthResponse;
import com.gofobao.framework.api.model.contract_bind_template.DebtTemplateRequest;
import com.gofobao.framework.api.model.contract_bind_template.DebtTemplateResponse;
import com.gofobao.framework.api.model.contract_debttemplate_query.TemplateQueryRequest;
import com.gofobao.framework.api.model.contract_debttemplate_query.TemplateQueryResponse;
import com.gofobao.framework.api.model.contract_details.ContractDetailsRequest;
import com.gofobao.framework.api.model.contract_details.ContractDetailsResponse;
import com.gofobao.framework.api.model.contract_entrust_enter.EntrustEnterRequest;
import com.gofobao.framework.api.model.contract_entrust_enter.EntrustEnterResponse;
import com.gofobao.framework.api.model.contract_entrust_protocol.EntrustProtocolRequest;
import com.gofobao.framework.api.model.contract_entrust_protocol.EntrustProtocolResponse;
import com.gofobao.framework.api.model.contract_get_contracts.ContractIdsRequest;
import com.gofobao.framework.api.model.contract_get_contracts.ContractIdsResponse;
import com.gofobao.framework.api.model.contract_get_contracts.SubPacks;
import com.gofobao.framework.api.model.contract_sms.SendSmsResponse;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.contract.biz.ContractBiz;
import com.gofobao.framework.contract.contants.ContractContants;
import com.gofobao.framework.contract.entity.BorrowContract;
import com.gofobao.framework.contract.service.ContractService;
import com.gofobao.framework.contract.vo.request.*;
import com.gofobao.framework.contract.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.IpHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by master on 2017/11/14.
 */
@Slf4j
@Service
public class ContractBizImpl implements ContractBiz {

    @Autowired
    private ContractService contractService;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Value("${jixin.contract.onlineAt}")
    private String onlineAt;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private Gson GSON;

    /**
     * 申请签署委托协议
     *
     * @param
     * @return
     */
    @Override
    public ResponseEntity<ApplyForContractRes> applyForSignContract(Long userId, HttpServletRequest request) {
        log.info("=======================================");
        log.info("============进入申请签署委托协议接口=========");
        log.info("=======================================");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        //是否开户
        if (ObjectUtils.isEmpty(userThirdAccount)
                || userThirdAccount.getAutoTenderState().equals("0")
                || userThirdAccount.getAutoTransferState().equals("0")) {
            log.error("当前用户未开通存管");
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "当前用户未开通存管",
                            ApplyForContractRes.class));
        }
        //是否需要实名
        if (StringUtils.isEmpty(userThirdAccount.getOpenAccountAt())) {
            log.info("当前用户需要实名");
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "对不起,当前用户需要实名",
                            ApplyForContractRes.class));
        }
        verifyRealName(userThirdAccount);
        log.info("打印签署用户信息:" + GSON.toJson(userThirdAccount));
        try {
            EntrustProtocolRequest entrustProtocolRequest = new EntrustProtocolRequest();
            entrustProtocolRequest.setAccountId(userThirdAccount.getAccountId());
            entrustProtocolRequest.setName(userThirdAccount.getName());
            entrustProtocolRequest.setIdType(IdTypeContant.ID_CARD);
            entrustProtocolRequest.setIdNo(userThirdAccount.getIdNo());
            entrustProtocolRequest.setMobile(userThirdAccount.getMobile());
            entrustProtocolRequest.setContractAuthType(ContractContants.LOAN);
            entrustProtocolRequest.setTemplateId(ContractContants.ENTRUST_AUTH_TEMPLATE);
            entrustProtocolRequest.setUserIP(IpHelper.getIpAddress(request));
            entrustProtocolRequest.setAcqRes("");
            EntrustProtocolResponse protocolResponse = contractService.applyForSignAuth(entrustProtocolRequest);
            if (!ObjectUtils.isEmpty(protocolResponse)
                    && protocolResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS)) {
                log.info("申请签署委托协议成功");
                ApplyForContractRes applyForContractRes = ApplyForContractRes.ok("申请签署委托协议成功", ApplyForContractRes.class);
                applyForContractRes.setContractId(protocolResponse.getContractId());
                applyForContractRes.setContractUrl(protocolResponse.getContractUrl());
                return ResponseEntity.ok(applyForContractRes);
            } else {
                log.error("申请签署委托协议失败");
                log.error("打印失败原因:" + protocolResponse.getMessage());
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                protocolResponse.getMessage(), ApplyForContractRes.class));
            }
        } catch (Exception e) {
            log.error("系统异常,申请签署委托协议失败");
            log.error("打印失败原因", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,请稍后再试吧",
                            ApplyForContractRes.class));
        }
    }

    /**
     * 发送短信验证码
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> sendSms(Long userId, String srvTxCode) {
        log.info("=================================");
        log.info("===========进入发送短信接口========");
        log.info("=================================");
        log.info("当前发送的是：" + (ContractContants.AUTHENTICATION.equals(srvTxCode) ? "实名短信" : "委托授权短信"));
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        //验证开户
        verifyOpenAccount(userThirdAccount);
        //如果是委托短信,用户是否需要实名
        if (ContractContants.SIGNCONTRACT.equals(srvTxCode)) {
            verifyRealName(userThirdAccount);
        }
        log.info("打印当前用户信息：" + GSON.toJson(userThirdAccount));
        try {
            SendSmsResponse smsResponse = contractService.sendSms(userThirdAccount.getMobile(), srvTxCode);
            if ((!ObjectUtils.isEmpty(smsResponse)) && (smsResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS))) {
                redisHelper.put(smsResponse.getSrvTxCode() + smsResponse.getMobile(), smsResponse.getSrvAuthCode());
                log.info("发送短信成功");
                log.info("打印业务授权码：" + smsResponse.getSrvAuthCode());
                return ResponseEntity.ok(VoBaseResp.ok("短信发送成功", VoBaseResp.class));
            } else {
                log.error("发送短信失败");
                log.error("打印发生短信失败原因：" + smsResponse.getMessage());
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                smsResponse.getMessage()));
            }
        } catch (Exception e) {
            log.error("系统异常,发送短信失败");
            log.error("打印失败原因", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,短信发送失败"));
        }
    }


    /**
     * 确定签署委托协议
     *
     * @param entrustEnterReq
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> entrustEnter(EntrustEnterReq entrustEnterReq, HttpServletRequest httpServletRequest) {
        log.info("=======================================");
        log.info("============进入确定签署委托协议接口=========");
        log.info("=======================================");
        log.info("打印接口参数：" + GSON.toJson(entrustEnterReq));
        try {
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(entrustEnterReq.getUserId());
            //是否开户
            verifyOpenAccount(userThirdAccount);
            //否是实名
            verifyRealName(userThirdAccount);

       /*     String srvAuthCode = redisHelper.get(ContractContants.SIGNCONTRACT + userThirdAccount.getMobile(), null);
            if (StringUtils.isEmpty(srvAuthCode)) {
                log.error("当前用户没有获取委托短信,非法访问");
                log.error("打印失败原因:当前用户没有获取委托短信,非法访问");
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                "当前用户没有获取委托短信,非法访问"));
            }*/
            EntrustEnterRequest entrustEnterRequest = new EntrustEnterRequest();
            entrustEnterRequest.setAccountId(userThirdAccount.getAccountId());
            entrustEnterRequest.setName(userThirdAccount.getName());
            entrustEnterRequest.setMobile(userThirdAccount.getMobile());
            entrustEnterRequest.setContractId(entrustEnterReq.getContractId());
            entrustEnterRequest.setSmsCode(entrustEnterReq.getSmsCode());
           // entrustEnterRequest.setSrvAuthCode(srvAuthCode);
            entrustEnterRequest.setContractAuthType(ContractContants.LOAN);
            EntrustEnterResponse entrustEnterResponse = contractService.entrustEnter(entrustEnterRequest);
            if (!ObjectUtils.isEmpty(entrustEnterResponse)
                    && entrustEnterResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS)) {
                userThirdAccount.setEntrustState(true);
                userThirdAccountService.save(userThirdAccount);

                log.info("确定签署委托协议成功");
                return ResponseEntity.ok(VoBaseResp.ok("确定签署委托协议成功"));
            } else {
                log.error("确定签署委托协议失败");
                log.error("打印失败原因:" + entrustEnterResponse.getMessage());
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                entrustEnterResponse.getMessage()));
            }

        } catch (Exception e) {
            log.error("系统异常,确定签署委托协议失败");
            log.error("打印失败原因", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,请稍后再试吧"));
        }

    }

    /**
     * 3.7 签证实名认证
     *
     * @param entrustAuthReq
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> realNameAuth(EntrustAuthReq entrustAuthReq, HttpServletRequest request) {
        log.info("=======================================");
        log.info("============进入实名认证接口=============");
        log.info("=======================================");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(entrustAuthReq.getUserId());
        verifyOpenAccount(userThirdAccount);
        log.info("打印用户实名信息:" + GSON.toJson(userThirdAccount));
        try {
            String srvAuthCode = redisHelper.get(ContractContants.AUTHENTICATION + userThirdAccount.getMobile(), null);
            if (StringUtils.isEmpty(srvAuthCode)) {
                log.info("非法请求,当前用户需获得短信验证码");
                log.info("打印当前用户信息：" + GSON.toJson(userThirdAccount));
                return ResponseEntity.ok(VoBaseResp.ok("非法请求", VoBaseResp.class));
            }
            RealNameAuthRequest realNameAuthRequest = new RealNameAuthRequest();
            realNameAuthRequest.setSmsCode(entrustAuthReq.getSmsCode());
            realNameAuthRequest.setSrvAuthCode(srvAuthCode);
            realNameAuthRequest.setName(userThirdAccount.getName());
            realNameAuthRequest.setMobile(userThirdAccount.getMobile());
            realNameAuthRequest.setIdNo(userThirdAccount.getIdNo());
            realNameAuthRequest.setAccountId(userThirdAccount.getAccountId());
            realNameAuthRequest.setIdType(IdTypeContant.ID_CARD);
            realNameAuthRequest.setUserIP(IpHelper.getIpAddress(request));
            log.info("打印接口参数：" + GSON.toJson(realNameAuthRequest));
            RealNameAuthResponse realNameAuthResponse = contractService.realNameAuth(realNameAuthRequest);
            if (!ObjectUtils.isEmpty(realNameAuthResponse)
                    && realNameAuthResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS)) {
                userThirdAccount.setOpenAccountAt(new Date());
                userThirdAccountService.save(userThirdAccount);
                log.info("用户实名成功");
                return ResponseEntity.ok(VoBaseResp.ok("用户实名成功"));
            } else {
                log.error("用户实名失败");
                log.error("打印失败原因:" + realNameAuthResponse.getMessage());
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                realNameAuthResponse.getMessage()));
            }
        } catch (Exception e) {
            log.error("系统异常,用户实名失败");
            log.error("打印失败原因", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,请稍后再试吧"));
        }
    }

    /**
     * 标的号绑定合同模板
     *
     * @param bindBorrow
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> debtTemplate(BindBorrow bindBorrow) {
        log.info("======================================");
        log.info("=========进入标的号绑定合同模板=========");
        log.info("======================================");
        try {
            Long borrowId = bindBorrow.getProductId();
            DebtTemplateRequest debtTemplateRequest = new DebtTemplateRequest();
            debtTemplateRequest.setTemplateId(bindBorrow.getTemplateId().toString());
            debtTemplateRequest.setProductId(ContractContants.BORROW_PERFIX + borrowId);
            debtTemplateRequest.setTradeType(bindBorrow.getTradeType().toString());
            log.info("打印请求参数: " + GSON.toJson(debtTemplateRequest));
            DebtTemplateResponse templateResponse = contractService.debtTemplate(debtTemplateRequest);
            log.info("打印即信响应信息：" + GSON.toJson(templateResponse));
            if (!ObjectUtils.isEmpty(templateResponse)
                    && templateResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS)) {
                log.info("标的号绑定合同模板成功");
                Borrow borrow = borrowService.findByBorrowId(borrowId);
                borrow.setIsContract(true);
                borrowService.save(borrow);
                return ResponseEntity.ok(VoBaseResp.ok("标的号绑定合同模板成功"));
            } else {
                log.info("标的号绑定合同模板失败,打印失败原因" + templateResponse.getMessage());
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                templateResponse.getMessage()));
            }
        } catch (Exception e) {
            log.info("系统异常");
            log.info("标的号绑定合同模板失败,打印失败原因", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,请稍后再试"));
        }
    }


    /**
     * @param templateQueryReq
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> templateQuery(TemplateQueryReq templateQueryReq) {
        try {
            TemplateQueryRequest queryRequest = new TemplateQueryRequest();
            queryRequest.setProductId(ContractContants.BORROW_PERFIX + templateQueryReq.getProductId());
            queryRequest.setTradeType(templateQueryReq.getTradeType());
            TemplateQueryResponse templateQueryResponse = contractService.templateQuery(queryRequest);
            if (!ObjectUtils.isEmpty(templateQueryResponse) && templateQueryResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS)) {

            }
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * 3.5获取合同ID列表
     *
     * @param contractIdsReq
     * @return
     */
    @Override
    public ResponseEntity<ContractIdsRes> contractIds(ContractIdsReq contractIdsReq) {
        log.info("======================================");
        log.info("=============获取合同ID列表============");
        log.info("======================================");
        log.info("打印参数:" + GSON.toJson(contractIdsReq));
        ContractIdsRequest contractIdsRequest = new ContractIdsRequest();
        Long userId = contractIdsReq.getTempUserId();
        //查询借贷债转合同
        if (contractIdsReq.getContractType().equals(ContractContants.LOAN)
                || contractIdsReq.getContractType().equals(ContractContants.DEBT)) {
            userId = contractIdsReq.getUserId();
            UserThirdAccount forUserThirdAccount = null;
            if (contractIdsReq.getTempUserId().equals(userId)) {
                forUserThirdAccount = userThirdAccountService.findByUserId(userId);
                contractIdsRequest.setAccountId(forUserThirdAccount.getAccountId());
            } else if (contractIdsReq.getTempUserId().equals(contractIdsReq.getForUserId())) {
                forUserThirdAccount = userThirdAccountService.findByUserId(contractIdsReq.getForUserId());
                contractIdsRequest.setForAccountId(forUserThirdAccount.getAccountId());
            }
        } else { //查询开户委托合同
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
            contractIdsRequest.setAccountId(contractIdsReq.getTempUserId().toString());
            commonVerify(userThirdAccount, ContractIdsRes.class);
        }
        contractIdsRequest.setAcqRes(contractIdsReq.getBatchNo());
        contractIdsRequest.setContractType(contractIdsReq.getContractType());
        if (!StringUtils.isEmpty(contractIdsReq.getBorrowId())) {
            Borrow borrow = borrowService.findByBorrowId(contractIdsReq.getBorrowId());
            if (ObjectUtils.isEmpty(borrow)
                    || StringUtils.isEmpty(borrow.getProductId())) {
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                "非法请求",
                                ContractIdsRes.class));
            }
            if (!borrow.getIsContract()) {
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                "当前标的没有绑定合同模板",
                                ContractIdsRes.class));
            }
            contractIdsRequest.setProductId(contractIdsReq.getBorrowId().toString());
        }
        contractIdsRequest.setChannel(ChannelContant.HTML);
        ContractIdsResponse contractIdsResponse = contractService.contractIds(contractIdsRequest);
        ContractIdsRes contractIdsRes = VoBaseResp.ok("查询成功", ContractIdsRes.class);
        if (!ObjectUtils.isEmpty(contractIdsResponse)
                && contractIdsResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS_STR)) {
            List<SubPacks> subPacks = contractIdsResponse.getSubPacks();
            if (CollectionUtils.isEmpty(subPacks)) {
                return ResponseEntity.ok(contractIdsRes);
            }
            List<ContractId> contracts = contractIdsRes.getContracts();
            subPacks.parallelStream().forEach(subPacks1 -> {
                ContractId contract = new ContractId();
                contract.setContractId(subPacks1.getContractId());
                //借款用户
                UserThirdAccount userThirdAccount1 = userThirdAccountService.findByAccountId(subPacks1.getAccountId());
                Users userServiceById = userService.findById(userThirdAccount1.getUserId());
                //投资用户
                UserThirdAccount forUserThirdAccount = userThirdAccountService.findByAccountId(subPacks1.getForAccountId());
                Users forUser = userService.findById(forUserThirdAccount.getUserId());
                if (userServiceById.getId().equals(contractIdsReq.getTempUserId())) {  //当前用户是借款人
                    contract.setUserName(forUser.getPhone());
                } else {
                    contract.setUserName(userServiceById.getUsername());
                }
                contracts.add(contract);
            });
            contractIdsRes.setContracts(contracts);
            return ResponseEntity.ok(contractIdsRes);

        } else {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            contractIdsResponse.getMessage(),
                            ContractIdsRes.class));
        }
    }

    /**
     * 3.6查看合同内容
     *
     * @param contractDetailsReq
     * @return
     */
    @Override

    public ResponseEntity<ContractDetails> contractContent(ContractDetailsReq contractDetailsReq) {
        try {
            log.info("======================================");
            log.info("=============查看合同内容============");
            log.info("======================================");
            log.info("打印参数:" + GSON.toJson(contractDetailsReq));
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(contractDetailsReq.getUserId());
            commonVerify(userThirdAccount, ContractDetails.class);
            ContractDetailsRequest contractDetailsRequest = new ContractDetailsRequest();
            contractDetailsRequest.setContractId(contractDetailsReq.getContractId());
            contractDetailsRequest.setAccountId(userThirdAccount.getAccountId());
            contractDetailsRequest.setDocumentType(contractDetailsReq.getDocumentType());
            ContractDetailsResponse contractDetailsResponse = contractService.contractDetails(contractDetailsRequest);
            ContractDetails contractDetails = VoBaseResp.ok("查询成功", ContractDetails.class);
            if (!ObjectUtils.isEmpty(contractDetailsResponse)
                    && contractDetailsResponse.getResult().equalsIgnoreCase(ContractResultContants.SUCCESS_STR)) {
                contractDetails.setDocumentDir(contractDetailsResponse.getDocumentDir());
                return ResponseEntity.ok(contractDetails);
            } else {
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,
                                contractDetailsResponse.getMessage(),
                                ContractDetails.class));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "系统异常,请稍后再试",
                            ContractDetails.class));
        }
    }

    /**
     * 用户是否开户
     *
     * @param userThirdAccount
     * @return
     */
    private ResponseEntity<VoBaseResp> verifyOpenAccount(UserThirdAccount userThirdAccount) {
        if (ObjectUtils.isEmpty(userThirdAccount)
                || userThirdAccount.getAutoTenderState().equals("0")) {
            log.error("当前用户未开通存管");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未开通存管"));
        }
        return null;
    }

    /**
     * 用户是否实名
     *
     * @param userThirdAccount
     * @return
     */
    private ResponseEntity<VoBaseResp> verifyRealName(UserThirdAccount userThirdAccount) {
        if (ObjectUtils.isEmpty(userThirdAccount) || StringUtils.isEmpty(userThirdAccount.getOpenAccountAt())) {
            log.info("当前用户需要合同实名");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "对不起,需要账户须实名"));
        }
        return null;
    }

    /**
     * @param userThirdAccount
     * @return
     */
    private ResponseEntity<VoBaseResp> verifyEntrust(UserThirdAccount userThirdAccount) {
        if (ObjectUtils.isEmpty(userThirdAccount) || !userThirdAccount.getEntrustState()) {
            log.info("当前用户需要签署委托授权");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "对不起,当前用户需要签署委托授权"));
        }
        return null;
    }

    private ResponseEntity commonVerify(UserThirdAccount userThirdAccount, Class obj) {
        ResponseEntity<VoBaseResp> verifyOpenAccount = verifyOpenAccount(userThirdAccount);
        ResponseEntity<VoBaseResp> verifyRealName = verifyRealName(userThirdAccount);
        ResponseEntity<VoBaseResp> verifyEntrust = verifyEntrust(userThirdAccount);
        if (!ObjectUtils.isEmpty(verifyOpenAccount)
                || !ObjectUtils.isEmpty(verifyRealName)
                || !ObjectUtils.isEmpty(verifyEntrust)) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            !ObjectUtils.isEmpty(verifyOpenAccount)
                                    ? verifyOpenAccount.getBody().getState().getMsg()
                                    : !ObjectUtils.isEmpty(verifyRealName)
                                    ? verifyRealName.getBody().getState().getMsg()
                                    : verifyEntrust.getBody().getState().getMsg(),
                            obj));
        }
        return null;
    }

    /**
     * @param contractBorrowIds
     * @return
     */
    @Override
    public ResponseEntity<BorrowContractListWarpRes> borrowContractList(ContractBorrowIds contractBorrowIds, Page page) {
        List<BorrowContract> borrowContracts = contractService.findUserContracts(contractBorrowIds, page);
        BorrowContractListWarpRes borrowContractListWarpRes = VoBaseResp.ok("查询成功", BorrowContractListWarpRes.class);
        if (CollectionUtils.isEmpty(borrowContracts)) {
            return ResponseEntity.ok(borrowContractListWarpRes);
        }
        List<BorrowContractListWarpRes.BorrowContractInfo> borrowContractInfos = new ArrayList<>();
        borrowContracts.forEach(borrowContract -> {
            BorrowContractListWarpRes.BorrowContractInfo borrowContractInfo = borrowContractListWarpRes.new BorrowContractInfo();
            Long borrowId = borrowContract.getBorrowId();
            Borrow borrow = borrowService.findById(borrowId);
            borrowContractInfo.setBorrowId(borrowId);
            borrowContractInfo.setBorrowName(borrowContract.getBorrowName());
            borrowContractInfo.setUserId(borrowContract.getUserId());
            borrowContractInfo.setForUserId(borrowContract.getForUserId());
            borrowContractInfo.setRecheckAt(DateHelper.dateToString(borrow.getRecheckAt(), DateHelper.DATE_FORMAT_YMD));
            borrowContractInfo.setTenderAt(DateHelper.dateToString(borrowContract.getCreatedAt(), DateHelper.DATE_FORMAT_YMD));
            borrowContractInfo.setBatchNo(borrowContract.getBatchNo());
            borrowContractInfos.add(borrowContractInfo);
        });

        borrowContractListWarpRes.setBorrowContractInfoList(borrowContractInfos);
        return ResponseEntity.ok(borrowContractListWarpRes);
    }
}
