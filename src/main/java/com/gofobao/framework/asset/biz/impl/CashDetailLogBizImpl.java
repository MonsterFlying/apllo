package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
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
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.ThirdAccountPasswordHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Component
@Slf4j
public class CashDetailLogBizImpl implements CashDetailLogBiz {
    @Autowired
    AssetService assetService;

    @Autowired
    UserService userService ;

    @Autowired
    CashDetailLogService cashDetailLogService ;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService ;

    @Autowired
    JixinManager jixinManager ;

    @Autowired
    CapitalChangeHelper capitalChangeHelper ;

    @Autowired
    BankAccountBizImpl bankAccountBiz ;

    @Value("${gofobao.javaDomain}")
    String javaDomain;

    @Value("${gofobao.h5Domain}")
    String h5Domain;


    static final Gson GSON = new Gson() ;

    @Value("${gofobao.aliyun-bankaps-url}")
    String aliyunQueryBankapsUrl ;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode ;

    @Autowired
    ThirdAccountPasswordHelper thirdAccountPasswordHelper ;

    @Autowired
    DictItemServcie dictItemServcie ;

    @Autowired
    DictValueService dictValueService ;


    @Autowired
    TaskSchedulerBiz taskSchedulerBiz ;

    LoadingCache<String, DictValue> bankLimitCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("PLATFORM_BANK", 0) ;
                    if(ObjectUtils.isEmpty(dictItem)){
                        return null ;
                    }

