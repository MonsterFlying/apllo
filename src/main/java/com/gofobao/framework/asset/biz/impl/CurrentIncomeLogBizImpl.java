package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.CurrentIncomeLogBiz;
import com.gofobao.framework.asset.entity.CurrentIncomeLog;
import com.gofobao.framework.asset.service.CurrentIncomeLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import jxl.CellType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
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
    UserThirdAccountService userThirdAccountService ;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process() throws Exception {
        log.info("活期利息调度启动");
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, jixinTxDateHelper.getSubDateStr(1));
        File xlsFilePathDir = new File(filePath);
        String xlsName = String.format("%s%s", fileName, ".xlsx");
        File xlsFile = new File(xlsFilePathDir, xlsName);
        if (!xlsFile.exists()) {
            log.error("EVE文件不存在");
            return false;
        }

        FileInputStream excelFile = new FileInputStream(xlsFile) ;
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = datatypeSheet.iterator();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo() ;
        Date nowDate = new Date() ;
        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            Cell typeCell = currentRow.getCell(19);
            String type = typeCell.getStringCellValue();
            if (!type.equals("5500")) {
                continue;
            }

            Cell moneyCell = currentRow.getCell(4);
            double money = moneyCell.getNumericCellValue();
            Cell accountCell = currentRow.getCell(3);
            String accountId = accountCell.getStringCellValue();
            Cell tranDateStrCell = currentRow.getCell(2);
            String tranDateStr = tranDateStrCell.getStringCellValue();
            Cell seqNoCell = currentRow.getCell(1);
            String seqNo = seqNoCell.getStringCellValue();
            String no = String.format("%s%s", tranDateStr, seqNo);

            log.info(String.format("处理活期收益开始: %s", no));
            List<CurrentIncomeLog> currentIncomeLogs = currentIncomeLogService.findBySeqNoAndState(no, 1);
            if (!CollectionUtils.isEmpty(currentIncomeLogs)) {
                log.error(String.format("当前用户已添加活期收益: %s - %s", accountId, no));
                continue;
            }

            UserThirdAccount accountUser = userThirdAccountService.findByAccountId(accountId);
            if(ObjectUtils.isEmpty(accountUser)){
                log.error(String.format("当前没有开通存管账户: %s - %s", accountId, no));
                continue;
            }

            long currMoney = new Double(money * 100D).longValue();
            CurrentIncomeLog currentIncomeLog  = new CurrentIncomeLog() ;
            currentIncomeLog.setCreateAt(nowDate);
            currentIncomeLog.setUserId(accountUser.getUserId());
            currentIncomeLog.setSeqNo(no);
            currentIncomeLog.setState(0);
            currentIncomeLog.setMoney(currMoney);

            currentIncomeLog = currentIncomeLogService.save(currentIncomeLog) ;
            AssetChange assetChange = new AssetChange() ;
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

        return true;
    }

}
