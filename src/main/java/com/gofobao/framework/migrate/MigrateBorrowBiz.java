package com.gofobao.framework.migrate;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
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
public class MigrateBorrowBiz {
    @Autowired
    BorrowService borrowService;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetService assetService;

    private static final String MIGRATE_PATH = "/root/apollo/migrate";
    private static final String BORROW_DIR = "borrow";
    /**
     * 银行编号
     */
    private static final String BANK_NO = "3005";

    /**
     * 合作单位编号
     */
    private static final String COINST_CODE = "000187";

    /**
     * 获取标的迁移文件
     */
    public void getBorrowMigrateFile() {
        log.info("获取标的开始");
        Date nowDate = new Date();
        String batchNo = DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_HMS_NUM);
        String fileName = String.format("%s-BIDIN-%s-%s-%s", BANK_NO, COINST_CODE,
                batchNo,
                DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM));
        // 创建
        BufferedWriter borrowWriter = null;
        try {
            File borrowFile = FileHelper.createFile(MIGRATE_PATH, BORROW_DIR, fileName);
            borrowWriter = Files.newWriter(borrowFile, Charset.forName("gbk"));
        } catch (Exception e) {
            log.error("创建标迁移文件失败", e);
        }

        String errorFileName = fileName + "_error";
        BufferedWriter errorBorrowWriter = null;
        try {
            File borrowFile = FileHelper.createFile(MIGRATE_PATH, BORROW_DIR, errorFileName);
            errorBorrowWriter = Files.newWriter(borrowFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            log.error("创建标迁移文件失败", e);
        }

        int realSize = 0, pageIndex = 0, pageSize = 2000;
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .eq("status", "3")
                    .eq("closeAt", null)
                    .eq("productId", null)
                    .in("type", Stream.of(0, 1, 4).toArray())
                    .eq("tenderId", null)
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs, pageable);
            if (CollectionUtils.isEmpty(borrowList)) {
                break;
            }
            realSize = borrowList.size();
            pageIndex++;

            Set<Long> userIdSet = borrowList.stream().map(borrow -> borrow.getUserId()).collect(Collectors.toSet());

            Specification<UserThirdAccount> uts = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", userIdSet.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uts);
            Map<Long, UserThirdAccount> userThirdAccountMap = userThirdAccountList
                    .stream()
                    .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));
            for (Borrow borrow : borrowList) {
                Long userId = borrow.getUserId();
                UserThirdAccount userThirdAccount = userThirdAccountMap.get(userId);
                if (ObjectUtils.isEmpty(userThirdAccount)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(borrow.getId()).append("|").append(borrow.getName()).append("|").append("借款人没有开户");
                    try {
                        errorBorrowWriter.write(stringBuffer.toString());
                        errorBorrowWriter.newLine();
                    } catch (IOException e) {
                        log.error("写入异常借款失败", e);
                    }

                    continue;
                } else {
                    // 写入存管
                    StringBuffer text = new StringBuffer();
                    try {
                        String borrowName = String.format("GFB%s", borrow.getId());
                        String borrowMenoy = borrow.getMoney().toString();
                        String intType = null;
                        Integer repayFashion = borrow.getRepayFashion();
                        String intPayDay = null;
                        String loanTerm = null;
                        if (1 == repayFashion) {
                            intType = "0";
                            intPayDay = "";
                            loanTerm = String.valueOf(borrow.getTimeLimit());
                        } else {
                            intType = "1";
                            Date successAt = borrow.getSuccessAt();
                            intPayDay = String.valueOf(DateHelper.getDay(successAt));
                            loanTerm = String.valueOf(DateHelper.diffInDays(DateHelper.addMonths(successAt, borrow.getTimeLimit()), successAt, false));
                        }
                        String borrowId = StringHelper.toString(borrow.getId());
                        String yield = StringHelper.toString(borrow.getApr() * 1000); // 逾期年化收益
                        text.append(FormatHelper.appendByTail(BANK_NO, 4)); // 银行代号
                        text.append(FormatHelper.appendByTail(batchNo, 6));  // 批次号
                        text.append(FormatHelper.appendByTail(StringHelper.toString(borrow.getId()), 40));  // 标的编号
                        text.append(FormatHelper.appendByTail(borrowName, 60));//  标的描述
                        text.append(FormatHelper.appendByTail(userThirdAccount.getAccountId(), 19)); // 借款人电子账户
                        text.append(FormatHelper.appendByPre(borrowMenoy, 13)); // 借款金额
                        text.append(FormatHelper.appendByTail(intType, 1)); // 付息方式
                        text.append(FormatHelper.appendByTail(intPayDay, 2)); // 利息每月支付
                        text.append(FormatHelper.appendByTail(loanTerm, 4));  // 借款期限
                        text.append(FormatHelper.appendByPre(yield, 8));  // 逾期年化收益
                        text.append(FormatHelper.appendByTail("", 19));  // 担保人电子账户
                        if(borrow.getType() != 1){
                            text.append(FormatHelper.appendByTail("6212462190000131545", 19));  // 名义借款人电子账户
                            text.append(FormatHelper.appendByTail("1", 1)); // 多种借款人模式标识
                        }else{
                            text.append(FormatHelper.appendByTail("", 19));  // 名义借款人电子账户
                            text.append(FormatHelper.appendByTail("0", 1)); // 多种借款人模式标识
                        }
                        if(borrow.getType() != 1){
                            text.append(FormatHelper.appendByTail("6212462190000131545", 19));  // 收款人
                            text.append(FormatHelper.appendByTail("1", 1));   //  多种借款人模式下使用
                        }else{
                            text.append(FormatHelper.appendByTail("", 19));  // 收款人
                            text.append(FormatHelper.appendByTail("0", 1));   //  多种借款人模式下使用
                        }

                        text.append(FormatHelper.appendByTail("", 100)); // 保留域
                        text.append(FormatHelper.appendByTail(borrowId, 100)); // 第三方平台保留使用,原样返回
                        borrowWriter.write(text.toString());
                        borrowWriter.write("\n");
                    } catch (Exception e) {
                        log.error("写入成功借款失败");
                    }
                }
            }
        } while (realSize == pageSize);
        try {
            if (borrowWriter != null) {
                borrowWriter.flush();
                borrowWriter.close();
            }

            if (errorBorrowWriter != null) {
                errorBorrowWriter.flush();
                errorBorrowWriter.close();
            }
        } catch (Exception e) {
            log.error("清楚数据", e);
        }
        log.info("标的迁移文件成功");
    }


    /**
     * 接收处理结果
     */
    public void postMigrateBorrowFile(String fileName) {
        File file = new File(String.format("%s/%s/%s", MIGRATE_PATH, BORROW_DIR, fileName));
        if (!file.exists()) {
            log.error("文件不存在");
            return;
        }

        BufferedReader reader = null;
        try {
            reader = Files.newReader(file, Charset.forName("gbk"));
        } catch (FileNotFoundException e) {
            log.error("读取文件异常", e);
            return;
        }

        List<Long> prodcutIdList = new ArrayList<>();
        File errorFile = new File(String.format("%s/%s/%s_error", MIGRATE_PATH, BORROW_DIR, fileName));
        final BufferedWriter errorWriter;
        try {
            errorWriter = Files.newWriter(errorFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("生成错误文件异常", e);
            return;
        }

        Stream<String> lines = reader.lines();
        lines.forEach((String item) -> {
            try {
                byte[] gbks = item.getBytes("gbk");
                String flag = FormatHelper.getStrForGBK(gbks, 271, 273);
                String idStr = FormatHelper.getStrForGBK(gbks, 10, 50);
                if (!"00".equals(flag)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(NumberHelper.toLong(idStr)).append("|").append(ERROR_MSG_DATA.get(flag));
                    try {
                        errorWriter.write(stringBuffer.toString());
                        errorWriter.newLine();
                    } catch (IOException e) {
                        log.error("写入错误数据", e);
                        return;
                    }
                } else {
                    prodcutIdList.add(NumberHelper.toLong(idStr));
                }
            } catch (Exception e) {
                log.error("获取失败", e);
            }
        });

        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", prodcutIdList.toArray())
                .build();

        Date nowDate =new Date() ;
        List<Borrow> borrowList = borrowService.findList(bs);
        borrowList.stream()
                .filter(borrow -> ObjectUtils.isEmpty(borrow.getProductId()))
                .forEach(borrow -> {
                    borrow.setProductId(borrow.getId().toString());
                    borrow.setTitularBorrowAccountId("6212462190000131545");
                    borrow.setUpdatedAt(nowDate);
                });
        borrowService.save(borrowList);
    }

    private static Map<String, String> ERROR_MSG_DATA = new HashMap<>();

    static {
        ERROR_MSG_DATA.put("01", "银行号不允许为空");
        ERROR_MSG_DATA.put("02", "批次号不允许为空");
        ERROR_MSG_DATA.put("03", "标的编号不允许为空");
        ERROR_MSG_DATA.put("04", "标的描述不允许为空");
        ERROR_MSG_DATA.put("05", "借款人电子账号不允许为空");
        ERROR_MSG_DATA.put("06", "借款金额不允许为空");
        ERROR_MSG_DATA.put("07", "借款金额不允许为非数字型");
        ERROR_MSG_DATA.put("09", "项目期限不能为空");
        ERROR_MSG_DATA.put("10", "预期年化收益率不允许为空");
        ERROR_MSG_DATA.put("11", "银行号与文件名不一致");
        ERROR_MSG_DATA.put("12", "文件名批次号与文件体不符");
        ERROR_MSG_DATA.put("13", "借款人账户不存在");
        ERROR_MSG_DATA.put("14", "借款人账户所属银行错误");
        ERROR_MSG_DATA.put("15", "借款人账户状态不正常");
        ERROR_MSG_DATA.put("19", "未找到借款人卡号对应手机号记录");
        ERROR_MSG_DATA.put("20", "第三方平台验证不通过");
        ERROR_MSG_DATA.put("21", "未找到借款人账户客户信息");
        ERROR_MSG_DATA.put("22", "标的信息已存在，不允许重复录入");
        ERROR_MSG_DATA.put("23", "付息方式不合法");
        ERROR_MSG_DATA.put("24", "利息每月支付日必送");
        ERROR_MSG_DATA.put("25", "黑名单客户无法录入标的信息");
        ERROR_MSG_DATA.put("26", "担保人卡片不存在");
        ERROR_MSG_DATA.put("27", "担保人卡片不属于该发卡行");
        ERROR_MSG_DATA.put("28", "担保人卡片跟借款人卡片非同产品");
        ERROR_MSG_DATA.put("29", "未找到此担保人卡号对应的账户记录");
        ERROR_MSG_DATA.put("30", "担保人账户已销户");
        ERROR_MSG_DATA.put("31", "未找到担保人的客户信息");
        ERROR_MSG_DATA.put("32", "名义借款人卡片不存在");
        ERROR_MSG_DATA.put("33", "名义借款人卡片不属于该发卡行");
        ERROR_MSG_DATA.put("34", "名义借款人卡片跟借款人卡片非同产品");
        ERROR_MSG_DATA.put("35", "未找到名义借款人卡号对应的账户记录");
        ERROR_MSG_DATA.put("36", "名义借款人账户已销户");
        ERROR_MSG_DATA.put("37", "未找到名义借款人的客户记录");
        ERROR_MSG_DATA.put("38", "未找到名义借款人卡片对应记录");
        ERROR_MSG_DATA.put("41", "收款人卡片不存在");
        ERROR_MSG_DATA.put("45", "受托支付标志非法");
        ERROR_MSG_DATA.put("46", "多种借款人模式下收款人电子账号必送");
        ERROR_MSG_DATA.put("47", "未找到此担保人卡号对应的账户记录");
        ERROR_MSG_DATA.put("48", "多种借款人标识非法");
        ERROR_MSG_DATA.put("49", "未找到担保人卡片对应记录");
        ERROR_MSG_DATA.put("50", "收款人卡片不属于该发卡行");
        ERROR_MSG_DATA.put("51", "收款人卡片跟借款人卡片非同产品");
        ERROR_MSG_DATA.put("52", "未找到收款人卡号对应的账户记录");
        ERROR_MSG_DATA.put("53", "收款人账户已销户");
        ERROR_MSG_DATA.put("54", "未找到收款人的客户记录");
        ERROR_MSG_DATA.put("55", "未找到收款人卡片对应记录");


    }
}
