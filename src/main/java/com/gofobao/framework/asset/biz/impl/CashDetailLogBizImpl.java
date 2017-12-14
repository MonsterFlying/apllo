package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Item;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Request;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Response;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.with_daw.WithDrawRequest;
import com.gofobao.framework.api.model.with_daw.WithDrawResponse;
import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.request.VoAdminCashReq;
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.VoCashLog;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
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
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
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
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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

import static com.gofobao.framework.core.vo.VoBaseResp.ERROR_BIND_BANK_CARD;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Component
@Slf4j
public class CashDetailLogBizImpl implements CashDetailLogBiz {

    @Autowired
    RechargeAndCashAndRedpackQueryHelper rechargeAndCashAndRedpackQueryHelper;
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
    BankAccountBizImpl bankAccountBiz;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    UserHelper userHelper;

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

    @Value("${gofobao.pcDomain}")
    private String pcDomain;

    @Autowired
    NewAssetLogService newAssetLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoPreCashResp> preCash(Long userId, HttpServletRequest httpServletRequest) {
        //同步资金
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客服！", VoPreCashResp.class));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (StringUtils.isEmpty(userThirdAccount.getCardNo())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(ERROR_BIND_BANK_CARD, "对不起,您的账号还未绑定银行卡", VoPreCashResp.class));
        }
        ResponseEntity<VoBaseResp> checkResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!checkResponse.getStatusCode().equals(HttpStatus.OK)) {
            VoPreCashResp voPreCashResp = VoBaseResp.error(checkResponse.getBody().getState().getCode(), checkResponse.getBody().getState().getMsg(), VoPreCashResp.class);
            return ResponseEntity.badRequest().body(voPreCashResp);
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

/*
        if (!balanceQueryResponse.getWithdrawFlag().equals("1")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前存管账户被银行禁止提现, 如有问题请联系广富宝金服客服!", VoPreCashResp.class));
        }*/

        // 判断当前用户资金是否一直
        Asset asset = assetService.findByUserIdLock(userId);
        // 本地资金大于存管资金
        Long currBal = new Double(new Double(balanceQueryResponse.getCurrBal()) * 100).longValue();
      /*  if (currBal < asset.getNoUseMoney() + asset.getUseMoney()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前客户本地金额大于存管金额,请联系客服吧", VoPreCashResp.class));
        }*/

        Long realCashMoney = getRealCashMoney(userId);
        VoPreCashResp resp = VoBaseResp.ok("查询成功", VoPreCashResp.class);
        resp.setBankName(userThirdAccount.getBankName());
        resp.setLogo(String.format("%s/%s", javaDomain, userThirdAccount.getBankLogo()));
        resp.setCardNo(userThirdAccount.getCardNo().substring(userThirdAccount.getCardNo().length() - 4));
        resp.setUseMoneyShow(StringHelper.formatDouble(realCashMoney / 100D, true));
        resp.setUseMoney(realCashMoney / 100D);
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
        ImmutableList<Integer> states = ImmutableList.of(0, 1, 3);  // 提现
        Date nowDate = new Date();
        Date beginDate = DateHelper.endOfDate(DateHelper.endOfMonth(DateHelper.subMonths(nowDate, 1)));
        Date endDate = DateHelper.beginOfDate(DateHelper.beginOfMonth(DateHelper.addMonths(nowDate, 1)));


        // 加入日期
        Specification<CashDetailLog> cashDetailLogSpecification = Specifications
                .<CashDetailLog>and()
                .eq("userId", userId)
                .in("state", states.toArray())
                .between("createTime", new Range<>(beginDate, endDate))
                .build();

        Long count = cashDetailLogService.count(cashDetailLogSpecification);
        int cashCount = count.intValue();
        // List<CashDetailLog> cashDetailLogs = cashDetailLogService.findByStateInAndUserId(states, userId);
        return 10 - cashCount <= 0 ? 0 : 10 - cashCount;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest, Long userId, VoCashReq voCashReq) throws Exception {
        //同步资金
        // assetBiz.synOffLineRecharge(userId);
        //当前用户
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
        if (StringUtils.isEmpty(userThirdAccount.getCardNo())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_BIND_BANK_CARD, "对不起!你的账号还未绑定银行卡号", VoHtmlResp.class));
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
        long userCashMoney = NumberHelper.toLong(voCashReq.getCashMoney() * 100);
        if (useMoney < userCashMoney) {
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

        double cashMoney = MoneyHelper.round(MoneyHelper.multiply(voCashReq.getCashMoney(), 100), 0);  // 提现金额
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
        withDrawRequest.setTxDate(DateHelper.getDateFor24());
        withDrawRequest.setIdType(IdTypeContant.getIdTypeContant(userThirdAccount));
        withDrawRequest.setIdNo(userThirdAccount.getIdNo());
        withDrawRequest.setName(StringUtils.trimAllWhitespace(userThirdAccount.getName()));
        withDrawRequest.setMobile(userThirdAccount.getMobile());
        withDrawRequest.setCardNo(userThirdAccount.getCardNo());
        withDrawRequest.setAccountId(userThirdAccount.getAccountId());
        withDrawRequest.setTxAmount(StringHelper.formatDouble(new Double(MoneyHelper.divide((cashMoney - fee), 100D)), false)); //  交易金额
        String routeCode = " ";
        if (bigCashState) {
            routeCode = "2";
            withDrawRequest.setCardBankCnaps(voCashReq.getBankAps()); // 联行卡号
        } else {
            if (userThirdAccount.getIdType() > 1) {
                // 313421080308  江西银行联行号
                routeCode = "2";
                withDrawRequest.setCardBankCnaps("313421080308"); // 这里正对于江西银行企业账号, 直接写死
            }
        }
        withDrawRequest.setRouteCode(routeCode);

        withDrawRequest.setTxFee(StringHelper.formatDouble(new Double(MoneyHelper.divide(fee, 100D)), false));
        withDrawRequest.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        String requestSourceStr = httpServletRequest.getHeader("requestSource");
        if (StringUtils.isEmpty(requestSourceStr)) {
            requestSourceStr = "-1";
        }
        Integer requestSource = Integer.valueOf(requestSourceStr);
        if (requestSource == 0) {
            withDrawRequest.setRetUrl(String.format("%s/%s", pcDomain, "account/cash"));
        } else {
            withDrawRequest.setRetUrl(String.format("%s/%s/%s", javaDomain, "pub/cash/show/", withDrawRequest.getTxDate() + withDrawRequest.getTxTime() + withDrawRequest.getSeqNo()));
        }

        withDrawRequest.setNotifyUrl(String.format("%s/%s", javaDomain, "pub/asset/cash/callback"));
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
            cashDetailLog.setCashType(bigCashState ? 1 : 0); // 现在提现都走了超网渠道, 所有类型都2820
            if (bigCashState) {
                cashDetailLog.setCompanyBankNo(voCashReq.getBankAps()); // 联行卡号
            }
            cashDetailLog.setFee(fee);
            cashDetailLog.setCreateTime(nowDate);
            cashDetailLog.setMoney(new Double(cashMoney).longValue());
            cashDetailLog.setSeqNo(withDrawRequest.getTxDate() + withDrawRequest.getTxTime() + withDrawRequest.getSeqNo());
            cashDetailLog.setState(4);  // 直接设置为失败
            cashDetailLog.setIp(httpServletRequest.getLocalAddr());
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
            userAssetCash(users, seqNo, cashDetailLog, nowDate);  // 用户提现资金扣减
            titel = "提现成功";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 处理成功! 如有疑问请致电客服.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(MoneyHelper.divide(cashDetailLog.getMoney(), 100D), true));
        } else if (JixinResultContants.toBeConfirm(response)) {
            userAssetCash(users, seqNo, cashDetailLog, nowDate);  // 用户提现资金扣减
            titel = "存管已接收提现受理";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 已被银行受理, 你可以2小时后查看结果. 如有疑问, 请联系平台客服!.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(MoneyHelper.divide(cashDetailLog.getMoney(), 100D), true));

            // 发送资金确认
            rechargeAndCashAndRedpackQueryHelper.save(RechargeAndCashAndRedpackQueryHelper.QueryType.QUERY_CASH, userId, cashDetailLog.getId(), true);
        } else {
            cashDetailLog.setState(4);
            cashDetailLog.setCallbackTime(new Date());
            cashDetailLog.setVerifyRemark(response.getRetCode());
            cashDetailLogService.save(cashDetailLog);

            titel = "提现失败";
            content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 处理失败! 如有疑问请致电客服.", DateHelper.dateToString(cashDetailLog.getCreateTime()),
                    StringHelper.formatDouble(cashDetailLog.getMoney(), true));
            log.info(String.format("处理提现失败: 交易流水: %s 返回状态/信息: %s/%s", seqNo, response.getRetCode(), response.getRetMsg()));
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

    /**
     * 提成成功, 执行用户资金扣减
     *
     * @param users         用户
     * @param seqNo         交易流水
     * @param cashDetailLog 提现记录
     * @param nowDate       时间
     * @throws Exception
     */
    private void userAssetCash(Users users, String seqNo, CashDetailLog cashDetailLog, Date nowDate) throws Exception {
        cashDetailLog.setState(3);
        cashDetailLog.setCallbackTime(nowDate);
        cashDetailLogService.save(cashDetailLog);

        // 更改用户资金
        AssetChange entity = new AssetChange();
        long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(realCashMoney);
        entity.setSeqNo(seqNo);
        entity.setUserId(users.getId());
        entity.setRemark(String.format("你在 %s 成功提现%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(realCashMoney / 100D, true)));
        entity.setSourceId(cashDetailLog.getId());
        if (cashDetailLog.getCashType() == 0) { // 小额提现
            entity.setType(AssetChangeTypeEnum.smallCash);
        } else {
            entity.setType(AssetChangeTypeEnum.bigCash);
        }

        assetChangeProvider.commonAssetChange(entity);
        if (cashDetailLog.getFee() > 0) {
            // 扣除用户提现手续费
            Long feeAccountId = assetChangeProvider.getFeeAccountId();
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(seqNo);
            entity.setUserId(users.getId());
            entity.setForUserId(feeAccountId);
            entity.setRemark(String.format("你在 %s 成功扣除提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
            entity.setSourceId(cashDetailLog.getId());
            if (cashDetailLog.getCashType() == 0) { // 小额提现
                entity.setType(AssetChangeTypeEnum.smallCashFee);
            } else {
                entity.setType(AssetChangeTypeEnum.bigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);

            // 平台收取提现手续费
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(seqNo);
            entity.setUserId(feeAccountId);
            entity.setForUserId(users.getId());
            entity.setRemark(String.format("你在 %s 成功收取提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
            entity.setSourceId(cashDetailLog.getId());
            if (cashDetailLog.getCashType() == 0) {
                entity.setType(AssetChangeTypeEnum.platformSmallCashFee);
            } else {
                entity.setType(AssetChangeTypeEnum.platformBigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);
        }
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

        if (ObjectUtils.isEmpty(cashDetailLog.getCallbackTime())) {
            return "cash/loading";
        }

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

    /**
     * 搜索用户资金变动特定类型
     *
     * @param cashDetailLog
     * @param userThirdAccount
     * @return
     */
    private List<AccountDetailsQuery2Item> findUserAssetChangeLog(CashDetailLog cashDetailLog, UserThirdAccount userThirdAccount) {
        List<AccountDetailsQuery2Item> accountDetailsQueryItemList = new ArrayList<>();
        // 查询用户操作记录
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        String rtnInd = "";
        String inpDate = "";
        String queryDate = StringUtils.isEmpty(cashDetailLog.getQuerySeqNo()) ? jixinTxDateHelper.getTxDateStr() : cashDetailLog.getQuerySeqNo();
        do {
            AccountDetailsQuery2Request accountDetailsQuery2Request = new AccountDetailsQuery2Request();
            accountDetailsQuery2Request.setRtnInd(rtnInd);
            accountDetailsQuery2Request.setInpDate(inpDate);
            accountDetailsQuery2Request.setStartDate(queryDate);
            accountDetailsQuery2Request.setEndDate(queryDate);
            accountDetailsQuery2Request.setType("0");
            accountDetailsQuery2Request.setAccountId(accountId);
            AccountDetailsQuery2Response accountDetailsQuery2Response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY2,
                    accountDetailsQuery2Request,
                    AccountDetailsQuery2Response.class);

            if ((ObjectUtils.isEmpty(accountDetailsQuery2Response)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQuery2Response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQuery2Response) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQuery2Response.getRetMsg();
                log.error(String.format("资金同步: %s", msg));
                return Collections.EMPTY_LIST;
            }

            String subPacks = accountDetailsQuery2Response.getSubPacks();
            if (StringUtils.isEmpty(subPacks)) {
                break;
            }

            Optional<List<AccountDetailsQuery2Item>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQuery2Response.getSubPacks(), new TypeToken<List<AccountDetailsQuery2Item>>() {
            }.getType()));
            List<AccountDetailsQuery2Item> accountDetailsQuery2Items = optional.orElse(Lists.newArrayList());

            inpDate = accountDetailsQuery2Items.get(accountDetailsQuery2Items.size() - 1).getInpDate();

            accountDetailsQueryItemList.addAll(accountDetailsQuery2Items);
            rtnInd = "1";
        } while (!CollectionUtils.isEmpty(accountDetailsQueryItemList));
        if (CollectionUtils.isEmpty(accountDetailsQueryItemList)) {
            log.error(String.format("大额提现调度: 查询存管系统信息为空 %s", GSON.toJson(cashDetailLog)));
            return Collections.EMPTY_LIST;
        }

        Collections.reverse(accountDetailsQueryItemList);  // 反序
        return accountDetailsQueryItemList;
    }


    /* private void deductionAsset(CashDetailLog cashDetailLog, AccountDetailsQueryItem cash) throws Exception {
         Date nowDate = new Date();
         String querySeqNo = String.format("%s%s%s", cash.getInpDate(), cash.getInpTime(), cash.getTraceNo());
         // 更改用户提现记录
         cashDetailLog.setState(4);
         cashDetailLog.setCallbackTime(nowDate);
         cashDetailLog.setQuerySeqNo(querySeqNo);
         cashDetailLogService.save(cashDetailLog);

         String seqNo = cashDetailLog.getSeqNo();
         long userId = cashDetailLog.getUserId();

         // 更改用户资金
         AssetChange entity = new AssetChange();
         long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
         String groupSeqNo = assetChangeProvider.getGroupSeqNo();
         entity.setGroupSeqNo(groupSeqNo);
         entity.setMoney(realCashMoney);
         entity.setSeqNo(seqNo);
         entity.setUserId(userId);
         entity.setRemark(String.format("你在 %s 成功返还提现%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(realCashMoney / 100D, true)));
         entity.setType(AssetChangeTypeEnum.cancelCash);
         assetChangeProvider.commonAssetChange(entity);
         if (cashDetailLog.getFee() > 0) {   // 扣除用户提现手续费
             Long feeAccountId = assetChangeProvider.getFeeAccountId();
             entity = new AssetChange();
             entity.setGroupSeqNo(groupSeqNo);
             entity.setMoney(cashDetailLog.getFee());
             entity.setSeqNo(seqNo);
             entity.setUserId(userId);
             entity.setForUserId(feeAccountId);
             entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
             entity.setType(AssetChangeTypeEnum.cancelCashFee);
             assetChangeProvider.commonAssetChange(entity);

             // 平台收取提现手续费
             entity = new AssetChange();
             entity.setGroupSeqNo(groupSeqNo);
             entity.setMoney(cashDetailLog.getFee());
             entity.setSeqNo(seqNo);
             entity.setUserId(feeAccountId);
             entity.setForUserId(userId);
             entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate), StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
             entity.setType(AssetChangeTypeEnum.cancelPlatformCashFee);
             assetChangeProvider.commonAssetChange(entity);
         }

         String titel = "提现失败";
         String content = String.format("敬爱的用户您好! 你在[%s]提交%s元的提现请求, 由于已被银行拒绝受理, 现在归还提现资金. 如有疑问联系平台客服!", DateHelper.dateToString(cashDetailLog.getCreateTime()),
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
     }
 */
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


    @Autowired
    private TenderService tenderService;

    @Autowired
    private MarketingRedpackRecordService marketingRedpackRecordService;

    /**
     * 获取提现金额
     * <p>
     * 1、如果有未还借款，则 可提现额 = 信用额度 - 正在申请、处理中的提现总额 和 账户可用金额 两者取小值
     * 2、如果没有未还借款，则 可提现额 = 账户可用金额
     * </p>
     * 注意此处增加防作弊规则
     * 遇过用户邀请好友频繁(近三十天超过 80个)
     * 可提现金额会减去红包金额
     *
     * @param userId
     * @return
     */
    private Long getRealCashMoney(Long userId) {
        Asset asset = assetService.findByUserIdLock(userId);
        UserCache userCache = userCacheService.findByUserIdLock(userId);
        Double money = 0D;
        if (asset.getPayment() > 0) {
            long netWorthQuota = userHelper.getNetWorthQuota(userId);
            money = Math.min(new Double(netWorthQuota), asset.getUseMoney());
        } else {
            money = asset.getUseMoney() * 1D;
        }

        Specification<Tender> tenderSpecification = Specifications
                .<Tender>and()
                .eq("userId", userId)
                .eq("status", 1)
                .build();
        // 存在投标, 直接返回
        long tenderCount = tenderService.count(tenderSpecification);
        if (tenderCount > 0) {
            return money.longValue();
        }
        log.warn("当前用户为投资提现");
        // 30 天红包数量大于 20个
        try {
            Date nowDate = new Date();
            Date beginDate = DateHelper.subDays(nowDate, 30);
            Specification<MarketingRedpackRecord> marketingRedpackRecordSpecification = Specifications
                    .<MarketingRedpackRecord>and()
                    .eq("userId", userId)
                    .between("publishTime", new Range(beginDate, nowDate))
                    .build();
            long redpackCount = marketingRedpackRecordService.count(marketingRedpackRecordSpecification);
            if (redpackCount < 20) {
                log.info("未进入作弊系统");
                return money.longValue();
            }

            long redPackMoney = marketingRedpackRecordService.countByUserIdAndDate(userId, beginDate, nowDate);
            log.warn("领取红包金额:" + redPackMoney);
            long cahsMoney = money.longValue() - redPackMoney;
            // 提现金额
            return cahsMoney < 0 ? 0 : cahsMoney;
        } catch (Exception e) {
            log.error("提现防作弊代码异常", e);
            return money.longValue();
        }

    }
}
