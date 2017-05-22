package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.asset.vo.request.VoUserBankListReq;
import com.gofobao.framework.asset.vo.response.VoUserBankListResp;
import com.gofobao.framework.asset.vo.response.VoUserBankResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MaskHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class BankAccountBizImpl implements BankAccountBiz{

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserCacheService userCacheService;

    /**
     * 获取用户银行卡列表
     * @param voUserBankListReq
     * @return
     */
    public ResponseEntity<VoUserBankListResp> listUserBank(VoUserBankListReq voUserBankListReq){
        Long userId = voUserBankListReq.getUserId();
        List<BankAccount> bankAccountList = bankAccountService.findByDeletedAtIsNullAndUserIdAndIsVerify(userId,1);
        Asset asset = assetService.findById(userId);
        UserCache userCache = userCacheService.findById(userId);

        int allMoney = asset.getTotal() - asset.getPayment() - userCache.getWaitExpenditureInterestManageFee();//账户所有钱
        int lockMoneySum = 0;//锁定总金额
        for (BankAccount bankAccount : bankAccountList) {
            lockMoneySum += Math.max((bankAccount.getRechargeTotal() - bankAccount.getCashTotal()), 0);
        }

        int canCashMoney = Math.min(asset.getUseMoney(), asset.getTotal() - asset.getPayment());//可提现额
        if (userId == 24) {
            if (DateHelper.isFuture(DateHelper.stringToDate("2017-07-21 00:00:00", "yyyy-MM-dd HH:mm:ss"))) {
                canCashMoney -= 25000000;
            }
        }
        if (asset.getPayment() > 0) {
            canCashMoney = (int) Math.min((asset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8 - asset.getPayment(), canCashMoney);
        }
        canCashMoney = Math.max(canCashMoney, 0);

        // 过滤
        List<VoUserBankResp> results = new ArrayList<>();
        VoUserBankResp bankResp = null;
        if (!CollectionUtils.isEmpty(bankAccountList)) {
            for (BankAccount account : bankAccountList) {
                bankResp = new VoUserBankResp();
                long lockMoney = Math.max(account.getRechargeTotal() - account.getCashTotal(), 0);
                long mostCash = Math.min(Math.max(allMoney - lockMoneySum + lockMoney, lockMoney), canCashMoney);

                bankResp.setLeastCash(lockMoney);
                bankResp.setMostCash(mostCash);
                bankResp.setBankName(convertBankInfoToMap().get(String.valueOf(account.getBank())));
                bankResp.setBankNo(MaskHelper.bankCardReplaceWithStar(account.getAccount()));
                bankResp.setPhone(MaskHelper.phoneReplaceWithStar(account.getPhone()));
                bankResp.setDef(account.getIsDefault());
                bankResp.setId(account.getId());
                bankResp.setType("储蓄卡");
                results.add(bankResp);
            }
        }
        return null;
    }

    /**
     * 将数据字典中的数据结构装换成Map形式
     *
     * @return
     */
    private Map<String, String> convertBankInfoToMap() {
        List<Map<String, String>> bankInfoList = null;
        /*try {
            bankInfoList = dictService.queryDictList(DictAliasCodeContants.PLATFORM_BANK);
        } catch (Exception e) {
            logger.error(String.format("银行信息装换异常：%s", e.getMessage()));
        }*/
        Map<String, String> map = new HashMap<>();

        if (!CollectionUtils.isEmpty(bankInfoList)) {
            for (Map<String, String> bean : bankInfoList) {
                map.put(bean.get("value01"), bean.get("value02"));
            }
        }

        return map;
    }
}
