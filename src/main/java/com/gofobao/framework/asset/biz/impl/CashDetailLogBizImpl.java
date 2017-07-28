package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.with_daw.WithDrawRequest;
import com.gofobao.framework.api.model.with_daw.WithDrawResponse;
import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAdminCashReq;
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.VoCashLog;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Component
@Slf4j
public class CashDetailLogBizImpl implements CashDetailLogBiz {
    @Autowired
    AssetService assetService;

    @Autowired
    UserService userService;

    @Autowired
    CashDetailLogService cashDetailLogService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    CapitalChangeHelper capitalChangeHelper;

    @Autowired
    BankAccountBizImpl bankAccountBiz;


    @Value("${gofobao.javaDomain}")
    String javaDomain;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Value("${gofobao.h5Domain}")
    String h5Domain;

    static final Gson GSON = new Gson();
    @Value("${gofobao.aliyun-bankaps-url}")
    String aliyunQueryBankapsUrl;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode;

    @Autowired
    ThirdAccountPasswordHelper thirdAccountPasswordHelper;

    @Autowired
    DictItemService dictItemService;

    @Autowired
    DictValueService dictValueService;


    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;

    @Autowired
    MqHelper mqHelper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoPreCashResp> preCash(Long userId, HttpServletRequest httpServletRequest) {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoPreCashResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoPreCashResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoPreCashResp.class));
        }

        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if (ObjectUtils.isEmpty(balanceQueryResponse)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定,请稍后重试！", VoPreCashResp.class));
        }

        if (!balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定,请稍后重试！", VoPreCashResp.class));
        }


        if (!balanceQueryResponse.getWithdrawFlag().equals("1")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前存管账户被银行禁止提现, 如有问题请联系广富宝金服客服!", VoPreCashResp.class));
        }

        // 判断当前用户资金是否一直
        Asset asset = assetService.findByUserIdLock(userId);
        VoPreCashResp resp = VoBaseResp.ok("查询成功", VoPreCashResp.class);
        resp.setBankName(userThirdAccount.getBankName());
        resp.setLogo(String.format("%s/%s", javaDomain, userThirdAccount.getBankLogo()));
        resp.setCardNo(userThirdAccount.getCardNo().substring(userThirdAccount.getCardNo().length() - 4));


        resp.setUseMoneyShow(StringHelper.formatDouble(getRealCashMoney(userId) / 100D, true));
        resp.setUseMoney(getRealCashMoney(userId) / 100D);
        int time = queryFreeTime(userId);
        resp.setFreeTime(time);
        return ResponseEntity.ok(resp);
    }


    /**
     * 获取免费提现次数
     *
     * @param userId
     * @return
     */
    private int queryFreeTime(Long userId) {
        ImmutableList<Integer> states = ImmutableList.of(0, 1, 3);
        List<CashDetailLog> cashDetailLogs = cashDetailLogService.findByStateInAndUserId(states, userId);
        return CollectionUtils.isEmpty(cashDetailLogs) ? 10 : cashDetailLogs.size() > 10 ? 0 : 10 - cashDetailLogs.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest, Long userId, VoCashReq voCashReq) throws Exception {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoHtmlResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoHtmlResp.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoHtmlResp.class));
        }

        // 用户可提现余额
        Long useMoney = getRealCashMoney(userId);
        if (useMoney <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户余额为0！", VoHtmlResp.class));
        }

        // 对于大于5万小于20万直接返回失败
        if (voCashReq.getCashMoney() > 50000 && voCashReq.getCashMoney() < 200000) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "快捷通道: 只支持小于等于5万; 大额提现: 只支持大于等于20万", VoHtmlResp.class));
        }

        // 判断提现金额
        if (useMoney < voCashReq.getCashMoney() * 100) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "提现金额大于账户可用余额！", VoHtmlResp.class));
        }

        // 判断当天提现次数
        int cashTimes = bankAccountBiz.getCashCredit4Day(userId);
        if (cashTimes > 10) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "对不起, 当天提现次数大于10次!", VoHtmlResp.class));
        }
        Date nowDate = new Date();
        double cashMoney = voCashReq.getCashMoney() * 100;  // 提现金额
        // 免费体现次数
        int freeTime = queryFreeTime(userId);

        // 计算提现费用
        long fee = 0L;
        if (freeTime <= 0) {  // 收费
            fee = 200L;
        }

        boolean bigCashState = false;
        // 判断是否为大额提现
        if (voCashReq.getCashMoney() >= 200000) {
            bigCashState = true;
        }

        WithDrawRequest withDrawRequest = new WithDrawRequest();
        withDrawRequest.setSeqNo(RandomHelper.generateNumberCode(6));
        withDrawRequest.setTxTime(DateHelper.getTime());
        withDrawRequest.setTxDate(DateHelper.getDate());
        withDrawRequest.setIdType(IdTypeContant.ID_CARD);
        withDrawRequest.setIdNo(userThirdAccount.getIdNo());
        withDrawRequest.setName(userThirdAccount.getName());
        withDrawRequest.setMobile(userThirdAccount.getMobile());
        withDrawRequest.setCardNo(userThirdAccount.getCardNo());
        withDrawRequest.setAccountId(userThirdAccount.getAccountId());
        withDrawRequest.setTxAmount(StringHelper.formatDouble(new Double((cashMoney - fee) / 100D), false)); //  交易金额
        withDrawRequest.setRouteCode(bigCashState ? "2" : "0");
        if (bigCashState) {
            withDrawRequest.setCardBankCnaps(voCashReq.getBankAps()); // 联行卡号
        }
        withDrawRequest.setTxFee(StringHelper.formatDouble(new Double(fee / 100D), false));
        withDrawRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        withDrawRequest.setRetUrl(String.format("%s/%s/%s", javaDomain, "/pub/cash/show/", withDrawRequest.getTxDate() + withDrawRequest.getTxTime() + withDrawRequest.getSeqNo()));
        withDrawRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/asset/cash/callback"));
        withDrawRequest.setAcqRes(String.valueOf(userId));
        withDrawRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        // 生成提现表单
        String html = jixinManager.getHtml(JixinTxCodeEnum.WITH_DRAW, withDrawRequest);
        if (StringUtils.isEmpty(html)) {
            log.error("CashDetailLogBizImpl.cash 生成表单 ");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        } else {
            // 写入提现记录
            CashDetailLog cashDetailLog = new CashDetailLog();
            cashDetailLog.setThirdAccountId(userThirdAccount.getAccountId());
            cashDetailLog.setBankName(userThirdAccount.getBankName());
            cashDetailLog.setCardNo(userThirdAccount.getCardNo());
            cashDetailLog.setCashType(bigCashState ? 1 : 0);
            if (bigCashState) {
                cashDetailLog.setCompanyBankNo(voCashReq.getBankAps()); // 联行卡号
            }
            cashDetailLog.setCompanyBankNo(voCashReq.getBankAps());
            cashDetailLog.setFee(fee);
            cashDetailLog.setCreateTime(nowDate);
            cashDetailLog.setMoney(new Double(cashMoney).longValue());
            cashDetailLog.setSeqNo(withDrawRequest.getTxDate() + withDrawRequest.getTxTime() + withDrawRequest.getSeqNo());
            cashDetailLog.setState(4);  // 直接设置为失败
            cashDetailLog.setVerifyTime(nowDate);
            cashDetailLog.setVerifyUserId(0L);
            cashDetailLog.setUserId(userId);
            cashDetailLog.setVerifyRemark("系统自动审核通过");
            cashDetailLogService.save(cashDetailLog);
        }

        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (Throwable e) {
            log.error("CashDetailLogBizImpl cash gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        return ResponseEntity.ok(voHtmlResp);
    }


    @Override
    public ResponseEntity<VoBankApsWrapResp> bankAps(Long userId, VoBankApsReq voBankApsReq) {
        Users users = userService.findById(userId);
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoBankApsWrapResp.class));
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoBankApsWrapResp.class));
        }


        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoBankApsWrapResp.class));
        }

        Map<String, String> params = new HashMap<>();
        params.put("card", userThirdAccount.getCardNo());
        params.put("page", voBankApsReq.getPage().toString());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("APPCODE %s", aliyunQueryAppcode));
        String jsonStr = null;
        try {
            jsonStr = OKHttpHelper.get(aliyunQueryBankapsUrl, params, headers);
        } catch (Throwable e) {
            log.error("CashDetailLogBizImpl.bankAps   银行联行查询异常");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络异常,请稍后重试", VoBankApsWrapResp.class));
        }
        JsonParser jsonParser = new JsonParser();
        JsonElement root = jsonParser.parse(jsonStr);
        JsonObject rootObject = root.getAsJsonObject();
        JsonObject resp = rootObject.getAsJsonObject("resp");
        String respCode = resp.get("RespCode").getAsString();
        if (!respCode.equals("200")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, resp.get("RespMsg").getAsString(), VoBankApsWrapResp.class));
        }
        JsonObject data = rootObject.getAsJsonObject("data");
        VoBankApsWrapResp voBankApsWrapResp = GSON.fromJson(data, VoBankApsWrapResp.class);
        voBankApsWrapResp.setState(new VoBaseResp.State(VoBaseResp.OK, "查询成功", DateHelper.dateToString(new Date())));
        return ResponseEntity.ok(voBankApsWrapResp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> cashCallback(HttpServletRequest request) throws Exception {
        WithDrawResponse response = jixinManager.callback(request, new TypeToken<WithDrawResponse>() {
        });
        if (ObjectUtils.isEmpty(response)) {
            log.error("CashDetailDizImpl.cashCallback: 网络异常");
            return ResponseEntity.ok("success");
        }

        Long userId = Long.parseLong(response.getAcqRes());
        if ((ObjectUtils.isEmpty(userId)) || (userId <= 0)) {
            log.error(String.format("CashDetailDizImpl.cashCallback: userId %s", response.getAcqRes()));
            return ResponseEntity.ok("success");
        }

        Users users = userService.findByIdLock(userId);
        if (ObjectUtils.isEmpty(users)) {
            log.error("CashDetailDizImpl.cashCallback: 用户不存在");
            return ResponseEntity.ok("success");
        }

        String seqNo = response.getTxDate() + response.getTxTime() + response.getSeqNo();  // 交易流水
        CashDetailLog cashDetailLog = cashDetailLogService.findTopBySeqNoLock(seqNo);
        if (ObjectUtils.isEmpty(cashDetailLog)) {
            log.error("CashDetailDizImpl.cashCallback: 交易记录不存在");
            return ResponseEntity.ok("success");
        }
        if (cashDetailLog.getState() == 3) {
            log.error("CashDetailDizImpl.cashCallback: 提现成功重复调用");
            return ResponseEntity.ok("success");
        }

        String titel = "";
        String content = "";
        Date nowDate = new Date();
        if ((JixinResultContants.SUCCESS.equals(response.getRetCode()))) { // 交易成功
            // 更改用户提现记录
            cashDetailLog.setState(3);
            cashDetailLog.setCallbackTime(nowDate);
            cashDetailLogService.save(cashDetailLog);
            // 更改用户资金
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Cash);
            entity.setMoney(cashDetailLog.getMoney().intValue());
            entity.setUserId(userId);
            entity.setToUserId(userId);
            capitalChangeHelper.capitalChange(entity);
            log.info(String.format("提现成功: 交易流水: %s 返回状态/信息: %s/%s", seqNo, response.getRetCode(), response.getRetMsg()));

            titel = "提现成功";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 处理成功! 如有疑问请致电客服.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
        } else if ((JixinResultContants.CASH_RETRY.equals(response.getRetCode()))) {
            // 此处考虑到资金安全, 我们使用 先扣除用户可用余额, 在由调取 5分钟 查询一次是是否提现成功或者失败
            cashDetailLog.setState(2); // 等待结果
            cashDetailLog.setCallbackTime(nowDate);
            cashDetailLog.setCancelTime(jixinTxDateHelper.getTxDate());
            cashDetailLogService.save(cashDetailLog);
            // 5 分钟查询, 总共查询  2个小时
            TaskScheduler taskScheduler = new TaskScheduler();
            taskScheduler.setCreateAt(new Date());
            taskScheduler.setUpdateAt(new Date());
            taskScheduler.setType(TaskSchedulerConstants.CASH_FORM);
            Map<String, String> data = new HashMap<>(1);
            data.put("cashId", cashDetailLog.getId().toString());
            Gson gson = new Gson();
            taskScheduler.setTaskData(gson.toJson(data));
            taskScheduler.setTaskNum(24);
            taskScheduler = taskSchedulerBiz.save(taskScheduler);
            if (ObjectUtils.isEmpty(taskScheduler.getId())) {
                log.error(String.format("添加大额提现查询失败 %s", gson.toJson(data)));
            }
        } else {  // 交易失败
            titel = "提现失败";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 处理失败! 如有疑问请致电客服.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(cashDetailLog.getMoney(), true));
            log.info(String.format("处理提现失败: 交易流水: %s 返回状态/信息: %s/%s", seqNo, response.getRetCode(), response.getRetMsg()));
            cashDetailLog.setState(4);
            cashDetailLog.setCallbackTime(new Date());
            cashDetailLog.setVerifyRemark(response.getRetMsg());
            cashDetailLogService.save(cashDetailLog);
        }

        try {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userId);
            notices.setRead(false);
            notices.setName(titel);
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            log.info(String.format("CashDetailLogBizImpl cashCallback send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("CashDetailLogBizImpl cashCallback send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }


    @Override
    public ResponseEntity<VoCashLogWrapResp> log(Long userId, int pageIndex, int pageSize) {
        pageIndex = pageIndex < 0 ? 0 : pageIndex;
        pageSize = pageSize < 0 ? 10 : pageSize;

        Pageable page = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        List<CashDetailLog> logs = cashDetailLogService.findByUserIdAndPage(userId, page);

        VoCashLogWrapResp respone = VoBaseResp.ok("查询成功", VoCashLogWrapResp.class);

        logs.forEach(bean -> {
            VoCashLogResp voCashLogResp = new VoCashLogResp();
            voCashLogResp.setBankNameAndCardNo(String.format("%s(%s)", bean.getBankName(), bean.getCardNo().substring(bean.getCardNo().length() - 4)));
            voCashLogResp.setCashMoney(StringHelper.formatDouble(bean.getMoney() / 100D, true));
            voCashLogResp.setCreateTime(DateHelper.dateToString(bean.getCreateTime()));
            voCashLogResp.setState(bean.getState() == 1 ? 0 : bean.getState() == 3 ? 1 : 2);
            voCashLogResp.setMsg(bean.getState() == 1 ? "等待银行处理,请耐心等候!" : bean.getState() == 3 ? "提现成功!" : "提现失败!");
            voCashLogResp.setId(bean.getId());
            respone.getData().add(voCashLogResp);
        });

        return ResponseEntity.ok(respone);
    }

    @Override
    public ResponseEntity<VoCashLogDetailResp> logDetail(Long id, Long userId) {
        CashDetailLog cashDetailLog = cashDetailLogService.findById(id);
        if (ObjectUtils.isEmpty(cashDetailLog) && cashDetailLog.getUserId() != userId) {
            log.error("CashDetailLogBizImpl.logDetail 查询用户提现记录不存在!");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoCashLogDetailResp.class));
        }

        VoCashLogDetailResp response = VoBaseResp.ok("查询成功", VoCashLogDetailResp.class);
        response.setBankNameAndCardNo(String.format("%s(%s)", cashDetailLog.getBankName(), cashDetailLog.getCardNo().substring(cashDetailLog.getCardNo().length() - 4)));
        response.setBankProcessTime(DateHelper.dateToString(cashDetailLog.getVerifyTime()));
        response.setCashMoney(StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
        response.setCashTime(DateHelper.dateToString(cashDetailLog.getCreateTime()));
        response.setFee(StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true));
        response.setRealCashMoney(StringHelper.formatDouble((cashDetailLog.getMoney() - cashDetailLog.getFee()) / 100D, true));
        Date cashTime = cashDetailLog.getState() == 4 ? DateHelper.addHours(cashDetailLog.getCreateTime(), 2) : cashDetailLog.getCallbackTime();
        response.setRealCashTime(DateHelper.dateToString(cashTime));


        // 状态
        Integer state = cashDetailLog.getState();

        String stateMsg = null;
        if (1 == state) {
            stateMsg = "提现申请已取消";
        } else if (1 == state) {
            stateMsg = "系统审核通过";
        } else if (2 == state) {
            stateMsg = "系统审核不同通过, 如有问题请联系客服";
        } else if (3 == state) {
            stateMsg = "提现成功";
        } else {
            stateMsg = "提现失败";
        }
        response.setStatus(state);
        response.setStatusMsg(stateMsg);

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String showCash(String seqNo, Model model) {
        CashDetailLog cashDetailLog = cashDetailLogService.findTopBySeqNoLock(seqNo);
        model.addAttribute("h5Domain", h5Domain);
        if (ObjectUtils.isEmpty(cashDetailLog)) {
            return "cash/faile";
        }
        if (cashDetailLog.getState().equals(1)) {
            return "cash/loading";
        } else if (cashDetailLog.getState().equals(3)) {
            return "cash/success";
        } else {
            return "cash/faile";
        }
    }

    @Override
    public ResponseEntity<VoCashLogWarpRes> psLogs(VoPcCashLogs cashLogs) {
        try {
            List<VoCashLog> logList = cashDetailLogService.pcLogs(cashLogs);
            VoCashLogWarpRes warpRes = VoBaseResp.ok("查询成功", VoCashLogWarpRes.class);
            warpRes.setTotalCount(0);
            if (!CollectionUtils.isEmpty(logList)) {
                warpRes.setTotalCount(logList.get(0).getTotalCount());
                logList.get(0).setTotalCount(null);
            }
            warpRes.setLogs(logList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询异常",
                            VoCashLogWarpRes.class));
        }
    }


    @Override
    public void toExcel(VoPcCashLogs cashLogs, HttpServletResponse response) {
        List<VoCashLog> logList = cashDetailLogService.pcLogs(cashLogs);
        if (!CollectionUtils.isEmpty(logList)) {
            LinkedHashMap<String, String> paramMaps = Maps.newLinkedHashMap();
            paramMaps.put("createTime", "时间");
            paramMaps.put("banKName", "提现银行");
            paramMaps.put("bankNo", "提现账号");
            paramMaps.put("money", "提现金额");
            paramMaps.put("serviceCharge", "手续费");
            try {
                ExcelUtil.listToExcel(logList, paramMaps, "资金流水", response);
            } catch (ExcelException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean doFormCashMoney(Long cashId, Integer curNum, Integer totalNum) throws Exception {
        // 查询当前标的信息
        CashDetailLog cashDetailLog = cashDetailLogService.findById(cashId);
        if (ObjectUtils.isEmpty(cashDetailLog)) {
            log.error(String.format("大额提现调度: 提现记录为空 %s", cashId));
            return false;
        }

        if (cashDetailLog.getState() != 2) {
            log.error(String.format("大额提现调度: 提现状态已经改变 %s", GSON.toJson(cashDetailLog)));
            return true;
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(cashDetailLog.getUserId());
        // 查询银行交易流水
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
            log.error(String.format("大额提现调度: %s", msg));
            return false;
        }

        double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100.0;
        double currBal = NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100.0;
        String tranType = cashDetailLog.getCashType() == 1 ? "2820" : "2616";  // 查询提现类型
        // 查询用户操作记录
        int pageSize = 20, pageIndex = 1, realSize = 0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        Date queryDate = ObjectUtils.isEmpty(cashDetailLog.getCancelTime()) ? jixinTxDateHelper.getTxDate() : cashDetailLog.getCancelTime();
        String queryDateStr = DateHelper.dateToString(queryDate, DateHelper.DATE_FORMAT_YMD_NUM);
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(queryDateStr);
            accountDetailsQueryRequest.setEndDate(queryDateStr);
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setTranType(tranType);  // 大额提现
            accountDetailsQueryRequest.setAccountId(accountId);
            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                log.error(String.format("资金同步: %s", msg));
                return false;
            }

            String subPacks = accountDetailsQueryResponse.getSubPacks();
            if (StringUtils.isEmpty(subPacks)) {
                break;
            }

            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
        } while (realSize == pageSize);
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.error(String.format("大额提现调度: 查询存管系统信息为空 %s", GSON.toJson(cashDetailLog)));
            return false;
        }

        Collections.reverse(accountDetailsQueryItemList);  // 反序

        // 查询当天提现记录
        ImmutableList<Integer> stateList = ImmutableList.of(2, 3);
        Date startDate = DateHelper.beginOfDate(cashDetailLog.getCreateTime());
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(cashDetailLog.getCreateTime(), 1));
        List<CashDetailLog> cashDetailLogList = cashDetailLogService.findByUserIdAndStateInAndCreateTimeBetween(cashDetailLog.getUserId(), stateList, startDate, endDate);
        List<CashDetailLog> unMathLogList = new ArrayList<>();
        for (CashDetailLog item : cashDetailLogList) {  // 去除成功的记录
            if (item.getState() == 3) { // 成功,日期最前的剔除掉
                double money = (item.getMoney() - item.getFee()) / 100D;
                int i = 0;
                boolean suitabilityState = false;// 匹配状态
                for (int len = accountDetailsQueryItemList.size(); i < len; i++) {
                    double queryMoney = new Double(accountDetailsQueryItemList.get(i).getTxAmount());
                    if (queryMoney == money) {
                        suitabilityState = true;
                        break;
                    }
                }
                if (suitabilityState) {
                    accountDetailsQueryItemList.remove(i);  // 删除匹配中的记录
                }
            } else {
                unMathLogList.add(item);
            }
        }

        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)
                || CollectionUtils.isEmpty(unMathLogList)) {
            return false;
        }


        for (AccountDetailsQueryItem item : accountDetailsQueryItemList) {
            double queryMoney = new Double(item.getTxAmount());
            int i = 0;
            boolean suitabilityState = false;// 匹配状态
            for (int len = unMathLogList.size(); i < len; i++) {
                double money = (unMathLogList.get(i).getMoney() - unMathLogList.get(i).getFee()) / 100D;
                if (money == queryMoney) {
                    suitabilityState = true ;
                    break;
                }
            }

            if(suitabilityState){
                Date nowDate = new Date() ;
                CashDetailLog remove = unMathLogList.remove(i);
                remove.setCancelTime(null);
                // 更改用户提现记录
                remove.setState(3);
                remove.setCallbackTime(nowDate);
                cashDetailLogService.save(remove);
                // 更改用户资金
                long userId = remove.getUserId() ;
                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.Cash);
                entity.setMoney(cashDetailLog.getMoney().intValue());
                entity.setUserId(userId);
                entity.setToUserId(userId);
                capitalChangeHelper.capitalChange(entity);
                String titel = "提现成功";
                String content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 处理成功! 如有疑问请致电客服.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                        StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
                try {
                    Notices notices = new Notices();
                    notices.setFromUserId(1L);
                    notices.setUserId(userId);
                    notices.setRead(false);
                    notices.setName(titel);
                    notices.setContent(content);
                    notices.setType("system");
                    notices.setCreatedAt(nowDate);
                    notices.setUpdatedAt(nowDate);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
                    mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
                    Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
                    mqConfig.setMsg(body);
                    log.info(String.format("CashDetailLogBizImpl doFormCash send mq %s", GSON.toJson(body)));
                    mqHelper.convertAndSend(mqConfig);
                } catch (Throwable e) {
                    log.error("CashDetailLogBizImpl doFormCash send mq exception", e);
                }

                if(remove.getId() == cashId){
                    log.info("大额提现提现已确定");
                    return true ;
                }
            }
        }
        return false;
    }

    @Override
    public ResponseEntity<VoHtmlResp> adminWebCash(HttpServletRequest httpServletRequest, VoAdminCashReq voAdminCashReq) throws Exception {
        String paramStr = voAdminCashReq.getParamStr();
        if (!SecurityHelper.checkSign(voAdminCashReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc 登记官方借款 签名验证不通过", VoHtmlResp.class));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long userId = Long.parseLong(paramMap.get("userId"));
        double money = Double.parseDouble(paramMap.get("money"));
        VoCashReq voCashReq = new VoCashReq();
        voCashReq.setCashMoney(money);
        return cash(httpServletRequest, userId, voCashReq);
    }


    /**
     * 获取提现金额
     * <p>
     * 1、如果有未还借款，则 可提现额 = 净值额度 - 正在申请、处理中的提现总额 和 账户可用金额 两者取小值
     * 2、如果没有未还借款，则 可提现额 = 账户可用金额
     *
     * @param userId
     * @return
     */
    private Long getRealCashMoney(Long userId) {
        Asset asset = assetService.findByUserIdLock(userId);
        ImmutableList<Integer> stateList = ImmutableList.of(0, 1);
        List<CashDetailLog> cashDetailLogs = cashDetailLogService.findByStateInAndUserId(stateList, userId);
        long cashingMoney = cashDetailLogs.stream().mapToLong(p -> p.getMoney()).sum();  // 正在提现金额
        Double money = Math.min((((asset.getUseMoney() + asset.getCollection()) * 0.8 - asset.getPayment()) - cashingMoney), asset.getUseMoney());
        money = ObjectUtils.isEmpty(money) ? 0 : money;
        return asset.getPayment() > 0 ? money.longValue() : asset.getUseMoney();
    }
}
