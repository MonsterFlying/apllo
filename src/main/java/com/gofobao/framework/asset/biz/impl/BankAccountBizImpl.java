package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.asset.vo.response.VoBankListResp;
import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
@Slf4j
public class BankAccountBizImpl implements BankAccountBiz{
    @Value("${gofobao.aliyun-bankinfo-url}")
    String aliyunQueryBankUrl ;

    @Value("${gofobao.aliyun-bankinfo-appcode}")
    String aliyunQueryAppcode ;

    @Value("${gofobao.javaDomain}")
    String javaDomain ;

    @Autowired
    DictValueService dictValueServcie ;

    @Autowired
    DictItemServcie dictItemServcie ;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService ;

    @Autowired
    UserThirdAccountService userThirdAccountService ;

    @Autowired
    CashDetailLogService cashDetailLogService ;

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

                    return dictValueServcie.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            }) ;


    @Override
    public ResponseEntity<VoBankTypeInfoResp> findTypeInfo(Long userId, String account) {
        if( StringUtils.isEmpty(account) ){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前银行账号为空", VoBankTypeInfoResp.class)) ;
        }

        UserThirdAccount thirdAccount = userThirdAccountService.findByUserId(userId);
        if( (!ObjectUtils.isEmpty(thirdAccount)) && (!StringUtils.isEmpty(thirdAccount.getCardNo()))){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户已经绑定银行卡！", VoBankTypeInfoResp.class)) ;
        }

        // 判断当前银行卡是否存在
        UserThirdAccount userThirdAccount = userThirdAccountService.findTopByCardNo(account) ;

        if(!ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前银行卡已被使用", VoBankTypeInfoResp.class)) ;
        }

        // 请求支付宝获取银行信息
        Map<String, String> params = new HashMap<>();
        params.put("bankcard", account);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("APPCODE %s", aliyunQueryAppcode));
        String jsonStr = OKHttpHelper.get(aliyunQueryBankUrl, params, headers);

        if (StringUtils.isEmpty(jsonStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试", VoBankTypeInfoResp.class)) ;
        }

        JsonObject result = new JsonParser().parse(jsonStr).getAsJsonObject();
        int status = result.get("status").getAsInt();
        if (status != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试", VoBankTypeInfoResp.class)) ;
        }

        JsonObject info = result.get("result").getAsJsonObject();
        String type = info.get("type").getAsString();
        if (!"借记卡".equals(type)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "平台暂不支持信用卡", VoBankTypeInfoResp.class)) ;
        }
        String bankname = info.get("bank").getAsString();

        // 获取银行
        DictValue bank = null;
        try {
            bank = bankLimitCache.get(bankname);
        } catch (ExecutionException e) {
            log.error("BankAccountBizImpl.findTypeInfo: bank type is exists ");
        }
        if(ObjectUtils.isEmpty(bank)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("平台暂不支持%s, 请尝试其他银行卡！", bankname), VoBankTypeInfoResp.class)) ;
        }

        // 返回银行信息
        VoBankTypeInfoResp resp = VoBaseResp.ok("查询成功", VoBankTypeInfoResp.class);
        resp.setBankName(bankname);
        resp.setTimesLimitMoney(bank.getValue04().split(",")[0]);
        resp.setDayLimitMoney(bank.getValue05().split(",")[0]);
        resp.setMonthLimitMonty(bank.getValue06().split(",")[0]);
        resp.setBankIcon(info.get("logo").getAsString());

        return ResponseEntity.ok(resp) ;

    }

    @Override
    public ResponseEntity<VoHtmlResp> credit() {
        return null;
    }

    @Override
    public void showDesc(Model model) {
        DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("PLATFORM_BANK", 0) ;
        if(ObjectUtils.isEmpty(dictItem)){
            throw new RuntimeException("BankAccountBizImpl.showDesc： 查询平台支持银行卡列表异常， 请联系平台客服!");
        }

        List<DictValue>  list = dictValueServcie.findByItemId(dictItem.getId()) ;
        List<Map<String, String>> bankList = new ArrayList<>() ;
        list.forEach(bean ->{
            Map<String, String> rs = new HashMap<>() ;
            rs.put("logo", String.format("%s/%s", javaDomain, bean.getValue03())) ; // logo
            rs.put("name", bean.getName()) ;
            rs.put("desc", String.format("单笔限额%s元，每日限额%s元，每月限额%s元"
                    , bean.getValue04().split(",")[0]
                    , bean.getValue05().split(",")[0]
                    , bean.getValue06().split(",")[0])) ;
            bankList.add(rs) ;
        });

        model.addAttribute("bankList", bankList) ;

    }

    @Override
    public ResponseEntity<VoBankListResp> list(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if(ObjectUtils.isEmpty(userThirdAccount)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请开通银行存管", VoBankListResp.class)) ;
        }

        if(userThirdAccount.getPasswordState() == 0){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "请先初始化密码", VoBankListResp.class)) ;
        }

        VoBankListResp response = VoBaseResp.ok("查询成功", VoBankListResp.class) ;
        response.setBankCard(UserHelper.hideChar(userThirdAccount.getCardNo(), UserHelper.BANK_ACCOUNT_NUM ));
        response.setBankLogo(String.format("%s/%s", javaDomain, userThirdAccount.getBankLogo())) ;
        response.setBankName(userThirdAccount.getBankName());
        response.setBankType("储蓄卡");
        return ResponseEntity.ok(response) ;
    }


    /**
     * 充值额度说明
     * @param userId

     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public double[] getRechargeCredit(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        String bankName = userThirdAccount.getBankName();
        double[] bankCredit = getBankCredit(bankName);  // 银行卡充值额度说明

        Date endDate = new Date() ;
        Date dayStartDate = DateHelper.beginOfYear(endDate) ;
        Date startDate = DateHelper.beginOfMonth(endDate);
        ImmutableList<Integer> stateList = ImmutableList.of(0, 1) ; // 充值成功和申请充值中的
        List<RechargeDetailLog> rechargeDetailLogs = rechargeDetailLogService.findByUserIdAndDelAndStateInAndCreateTimeBetween(userId, 0, stateList, startDate, endDate);
        if(CollectionUtils.isEmpty(rechargeDetailLogs)){
            return bankCredit ;
        }
        double [] result = new double[3];
        result[0] = bankCredit[0] ;  // 每笔限额

        // 今天充值金额
        long dayRechargeSum = rechargeDetailLogs
                .stream()
                .filter( bean ->  DateHelper.diffInDays(dayStartDate, bean.getCreateTime(), false) > 0)
                .mapToLong(bean->bean.getMoney())
                .sum() ;

        // 这个月充值金额
        long mouthRechargeSum = rechargeDetailLogs
                .stream()
                .mapToLong(bean->bean.getMoney())
                .sum() ;

        result[1] = bankCredit[1] - dayRechargeSum ;
        result[2] = bankCredit[2] - mouthRechargeSum ;

        return result ;
    }


    /**
     * 提现额度查询
     * @param userId

     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public double[] getCashCredit(Long userId) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        String bankName = userThirdAccount.getBankName();
        double[] bankCredit = getBankCredit(bankName);  // 银行卡充值额度说明

        Date endDate = new Date() ;
        Date dayStartDate = DateHelper.beginOfYear(endDate) ;
        Date startDate = DateHelper.beginOfMonth(endDate);
        ImmutableList<Integer> stateList = ImmutableList.of(0, 1) ; // 充值成功和申请充值中的
        List<CashDetailLog> cashDetailLogs = cashDetailLogService.findByUserIdAndStateInAndCreateTimeBetween(userId, stateList, startDate, endDate);
        if(CollectionUtils.isEmpty(cashDetailLogs)){
            return bankCredit ;
        }

        // 每一笔提现限额
        double [] result = new double[3];
        result[0] = bankCredit[0] ;

        // 今天提现金额
        long dayCashSum = cashDetailLogs
                .stream()
                .filter( bean ->  DateHelper.diffInDays(dayStartDate, bean.getCreateTime(), false) > 0)
                .mapToLong(bean->bean.getMoney())
                .sum() ;

        // 这个月提现金额
        long mouthCashSum = cashDetailLogs
                .stream()
                .mapToLong(bean->bean.getMoney())
                .sum() ;

        result[1] = bankCredit[1] - dayCashSum ;
        result[2] = bankCredit[2] - mouthCashSum ;
        return result ;
    }

    /**
     * 获取银行额度
     * @param bankName
     * @return
     */
    private double[] getBankCredit(String bankName) {
        // 获取银行
        DictValue bank = null;
        try {
            bank = bankLimitCache.get(bankName);
        } catch (ExecutionException e) {
            log.error("BankAccountBizImpl.findTypeInfo: bank type is exists ");
        }
        double [] money = {Double.parseDouble(bank.getValue04().split(",")[1]),
                Double.parseDouble(bank.getValue05().split(",")[1]),
                Double.parseDouble(bank.getValue06().split(",")[1])};
        return money ;
    }

}