                    return dictValueService.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            }) ;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoPreCashResp> preCash(Long userId, HttpServletRequest httpServletRequest) {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoPreCashResp.class));
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

        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest() ;
        balanceQueryRequest.setChannel(ChannelContant.getchannel(httpServletRequest));
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if(ObjectUtils.isEmpty(balanceQueryResponse)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定,请稍后重试！", VoPreCashResp.class));
        }

        if(!balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不稳定,请稍后重试！", VoPreCashResp.class));
        }


        if(!balanceQueryResponse.getWithdrawFlag().equals("1")){
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
        resp.setUseMoneyShow(StringHelper.formatDouble(asset.getUseMoney() / 100D, true));
        resp.setUseMoney(asset.getUseMoney() / 100D);
        int time = queryFreeTime(userId) ;
        resp.setFreeTime(time);
        return ResponseEntity.ok(resp) ;
    }


    /**
     * 获取免费提现次数
     * @param userId
     * @return
     */
    private int queryFreeTime(Long userId) {
        ImmutableList<Integer> states = ImmutableList.of(0, 1, 3) ;
        List<CashDetailLog> cashDetailLogs = cashDetailLogService.findByStateInAndUserId(states, userId) ;
        return CollectionUtils.isEmpty(cashDetailLogs) ? 10 :  cashDetailLogs.size() > 10 ? 0 : 10 - cashDetailLogs.size() ;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest, Long userId, VoCashReq voCashReq)  throws  Exception{
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "当前用户不存在");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoHtmlResp.class));
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
        Asset asset = assetService.findByUserIdLock(userId);
        Integer useMoney = asset.getUseMoney() ;
        if(useMoney <= 0){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户余额为0！", VoHtmlResp.class));
        }

        if( (voCashReq.getCashMoney() >= 200000) && (StringUtils.isEmpty(voCashReq.getBankAps()))){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "提现金额大于20万,请走人行通道！", VoHtmlResp.class));
        }

        // 判断提现金额
        if(useMoney < voCashReq.getCashMoney() * 100){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "提现金额大于账户可用余额！", VoHtmlResp.class));
        }


        // 判断提现额度剩余
        double[] cashCredit = bankAccountBiz.getCashCredit(userId);
        // 判断单笔额度
        double oneTimes = cashCredit[0];
        if(voCashReq.getCashMoney() > oneTimes){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("%s每笔最大提现额度为%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(oneTimes, true)),
                            VoHtmlResp.class));
        }

        // 判断当天额度
        double dayTimes = cashCredit[1];
        if( (dayTimes <= 0) || (dayTimes - voCashReq.getCashMoney() < 0)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("今天你在%s的剩余提现额度%s",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(dayTimes < 0? 0 : dayTimes, true)),
                            VoHtmlResp.class));
        }

        // 判断每月额度
        double mouthTimes = cashCredit[2];
        if( (mouthTimes <= 0) || (mouthTimes - voCashReq.getCashMoney() < 0)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            String.format("当月你在%s的剩余提现额度%s元",
                                    userThirdAccount.getBankName(),
                                    StringHelper.formatDouble(mouthTimes < 0? 0 : mouthTimes, true)),
                            VoHtmlResp.class));
        }


        boolean bigCashState = false ; // 人行通道?
        if((voCashReq.getCashMoney() >= 200000) && (!StringUtils.isEmpty(voCashReq.getBankAps()))){
            bigCashState = true;
        }

        Date nowDate = new Date() ;

        if(bigCashState){
            Date zeroDate = DateHelper.beginOfDate(nowDate) ;
            Date startDate = DateHelper.addHours(zeroDate, 9)  ;
            Date endDate = DateHelper.addMinutes(zeroDate, 60 * 16 + 45) ;

            if(  (startDate.getTime() > zeroDate.getTime())  || (endDate.getTime() < nowDate.getTime()) ){
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "江西银行的大额度提现通道开放时间:09:00-16:45(仅限工作日),建议进行实时提现,提到提现效率.", VoHtmlResp.class));
            }
        }


        double cashMoney = voCashReq.getCashMoney() * 100;  // 提现金额
        // 免费体现次数
        int freeTime = queryFreeTime(userId) ;
        // 计算提现费用
        long fee = 0L ;
        if(freeTime > 10){  // 收费
            fee = 200L ;
        }

        WithDrawRequest request = new WithDrawRequest() ;
        request.setSeqNo(RandomHelper.generateNumberCode(6));
        request.setTxTime(DateHelper.getTime());
        request.setTxDate(DateHelper.getDate());
        request.setIdType(IdTypeContant.ID_CARD);
        request.setIdNo(users.getCardId());
        request.setName(users.getRealname());
        request.setMobile(userThirdAccount.getMobile());
        request.setCardNo(userThirdAccount.getCardNo());
        request.setAccountId(userThirdAccount.getAccountId());
        request.setTxAmount(StringHelper.formatDouble(new Double((cashMoney - fee) / 100D), false )); //  交易金额
        if(bigCashState){
            request.setRouteCode("2");
            request.setCardBankCnaps(voCashReq.getBankAps());
        }else{
            request.setRouteCode("0");
        }
        if(fee == 0){  // 费用
            request.setTxFee("0");
        }else{
            request.setTxFee( StringHelper.formatDouble( new Double(fee / 100D), false));
        }

        request.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, userId));
        request.setRetUrl(String.format("%s/%s/%s", javaDomain, "/pub/cash/show/", request.getTxDate() + request.getTxTime() + request.getSeqNo() ));
        request.setNotifyUrl(String.format("%s/%s", javaDomain, "/pub/asset/cash/callback"));
        request.setAcqRes(String.valueOf(userId));
        request.setChannel(ChannelContant.getchannel(httpServletRequest));
        // 生成提现表单
        String html = jixinManager.getHtml(JixinTxCodeEnum.WITH_DRAW, request);
        if(StringUtils.isEmpty(html)){
            log.error("CashDetailLogBizImpl.cash 生成表单 ");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }else{
            // 写入提现记录
            CashDetailLog cashDetailLog = new CashDetailLog() ;
            cashDetailLog.setThirdAccountId(userThirdAccount.getAccountId());
            cashDetailLog.setBankName(userThirdAccount.getBankName());
            cashDetailLog.setCardNo(userThirdAccount.getCardNo());
            cashDetailLog.setCashType(bigCashState ? 1 : 0);
            cashDetailLog.setCompanyBankNo(voCashReq.getBankAps());
            cashDetailLog.setFee(fee);
            cashDetailLog.setCreateTime(nowDate);
            cashDetailLog.setMoney(new Double(cashMoney).longValue());
            cashDetailLog.setSeqNo(request.getTxDate() + request.getTxTime() + request.getSeqNo());
            cashDetailLog.setState(1);  //  审核成功
            cashDetailLog.setVerifyTime(nowDate);
            cashDetailLog.setVerifyUserId(0L);
            cashDetailLog.setUserId(userId);
            cashDetailLog.setVerifyRemark("系统自动审核通过");
            cashDetailLogService.save(cashDetailLog) ;
            // 冻结金额
            CapitalChangeEntity entity = new CapitalChangeEntity() ;
            entity.setType(CapitalChangeEnum.Frozen);
            entity.setMoney(new Double(cashMoney).intValue());
            entity.setUserId(userId);
            entity.setToUserId(userId);
            entity.setRemark("提现冻结资金");
            capitalChangeHelper.capitalChange(entity) ;
        }

        VoHtmlResp voHtmlResp = VoBaseResp.ok("成功", VoHtmlResp.class);
        try {
            voHtmlResp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("CashDetailLogBizImpl cash gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoHtmlResp.class)) ;
        }

        return ResponseEntity.ok(voHtmlResp) ;
    }





    @Override
    public ResponseEntity<VoBankApsWrapResp> bankAps(Long userId, VoBankApsReq voBankApsReq) {
        Users users = userService.findById(userId);
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于被冻结状态，如有问题请联系客户！", VoBankApsWrapResp.class));
        }
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
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
        params.put("page", voBankApsReq.getPage().toString() );
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("APPCODE %s", aliyunQueryAppcode));
        String jsonStr = null;
        try {
            jsonStr = OKHttpHelper.get(aliyunQueryBankapsUrl, params, headers);
        } catch (Exception e) {
            log.error("CashDetailLogBizImpl.bankAps   银行联行查询异常");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络异常,请稍后重试", VoBankApsWrapResp.class));
        }
        JsonParser jsonParser = new JsonParser();
        JsonElement root = jsonParser.parse(jsonStr);
        JsonObject rootObject = root.getAsJsonObject();
        JsonObject resp = rootObject.getAsJsonObject("resp");
        String respCode = resp.get("RespCode").getAsString() ;
        if(!respCode.equals("200")){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, resp.get("RespMsg").getAsString(), VoBankApsWrapResp.class));
        }
        JsonObject data = rootObject.getAsJsonObject("data");
        VoBankApsWrapResp voBankApsWrapResp = GSON.fromJson(data, VoBankApsWrapResp.class) ;
        voBankApsWrapResp.setState(new VoBaseResp.State(VoBaseResp.OK, "查询成功" , DateHelper.dateToString(new Date())));
        return ResponseEntity.ok(voBankApsWrapResp) ;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> cashCallback(HttpServletRequest request) throws Exception {
        WithDrawResponse response = jixinManager.callback(request, new TypeToken<WithDrawResponse>(){}) ;
        if (ObjectUtils.isEmpty(response)) {
            log.error("CashDetailDizImpl.cashCallback: 网络异常");
            return ResponseEntity.ok("failed") ;
        }

        Long userId =  Long.parseLong( response.getAcqRes() );
        if( (ObjectUtils.isEmpty(userId)) ||(userId <= 0) ){
            log.error(String.format("CashDetailDizImpl.cashCallback: userId %s", response.getAcqRes()));
            return ResponseEntity.ok("failed") ;
        }

        Users users = userService.findByIdLock(userId);
        if(ObjectUtils.isEmpty(users)){
            log.error("CashDetailDizImpl.cashCallback: 用户不存在") ;
            return ResponseEntity.ok("failed") ;
        }

        String seqNo = response.getTxDate() + response.getTxTime() + response.getSeqNo() ;  // 交易流水
        CashDetailLog  cashDetailLog  = cashDetailLogService.findTopBySeqNoLock(seqNo) ;
        if(ObjectUtils.isEmpty(cashDetailLog)){
            log.error("CashDetailDizImpl.cashCallback: 交易记录不存在") ;
            return ResponseEntity.ok("failed") ;
        }
        if(cashDetailLog.getState() == 3){
            log.error("CashDetailDizImpl.cashCallback: 提现成功重复调用") ;
            return ResponseEntity.ok("success") ;
        }

        if (JixinResultContants.SUCCESS.equals(response.getRetCode())) { // 交易成功
            // 更改用户提现记录
            cashDetailLog.setState(3);
            cashDetailLog.setCallbackTime(new Date());
            cashDetailLogService.save(cashDetailLog);

            // 更改用户资金
            CapitalChangeEntity entity = new CapitalChangeEntity() ;
            entity.setType(CapitalChangeEnum.Cash);
            entity.setMoney(cashDetailLog.getMoney().intValue());
            entity.setUserId(userId);
            entity.setToUserId(userId);
            capitalChangeHelper.capitalChange(entity) ;
            return ResponseEntity.ok("success") ;
        }else if(JixinResultContants.CASH_RETRY.equals(response.getRetCode())){  // 需要调度同步
            log.info("提现需要进一步确认");
            Map<String, String> data = new HashMap<>(2) ;
            data.put("cashId", cashDetailLog.getId().toString()) ;
            data.put("userId", userId.toString()) ;

            TaskScheduler taskScheduler = new TaskScheduler() ;
            taskScheduler.setUpdateAt(new Date());
            taskScheduler.setCreateAt(new Date());
            taskScheduler.setType(TaskSchedulerConstants.CASH_FORM);
            taskScheduler.setTaskNum(Integer.MAX_VALUE - 2);
            taskScheduler.setTaskData( new Gson().toJson(data) );
            taskSchedulerBiz.save(taskScheduler) ;
            return ResponseEntity.ok("success") ;
        }else{  // 交易失败
            log.info(String.format("处理提现失败: 交易流水: %s 返回状态/信息: %s/%s", seqNo, response.getRetCode(), response.getRetMsg()));
            cashDetailLog.setState(4);
            cashDetailLog.setCallbackTime(new Date());
            cashDetailLog.setVerifyRemark(response.getRetMsg());
            cashDetailLogService.save(cashDetailLog) ;
            CapitalChangeEntity entity = new CapitalChangeEntity() ;
            entity.setType(CapitalChangeEnum.Unfrozen);
            entity.setMoney(cashDetailLog.getMoney().intValue());
            entity.setUserId(userId);
            entity.setToUserId(userId);
            capitalChangeHelper.capitalChange(entity) ;  // 借去冻结资金
            return ResponseEntity.ok("success") ;
        }
    }


    @Override
    public ResponseEntity<VoCashLogWrapResp> log(Long userId, int pageIndex, int pageSize) {
        pageIndex = pageIndex < 0 ? 0 : pageIndex ;
        pageSize = pageSize < 0 ?  10 : pageSize;

        Pageable page = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        List<CashDetailLog> logs = cashDetailLogService.findByUserIdAndPage(userId, page) ;

        VoCashLogWrapResp respone = VoBaseResp.ok("查询成功", VoCashLogWrapResp.class);

        logs.forEach(bean -> {
            VoCashLogResp voCashLogResp = new VoCashLogResp() ;
            voCashLogResp.setBankNameAndCardNo(String.format("%s(%s)", bean.getBankName(), bean.getCardNo().substring(bean.getCardNo().length() - 4)));
            voCashLogResp.setCashMoney(StringHelper.formatDouble(bean.getMoney() / 100D, true));
            voCashLogResp.setCreateTime(DateHelper.dateToString(bean.getCreateTime()));
            voCashLogResp.setState(bean.getState() == 1 ? 0 : bean.getState() == 3 ? 1 : 2);
            voCashLogResp.setMsg(bean.getState() == 1 ? "等待银行处理,请耐心等候!" : bean.getState() == 3 ? "提现成功!" : "提现失败!");
            voCashLogResp.setId(bean.getId());
            respone.getData().add(voCashLogResp) ;
        });

        return ResponseEntity.ok(respone);
    }

    @Override
    public ResponseEntity<VoCashLogDetailResp> logDetail(Long id) {
        CashDetailLog cashDetailLog = cashDetailLogService.findById(id) ;
        if(ObjectUtils.isEmpty(cashDetailLog)){
            log.error("CashDetailLogBizImpl.logDetail 查询用户提现记录不存在!");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,  "服务器开小差了， 请稍候重试", VoCashLogDetailResp.class)) ;
        }

        VoCashLogDetailResp response = VoBaseResp.ok("查询成功", VoCashLogDetailResp.class) ;
        response.setBankNameAndCardNo(String.format("%s(%s)", cashDetailLog.getBankName(), cashDetailLog.getCardNo().substring(cashDetailLog.getCardNo().length() - 4)));
        response.setBankProcessTime(DateHelper.dateToString(cashDetailLog.getVerifyTime()));
        response.setCashMoney(StringHelper.formatDouble(cashDetailLog.getMoney() / 100D, true));
        response.setCashTime(DateHelper.dateToString(cashDetailLog.getCreateTime()));
        response.setFee(StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true));
        response.setRealCashMoney(StringHelper.formatDouble( (cashDetailLog.getMoney() - cashDetailLog.getFee()) / 100D, true));
        Date cashTime = null ;
        if(cashDetailLog.getState() == 1){
            cashTime =  DateHelper.addHours(cashDetailLog.getCreateTime() , 2) ;
        }else{
            cashTime = cashDetailLog.getCallbackTime() ;
        }

        response.setRealCashTime(DateHelper.dateToString(cashTime));


        // 状态
        Integer state = cashDetailLog.getState();

        String stateMsg = null;
        if("-1".equals(state)){
            stateMsg = "提现申请已取消"  ;
        }else if("1".equals(state)){
            stateMsg = "系统审核通过" ;
        }else if("2".equals(state)){
            stateMsg = "系统审核不同通过, 如有问题请联系客服" ;
        }else if("3".equals(state)){
            stateMsg = "提现成功" ;
        }else{
            stateMsg = "提现失败" ;
        }
        response.setStatus(state);
        response.setStatusMsg(stateMsg) ;

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String showCash(String seqNo, Model model) {
        CashDetailLog  cashDetailLog  = cashDetailLogService.findTopBySeqNoLock(seqNo) ;
        model.addAttribute("h5Domain", h5Domain) ;
        if(ObjectUtils.isEmpty(cashDetailLog)){
            return "/cash/faile" ;
        }
        if(cashDetailLog.getState().equals(1)){
            return "/cash/loading" ;
        }else if(cashDetailLog.getState().equals(3)){
            return "/cash/success" ;
        }else {
            return "/cash/faile" ;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doBigCashForm(String taskData) throws Exception {
        Map<String, String> dataMap = new Gson().fromJson(taskData, TypeTokenContants.MAP_ALL_STRING_TOKEN) ;
        Long cashId = Long.parseLong(dataMap.get("cashId")) ;
        Long userId = Long.parseLong(dataMap.get("userId")) ;
        Asset asset = assetService.findByUserIdLock(userId) ;
        //查询提现状态
        CashDetailLog cashDetailLog = cashDetailLogService.findById(cashId);
        if(cashDetailLog.getState().equals(3) || cashDetailLog.getState().equals(4)){
            return true ;
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        // 查询存管账户资金状况
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest() ;
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        balanceQueryRequest.setChannel(ChannelContant.HTML) ;
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if(ObjectUtils.isEmpty(balanceQueryResponse) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)){
            log.error("大额提现调度, 查询用户存管金额失败!");
            return false ;
        }
        Double thirdCurrBal = Double.parseDouble(balanceQueryResponse.getCurrBal());   // 存管总额度
        Double thirdAvailbal = Double.parseDouble(balanceQueryResponse.getAvailBal());   // 存管可用余额
        Double cashRealMoney=  (cashDetailLog.getMoney() - cashDetailLog.getFee()) / 100D  ; // 提现金额
        Double useMoney =  asset.getUseMoney() / 100D ;
        Double total = asset.getTotal() / 100D;

        if((!thirdCurrBal.equals(total)) || (!thirdAvailbal.equals(useMoney + cashRealMoney + cashDetailLog.getFee() / 100D))){
            return false ;
        }

        Integer cashType = cashDetailLog.getCashType() ;
        Date createTime = cashDetailLog.getCreateTime() ;
        Date startTime = DateHelper.subHours(createTime, 2);
        Date endTime = DateHelper.addDays(createTime, 2) ;   // T + 2查询时间
        Date nowDate = new Date() ;
        endTime = endTime.getTime() < nowDate.getTime() ? nowDate : endTime ;  // 对查询时间进行优化
        int pageIndex = 1, pageSize = 20 ,realSize = 0;
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
            accountDetailsQueryRequest.setType("9");
            accountDetailsQueryRequest.setEndDate(DateHelper.dateToString(endTime, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQueryRequest.setStartDate(DateHelper.dateToString(startTime, DateHelper.DATE_FORMAT_YMD_NUM));
            accountDetailsQueryRequest.setTranType("1".equals(cashType) ? "2820" : "2616");
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, accountDetailsQueryRequest, AccountDetailsQueryResponse.class);
            Preconditions.checkNotNull(accountDetailsQueryResponse, "查询提现状态异常") ;
            Preconditions.checkArgument(!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()), "查询提现状态异常, 验证不通过");
            Optional< List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType())) ;
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList()) ;
            realSize = accountDetailsQueryItems.size() ;

            for(AccountDetailsQueryItem accountDetailsQueryItem: accountDetailsQueryItems){
                Double txAmount = Double.parseDouble(accountDetailsQueryItem.getTxAmount());  // 交易金额
                if(cashRealMoney.equals(txAmount)){
                    cashDetailLog.setState(3);
                    cashDetailLog.setCallbackTime(new Date());
                    cashDetailLogService.save(cashDetailLog);

                    // 更改用户资金
                    CapitalChangeEntity entity = new CapitalChangeEntity() ;
                    entity.setType(CapitalChangeEnum.Cash);
                    entity.setMoney(cashDetailLog.getMoney().intValue());
                    entity.setUserId(userId);
                    entity.setToUserId(userId);
                    capitalChangeHelper.capitalChange(entity) ;
                    return true;
                }
            }

        }while (realSize == pageSize) ;
        return false;
    }

}
