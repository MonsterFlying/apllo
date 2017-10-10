package com.gofobao.framework.financial.biz.impl;

import com.gofobao.framework.financial.biz.JixinAssetBiz;
import com.gofobao.framework.financial.entity.JixinAsset;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.JixinAssetService;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

@Service
@Slf4j
public class JixinAssetBizImpl implements JixinAssetBiz {

    @Autowired
    JixinAssetService jixinAssetService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Override
    public boolean record(NewEve newEve) {    // 判断即信资金记录是否存在
        JixinAsset jixinAsset = jixinAssetService.findTopByAccountId(newEve.getCardnbr());
        Date nowDate = new Date();
        if (ObjectUtils.isEmpty(jixinAsset)) { // 发现没有就创建
            UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(newEve.getCardnbr());
            if (ObjectUtils.isEmpty(userThirdAccount)) {
                log.error(String.format("即信金额统计, 根据电子账号信息, 查询用户信息为空, 数据[%s]", newEve.getCardnbr()));
                return false;
            }
            jixinAsset = new JixinAsset();
            jixinAsset.setAccountId(userThirdAccount.getAccountId());
            jixinAsset.setCurrMoney(0L);
            jixinAsset.setUserId(userThirdAccount.getUserId());
            jixinAsset.setUpdateTime(nowDate);
            jixinAsset = jixinAssetService.save(jixinAsset);
        }

        // 转换金额
        String crflag = newEve.getCrflag();
        String amount = newEve.getAmount();
        long curMoney = 0;  // 金额

        if ("C".equalsIgnoreCase(crflag)) {
            curMoney = jixinAsset.getCurrMoney() - MoneyHelper.yuanToFen(amount);
        } else if ("D".equalsIgnoreCase(crflag)) {
            curMoney = jixinAsset.getCurrMoney() + MoneyHelper.yuanToFen(amount);
        } else {
            log.error("未知的交易金额符号");
            return false;
        }

        jixinAsset.setUpdateTime(nowDate);
        jixinAsset.setCurrMoney(curMoney); // 返回当前变动后的金额
        return true;
    }
}
