package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.CurrentIncomeLogBiz;
import com.gofobao.framework.asset.entity.CurrentIncomeLog;
import com.gofobao.framework.asset.service.CurrentIncomeLogService;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.service.EveService;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import jxl.biff.DoubleHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class CurrentIncomeLogBizImpl implements CurrentIncomeLogBiz {

    @Value("${jixin.product-no}")
    String productNo;

    @Value("${jixin.bank-no}")
    String bankNo;

    @Value("${jixin.save-file-path}")
    String filePath;

    @Autowired
    CurrentIncomeLogService currentIncomeLogService;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    EveService eveService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(String date) throws Exception {
        Specification<Eve> specification = Specifications.
                <Eve>and()
                .eq("queryDate", date)
                .eq("transtype", "5500")
                .build();

        int pageSize = 1000, pageIndex = 0, realSize = 0;
        Date nowDate = new Date();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            List<Eve> eveList = eveService.findList(specification, pageable);
            if (CollectionUtils.isEmpty(eveList)) {
                if (pageIndex == 0) {
                    log.error("活期收益记录为空");
                    return false;
                }
                break;
            }

            pageIndex++;
            realSize = eveList.size();
            for (Eve eve : eveList) {
                double money = NumberHelper.toDouble(StringUtils.trimAllWhitespace(eve.getAmount())); // 金额
                String accountId = StringUtils.trimAllWhitespace(eve.getCardnbr()); // 账号
                String tranDateStr = StringUtils.trimAllWhitespace(eve.getCendt());  //原始跟踪号
                String seqNo = StringUtils.trimAllWhitespace(eve.getSeqno());  // 序列号
                String no = String.format("20%s%s", tranDateStr, seqNo);
                List<CurrentIncomeLog> currentIncomeLogs = currentIncomeLogService.findBySeqNoAndState(no, 1);
                if (!CollectionUtils.isEmpty(currentIncomeLogs)) {
                    log.error(String.format("当前用户已添加活期收益: %s - %s", accountId, no));
                    continue;
                }

                UserThirdAccount accountUser = userThirdAccountService.findByAccountId(accountId);
                if (ObjectUtils.isEmpty(accountUser)) {
                    log.error(String.format("当前没有开通存管账户: %s - %s", accountId, no));
                    continue;
                }

                long currMoney = new Double(money * 100D).longValue();
                CurrentIncomeLog currentIncomeLog = new CurrentIncomeLog();
                currentIncomeLog.setCreateAt(nowDate);
                currentIncomeLog.setUserId(accountUser.getUserId());
                currentIncomeLog.setSeqNo(no);
                currentIncomeLog.setState(0);
                currentIncomeLog.setMoney(currMoney);

                currentIncomeLog = currentIncomeLogService.save(currentIncomeLog);
                AssetChange assetChange = new AssetChange();
                assetChange.setUserId(accountUser.getUserId());
                assetChange.setSeqNo(no);
                assetChange.setRemark(String.format("收到活期收益%s元", StringHelper.formatDouble(money, true)));
                assetChange.setGroupSeqNo(groupSeqNo);
                assetChange.setSourceId(currentIncomeLog.getId());
                assetChange.setType(AssetChangeTypeEnum.currentIncome);
                assetChange.setMoney(currMoney);
                assetChangeProvider.commonAssetChange(assetChange);
                log.info(String.format("处理活期收益成功: %s", no));
            }
        } while (realSize == pageSize);

        return true;
    }

}
