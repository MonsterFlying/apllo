package com.gofobao.framework.migrate;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MigrateProtocolBiz {
    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetService assetService;

    private static final String MIGRATE_PATH = "D:/apollo/migrate";
    private static final String PROTOCOL_DIR = "protocol";
    /**
     * 银行编号
     */
    private static final String BANK_NO = "3005";
    /**
     * 产品编号
     */
    private static final String PROUDCT_NO = "0110";

    private static final String FUISSUER = "MP";

    private static final String COINSTCODE = "000187";

    /**
     * 迁移结果文件
     */
    private static final String RESULT_MEMBER_FILE_PATH = "D:/Apollo/migrate/protocol_result/3005-MP-SIGRES-161519-20170421";

    /**
     * 写入协议存管
     */
    public void postProtocolMigrateFile() {
        final Date nowDate = new Date();
        File file = new File(RESULT_MEMBER_FILE_PATH);
        if (!file.exists()) {
            log.error("协议文件不存在");
            return;
        }

        BufferedReader reader = null;
        try {
            reader = Files.newReader(file, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("读取文件异常", e);
            return;
        }

        File errorFile = new File(RESULT_MEMBER_FILE_PATH + "_error");
        final BufferedWriter errorWriter;
        try {
            errorWriter = Files.newWriter(errorFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("生成错误文件异常", e);
            return;
        }
        Stream<String> lines = reader.lines();

        List<Long> transferIdList = new ArrayList<>();
        Map<Long, String> transferMap = new HashMap<>();
        List<Long> tenderids = new ArrayList<>();
        Map<Long, String> tenderMap = new HashMap<>();
        List<String> lineList = lines.collect(Collectors.toList());
        for (String line : lineList) {
            try{
                byte[] gbks = line.getBytes("gbk");
                String flag =  FormatHelper.getStrForGBK(gbks, 88, 90) ;
                String idStr = FormatHelper.getStrForGBK(gbks, 90, 190) ;
                String type = FormatHelper.getStrForGBK(gbks, 33, 34) ;
                String seqNo = FormatHelper.getStrForGBK(gbks, 34, 74) ;
                long id = NumberHelper.toLong(idStr);
                if (!"00".equals(flag)) {
                    StringBuffer error = new StringBuffer();
                    error.append(id).append("|").append(ERROR_MSSAGE.get(flag));
                    try {
                        errorWriter.write(error.toString());
                        errorWriter.newLine();
                    } catch (IOException e) {
                        log.error("写入文件错误", e);
                        return;
                    }
                } else {
                    if ("1".equals(type)) {  // 自动投标
                        tenderids.add(id);
                        tenderMap.put(id, seqNo);
                    } else { // 债权转让
                        transferIdList.add(id);
                        transferMap.put(id, seqNo);
                    }
                }
            }catch (Exception e){
                log.error("写入异常", e);
            }
        }

        if(!CollectionUtils.isEmpty(tenderids)){
            Specification<UserThirdAccount> specification = Specifications
                    .<UserThirdAccount>and()
                    .in("id", tenderids.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(specification);
            userThirdAccountList.stream().filter(userThirdAccount -> userThirdAccount.getAutoTenderState() != 1).
                    forEach(userThirdAccount -> {
                userThirdAccount.setAutoTenderState(1);
                userThirdAccount.setAutoTenderTotAmount(999999999L);
                userThirdAccount.setAutoTenderTxAmount(999999999L);
                userThirdAccount.setAutoTenderOrderId(tenderMap.get(userThirdAccount.getId()));
            });

            userThirdAccountService.save(userThirdAccountList);
        }

        if(!CollectionUtils.isEmpty(transferIdList)){
            Specification<UserThirdAccount> specification = Specifications
                    .<UserThirdAccount>and()
                    .in("id", transferIdList.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(specification);
            userThirdAccountList.stream().filter(userThirdAccount -> userThirdAccount.getAutoTransferState() != 1).forEach(userThirdAccount -> {
                userThirdAccount.setAutoTransferState(1);
                userThirdAccount.setAutoTransferBondOrderId(transferMap.get(userThirdAccount.getId()));
            });
            userThirdAccountService.save(userThirdAccountList);
        }
    }

    static Map<String, String> ERROR_MSSAGE = new HashMap<>();

    static {
        ERROR_MSSAGE.put("00", "签约成功");
        ERROR_MSSAGE.put("01", "银行号为空");
        ERROR_MSSAGE.put("02", "批次号为空");
        ERROR_MSSAGE.put("03", "电子账户为空");
        ERROR_MSSAGE.put("04", "理财产品发行方为空");
        ERROR_MSSAGE.put("05", "签约类型为空");
        ERROR_MSSAGE.put("06", "签约类型值非1或2");
        ERROR_MSSAGE.put("07", "签约流水号为空");
        ERROR_MSSAGE.put("08", "签约日期为空");
        ERROR_MSSAGE.put("09", "签约时间为空");
        ERROR_MSSAGE.put("10", "存在未取消的签约记录");
        ERROR_MSSAGE.put("11", "签约流水号已存在");
        ERROR_MSSAGE.put("12", "卡号不存在");
        ERROR_MSSAGE.put("13", "卡片不属于该发卡行");
        ERROR_MSSAGE.put("14", "卡片未激活");
        ERROR_MSSAGE.put("15", "卡片未申领或制卡");
        ERROR_MSSAGE.put("16", "未找到电子账户的客户记录");
    }

    private static String getErrorMsg(String code) {
        return ERROR_MSSAGE.get(code);
    }


    @Autowired
    private JixinTxDateHelper jixinTxDateHelper ;
    /**
     * 获取用户协议迁移数据
     */
    public void getProtocolMigrateFile() {
        Date nowDate = new Date();
        String timeStr = DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_HMS_NUM);
        String dateStr =jixinTxDateHelper.getTxDateStr() ;
        String fileName = String.format("%s-%s-SIGTRAN-%s-%s", BANK_NO, FUISSUER,
                timeStr,
                dateStr);
        // 创建文件
        BufferedWriter gbk = null;
        try {
            File normalFile = FileHelper.createFile(MIGRATE_PATH, PROTOCOL_DIR, fileName);
            gbk = Files.newWriter(normalFile, Charset.forName("gbk"));
        } catch (Exception e) {
            return;
        }


        BufferedWriter errorWirter = null;
        FileWriter fileWriter = null;
        try {
            File errorFile = FileHelper.createFile(MIGRATE_PATH, PROTOCOL_DIR, fileName + "_error");
            errorWirter = Files.newWriter(errorFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return;
        }

        int realSize = 0, pageIndex = 0, pageSize = 2000;
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            Page<UserThirdAccount> userThirdAccounts = userThirdAccountService.findAll(pageable);
            if (ObjectUtils.isEmpty(userThirdAccounts)) {
                log.error("查询user失败");
                return;
            }

            realSize = userThirdAccounts.getSize();
            pageIndex++;
            List<UserThirdAccount> userThirdAccountList = userThirdAccounts.getContent();
            if (CollectionUtils.isEmpty(userThirdAccountList)) {
                break;
            }


            for (UserThirdAccount item : userThirdAccountList) {
                if (item.getAutoTransferState() != 1) {  // 自动债权转让
                    try {
                        String orderId = System.currentTimeMillis() + RandomHelper.generateNumberCode(10);
                        StringBuffer text = new StringBuffer();
                        text.append(FormatHelper.appendByTail(BANK_NO, 4)); // 银行代号
                        text.append(FormatHelper.appendByTail(timeStr, 6));  // 批次号
                        text.append(FormatHelper.appendByTail(item.getAccountId(), 19));  // 签约电子账号
                        text.append(FormatHelper.appendByTail(FUISSUER, 4));
                        text.append(FormatHelper.appendByTail("2", 1));
                        text.append(FormatHelper.appendByTail(String.format("%s%s%s", COINSTCODE, "0000", orderId), 40));
                        text.append(FormatHelper.appendByTail(dateStr, 8));
                        text.append(FormatHelper.appendByTail(timeStr, 6));
                        text.append(FormatHelper.appendByTail(item.getId().toString(), 100));
                        text.append(FormatHelper.appendByTail("", 100));
                        gbk.write(text.toString());
                        gbk.write("\n");
                    } catch (Exception e) {
                        log.error("债权转让迁移错误");
                    }
                } else if (item.getAutoTenderState() != 1) {
                    try {
                        String orderId = System.currentTimeMillis() + RandomHelper.generateNumberCode(14);
                        StringBuffer text = new StringBuffer();
                        text.append(FormatHelper.appendByTail(BANK_NO, 4)); // 银行代号
                        text.append(FormatHelper.appendByTail(timeStr, 6));  // 批次号
                        text.append(FormatHelper.appendByTail(item.getAccountId(), 19));  // 签约电子账号
                        text.append(FormatHelper.appendByTail(FUISSUER, 4));
                        text.append(FormatHelper.appendByTail("1", 1));
                        text.append(FormatHelper.appendByTail(String.format("%s%s%s", COINSTCODE, "0000", orderId), 40));
                        text.append(FormatHelper.appendByTail(dateStr, 8));
                        text.append(FormatHelper.appendByTail(timeStr, 6));
                        text.append(FormatHelper.appendByTail(item.getId().toString(), 100));
                        text.append(FormatHelper.appendByTail("", 100));
                        gbk.write(text.toString());
                        gbk.write("\n");
                    } catch (Exception e) {
                        log.error("债权转让迁移错误");
                    }
                }
            }
        } while (realSize == pageSize);
        try {
            if (gbk != null) {
                gbk.flush();
                gbk.close();
            }
            if (errorWirter != null) {
                errorWirter.flush();
                errorWirter.close();
            }
        } catch (Exception e) {
            log.error("协议关闭资源错误");
        }
        log.info("用户迁移文件成功");
    }

}
