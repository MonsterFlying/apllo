package com.gofobao.framework.migrate;

/**
 * 投标迁移
 */

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MigrateTenderBiz {
    @Autowired
    BorrowService borrowService;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetService assetService;

    private static final String MIGRATE_PATH = "D:/apollo/migrate";
    private static final String TENDER_DIR = "tender";
    /**
     * 银行编号
     */
    private static final String BANK_NO = "3005";

    /**
     * 合作单位编号
     */
    private static final String COINST_CODE = "000187";
    /**
     * 产品发行方
     */
    private static final String FUISSUER = "MP";

    /**
     * 迁移结果文件
     */
    private static final String RESULT_TENDER_FILE_PATH = "D:/Apollo/migrate/tender_result/3005-BIDRESP-102752-20170421";

    @Autowired
    private TenderService tenderService;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    /**
     * 获取标的迁移文件
     */
    public void getTenderMigrateFile() {
        Date nowDate = new Date();
        String batchNo = DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_HMS_NUM);
        String fileName = String.format("%s-BID-%s-%s", BANK_NO,
                batchNo,
                jixinTxDateHelper.getTxDateStr());
        // 创建
        BufferedWriter tenderWriter = null;
        try {
            File tenderFile = FileHelper.createFile(MIGRATE_PATH, TENDER_DIR, fileName);
            tenderWriter = Files.newWriter(tenderFile, Charset.forName("gbk"));
        } catch (Exception e) {
            log.error("创建投标迁移文件失败", e);
        }

        String errorFileName = fileName + "_error";
        BufferedWriter errorTenderWriter = null;
        try {
            File tenderErrorFile = FileHelper.createFile(MIGRATE_PATH, TENDER_DIR, errorFileName);
            errorTenderWriter = Files.newWriter(tenderErrorFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            log.error("创建投标迁移文件失败", e);
        }
        int realSize = 0, pageIndex = 0, pageSize = 2000;
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .ne("productId", null)
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs, pageable);
            if (CollectionUtils.isEmpty(borrowList)) {
                break;
            }
            pageIndex++;
            realSize = borrowList.size();
            Map<Long, Borrow> borrowMap = borrowList
                    .stream()
                    .collect(Collectors.toMap(Borrow::getId, Function.identity()));

            Set<Long> borrowIdSet = borrowList
                    .stream()
                    .map(borrow -> borrow.getId())
                    .collect(Collectors.toSet());
            // 投标成功, 未转让
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("borrowId", borrowIdSet.toArray())
                    .eq("status", 1)
                    .eq("transferFlag", 0)
                    .eq("authCode", null)
                    .build();
            List<Tender> tenderList = tenderService.findList(ts);

            Set<Long> userIdSet = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());
            Specification<UserThirdAccount> uts = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", userIdSet.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uts);
            Map<Long, UserThirdAccount> userThirdAccountMap = userThirdAccountList
                    .stream()
                    .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));
            for (Tender tender : tenderList) {
                StringBuffer text = new StringBuffer();
                try {
                    UserThirdAccount userThirdAccount = userThirdAccountMap.get(tender.getUserId());
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        StringBuffer error = new StringBuffer();
                        error.append(tender.getId()).append("|").append(tender.getUserId()).append("|").append("未开户");
                        errorTenderWriter.write(error.toString());
                        errorTenderWriter.newLine();
                        continue;
                    }
                    String seriNo = String.format("%s0000%s", COINST_CODE, JixinHelper.getOrderId(JixinHelper.TENDER_PREFIX));
                    Borrow borrow = borrowMap.get(tender.getBorrowId());
                    Date successAt = borrow.getSuccessAt();
                    String inDate = DateHelper.dateToString(successAt, DateHelper.DATE_FORMAT_YMD_NUM);
                    String intType = null;
                    Integer repayFashion = borrow.getRepayFashion();
                    String intPayDay = null;
                    String endDate = null;
                    String buyDate = jixinTxDateHelper.getTxDateStr();
                    if (1 == repayFashion) {
                        intType = "0";
                        intPayDay = "";
                        endDate = DateHelper.dateToString(DateHelper.addDays(successAt, borrow.getTimeLimit()), DateHelper.DATE_FORMAT_YMD_NUM);
                    } else {
                        intType = "1";
                        intPayDay = String.valueOf(DateHelper.getDay(successAt));
                        if (intPayDay.length() == 1) {
                            intPayDay = "0" + intPayDay;
                        }
                        endDate = DateHelper.dateToString(DateHelper.addMonths(successAt, borrow.getTimeLimit()), DateHelper.DATE_FORMAT_YMD_NUM);
                    }

                    String yield = StringHelper.toString(borrow.getApr() * 1000); // 逾期年化收益
                    text.append(FormatHelper.appendByTail(BANK_NO, 4)); // 银行代号
                    text.append(FormatHelper.appendByTail(batchNo, 6));  // 批次号
                    text.append(FormatHelper.appendByTail(userThirdAccount.getAccountId(), 19));  // 债权持有人电子账号
                    text.append(FormatHelper.appendByTail(FUISSUER, 4));  // 产品发行方
                    text.append(FormatHelper.appendByTail("", 6));  // 标的号
                    text.append(FormatHelper.appendByTail(seriNo, 40));  // 申请流水号
                    text.append(FormatHelper.appendByPre(StringHelper.toString(tender.getValidMoney()), 13));  //当前持有债权金额
                    text.append(FormatHelper.appendByTail(buyDate, 8));  // 债权获取日期
                    text.append(FormatHelper.appendByTail(inDate, 8));  // 起息日
                    text.append(FormatHelper.appendByTail(intType, 1));  // 付息方式
                    text.append(FormatHelper.appendByTail(intPayDay, 2));  // 利息每月支付日
                    text.append(FormatHelper.appendByTail(endDate, 8));  // 产品到期日
                    text.append(FormatHelper.appendByPre(yield, 8));  // 预期年化收益率
                    text.append(FormatHelper.appendByTail("156", 3));  // 币种
                    text.append(FormatHelper.appendByTail(borrow.getProductId(), 40));  // 标的编号
                    text.append(FormatHelper.appendByTail(String.valueOf(tender.getId()), 60));  //保留域
                    tenderWriter.write(text.toString());
                    tenderWriter.write("\n");
                } catch (Exception e) {
                    log.error("投标写入失败", e);
                }
            }

        } while (realSize == pageSize);
        try {
            if (tenderWriter != null) {
                tenderWriter.flush();
                tenderWriter.close();
            }

            if (errorTenderWriter != null) {
                errorTenderWriter.flush();
                errorTenderWriter.close();
            }
        } catch (Exception e) {
            log.error("清楚债权", e);
        }
        log.info("债权迁移文件成功");
    }


    /**
     * 接收处理结果
     */
    public void postMigrateTenderFile() {
        File file = new File(RESULT_TENDER_FILE_PATH);
        if (!file.exists()) {
            log.error("债权文件不存在");
            return;
        }

        BufferedReader reader = null;
        try {
            reader = Files.newReader(file, Charset.forName("gbk"));
        } catch (FileNotFoundException e) {
            log.error("读取文件异常", e);
            return;
        }

        List<Long> tenderIdList = new ArrayList<>();
        File errorFile = new File(RESULT_TENDER_FILE_PATH + "_error");
        final BufferedWriter errorWriter;
        try {
            errorWriter = Files.newWriter(errorFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("生成错误文件异常", e);
            return;
        }

        Stream<String> lines = reader.lines();
        Map<Long, String> authCodeMap = new HashMap<>();
        lines.forEach((String item) -> {
            try {
                byte[] gbks = item.getBytes("gbk");
                String flag = FormatHelper.getStrForGBK(gbks, 160, 162);
                String idStr = FormatHelper.getStrForGBK(gbks, 222, 282);
                if (!"00".equals(flag)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(NumberHelper.toLong(idStr)).append("|").append(ERROR_MSG_DATA.get(flag));
                    try {
                        errorWriter.write(stringBuffer.toString());
                        errorWriter.newLine();
                    } catch (IOException e) {
                        log.error("写入债权错误数据", e);
                        return;
                    }
                } else {
                    String authCode = FormatHelper.getStrForGBK(gbks, 162, 182);
                    String orderId = FormatHelper.getStrForUTF8(gbks, 39, 79);
                    orderId = orderId.substring(10);
                    long tenderId = NumberHelper.toLong(idStr);
                    tenderIdList.add(tenderId);
                    authCodeMap.put(tenderId, authCode + "#" + orderId);
                }
            } catch (Exception e) {
                log.error("获取字段错误", e);
                return;
            }
        });

        Specification<Tender> bs = Specifications
                .<Tender>and()
                .in("id", tenderIdList.toArray())
                .build();
        List<Tender> tenderList = tenderService.findList(bs);
        tenderList.stream().filter(tender -> !tender.getThirdTenderFlag()).forEach(tender -> {
            String[] split = authCodeMap.get(tender.getId()).split("#");
            tender.setAuthCode(split[0]);
            tender.setThirdTenderOrderId(split[1]);
            tender.setThirdTenderFlag(true);
        });
        tenderService.save(tenderList);
    }

    private static Map<String, String> ERROR_MSG_DATA = new HashMap<>();

    static {
        ERROR_MSG_DATA.put("14", "无效账号");
        ERROR_MSG_DATA.put("93", "姓名校验错误");
        ERROR_MSG_DATA.put("99", "其它错误原因");
    }
}
