package com.gofobao.framework.financial.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.service.CurrentIncomeLogService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.FormatHelper;
import com.google.common.io.Files;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class NewAleveBizImpl implements NewAleveBiz {
    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    UserService userService;

    @Autowired
    NewAleveService newAleveService;

    @Autowired
    JixinFileManager jixinFileManager;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Value("${jixin.product-no}")
    String productNo;

    @Value("${jixin.bank-no}")
    String bankNo;

    @Value("${jixin.save-file-path}")
    String filePath;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    CurrentIncomeLogService currentIncomeLogService;

    @Autowired
    AssetBiz assetBiz;

    @Override
    public boolean downloadNewAleveFileAndImportDatabase(String date) {
        log.info("===========================================");
        log.info(String.format("ALEVE调度启动, 时间: %s", date));
        log.info("===========================================");

        try {
            String fileName = String.format("%s-ALEVE%s-%s", bankNo, productNo, date);
            boolean downloadState = jixinFileManager.download(fileName);
            if (!downloadState) {
                throw new Exception("ALEVE下载失败");
            }

            importDatabase(date, fileName);
            return true;
        } catch (Exception ex) {
            exceptionEmailHelper.sendErrorMessage("ALEVE文件下载失败", String.format("时间: %s", date));
            log.error("ALEVE调度执行失败", ex);
        }
        return false;
    }

    /**
     * 将aleve 文件入库
     *
     * @param date
     * @param fileName
     * @throws FileNotFoundException
     */
    @Override
    public void importDatabase(String date, String fileName) throws FileNotFoundException {
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);

        Date opDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Date endDate = DateHelper.stringToDate("2017-09-21 00:00:00");
        if (DateHelper.diffInDays(endDate, opDate, false) > 0) {  // 老版本系统
            log.info("======================================");
            log.info("进入老版本");
            log.info("======================================");
            importDatabaseOfAleveFor1_1_0(date, bufferedReader);
        } else {
            log.info("======================================");
            log.info("进入新版本");
            log.info("======================================");
            importDatabaseOfAleveFor1_1_4(date, bufferedReader);
        }
    }

    private void importDatabaseOfAleveFor1_1_0(String date, BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        lines.forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                try {
                    byte[] bytes = line.getBytes("gbk");
                    String bank = FormatHelper.getStrForGBK(bytes, 0, 4); // 银行号
                    String cardnbr = FormatHelper.getStrForGBK(bytes, 4, 23); // 电子账号
                    String amount = FormatHelper.getStrForGBK(bytes, 23, 40); // 交易金额
                    String cur_num = FormatHelper.getStrForGBK(bytes, 40, 43); // 货币代码
                    String crflag = FormatHelper.getStrForGBK(bytes, 43, 44); // 交易金额符号
                    String valdate = FormatHelper.getStrForGBK(bytes, 44, 52); // 入帐日期
                    String inpdate = FormatHelper.getStrForGBK(bytes, 52, 60); // 交易日期
                    String reldate = FormatHelper.getStrForGBK(bytes, 60, 68); // 自然日期
                    String inptime = FormatHelper.getStrForGBK(bytes, 68, 76); // 交易时间
                    String tranno = FormatHelper.getStrForGBK(bytes, 76, 82); // 交易流水号
                    String ori_tranno = FormatHelper.getStrForGBK(bytes, 82, 88); // 关联交易流水号
                    String transtype = FormatHelper.getStrForGBK(bytes, 88, 92); // 交易类型
                    String desline = FormatHelper.getStrForGBK(bytes, 92, 134); // 交易描述
                    String curr_bal = FormatHelper.getStrForGBK(bytes, 134, 151); // 交易后余额
                    String forcardnbr = FormatHelper.getStrForGBK(bytes, 151, 170); // 对手交易帐号
                    String revind = FormatHelper.getStrForGBK(bytes, 170, 171); // 冲正、撤销标志
                    String resv = FormatHelper.getStrForGBK(bytes, 171, 371); // 保留域

                    // 资金处理
                    amount = MoneyHelper.divide(amount, "100", 2);  // 将分转成元
                    curr_bal = MoneyHelper.divide(curr_bal, "100", 2);//  将分转成元
                    NewAleve newAleve = new NewAleve();
                    newAleve.setAmount(amount);
                    newAleve.setBank(bank);
                    newAleve.setCardnbr(cardnbr);
                    newAleve.setCrflag(crflag);
                    newAleve.setCurNum(cur_num);
                    newAleve.setDesline(desline);
                    newAleve.setCurrBal(curr_bal);
                    newAleve.setValdate(valdate);
                    newAleve.setInpdate(inpdate);
                    newAleve.setReldate(reldate);
                    newAleve.setInptime(inptime);
                    newAleve.setTranno(tranno);
                    newAleve.setOriTranno(ori_tranno);
                    newAleve.setTranstype(transtype);
                    newAleve.setForcardnbr(forcardnbr);
                    newAleve.setRevind(revind);
                    newAleve.setResv(resv);
                    newAleve.setQueryTime(date);

                    // 查找输入输出
                    NewAleve existsNewAleve = newAleveService.findTopByReldateAndInptimeAndTranno(reldate, inptime, tranno);
                    if (ObjectUtils.isEmpty(existsNewAleve)) {
                        newAleveService.save(newAleve);
                    }
                } catch (Exception ex) {
                    log.error("aleve 保存数据库异常", ex);
                }
            }
        });
    }

    /**
     * 文件传输版本在1.1.4 之后
     * 传输文件时间导入大于2017年9月20号以后启用
     *
     * @param date
     * @param bufferedReader
     */
    private void importDatabaseOfAleveFor1_1_4(String date, BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        lines.forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                try {
                    byte[] bytes = line.getBytes("gbk");
                    String bank = FormatHelper.getStrForGBK(bytes, 0, 4); // 银行号
                    String cardnbr = FormatHelper.getStrForGBK(bytes, 4, 23); // 电子账号
                    String amount = FormatHelper.getStrForGBK(bytes, 23, 40); // 交易金额
                    String cur_num = FormatHelper.getStrForGBK(bytes, 40, 43); // 货币代码
                    String crflag = FormatHelper.getStrForGBK(bytes, 43, 44); // 交易金额符号
                    String valdate = FormatHelper.getStrForGBK(bytes, 44, 52); // 入帐日期
                    String inpdate = FormatHelper.getStrForGBK(bytes, 52, 60); // 交易日期
                    String reldate = FormatHelper.getStrForGBK(bytes, 60, 68); // 自然日期
                    String inptime = FormatHelper.getStrForGBK(bytes, 68, 76); // 交易时间
                    String tranno = FormatHelper.getStrForGBK(bytes, 76, 82); // 交易流水号
                    String ori_tranno = FormatHelper.getStrForGBK(bytes, 82, 88); // 关联交易流水号
                    String transtype = FormatHelper.getStrForGBK(bytes, 88, 92); // 交易类型
                    String desline = FormatHelper.getStrForGBK(bytes, 92, 134); // 交易描述
                    String curr_bal = FormatHelper.getStrForGBK(bytes, 134, 151); // 交易后余额
                    String forcardnbr = FormatHelper.getStrForGBK(bytes, 151, 170); // 对手交易帐号
                    String revind = FormatHelper.getStrForGBK(bytes, 170, 171); // 冲正、撤销标志
                    String accchg = FormatHelper.getStrForGBK(bytes, 171, 172); // 交易标识
                    String seqno = FormatHelper.getStrForGBK(bytes, 172, 178); // 系统跟踪号
                    String ori_num = FormatHelper.getStrForGBK(bytes, 178, 184); // 原交易流水号
                    String resv = FormatHelper.getStrForGBK(bytes, 184, 371); // 保留域

                    // 资金处理
                    amount = MoneyHelper.divide(amount, "100", 2);  // 将分转成元
                    curr_bal = MoneyHelper.divide(curr_bal, "100", 2);//  将分转成元
                    NewAleve newAleve = new NewAleve();
                    newAleve.setAccchg(accchg);
                    newAleve.setAmount(amount);
                    newAleve.setBank(bank);
                    newAleve.setCardnbr(cardnbr);
                    newAleve.setCrflag(crflag);
                    newAleve.setCurNum(cur_num);
                    newAleve.setDesline(desline);
                    newAleve.setCurrBal(curr_bal);
                    newAleve.setValdate(valdate);
                    newAleve.setInpdate(inpdate);
                    newAleve.setReldate(reldate);
                    newAleve.setInptime(inptime);
                    newAleve.setTranno(tranno);
                    newAleve.setOriTranno(ori_tranno);
                    newAleve.setTranstype(transtype);
                    newAleve.setForcardnbr(forcardnbr);
                    newAleve.setRevind(revind);
                    newAleve.setSeqno(seqno);
                    newAleve.setOriNum(ori_num);
                    newAleve.setResv(resv);
                    newAleve.setQueryTime(date);

                    // 查找输入输出
                    NewAleve existsNewAleve = newAleveService.findTopByReldateAndInptimeAndTranno(reldate, inptime, tranno);
                    if (ObjectUtils.isEmpty(existsNewAleve)) {
                        newAleveService.save(newAleve);
                    }
                } catch (Exception ex) {
                    log.error("aleve 保存数据库异常", ex);
                }
            }
        });
    }

    @Override
    public void calculationCurrentInterest(String date) throws Exception {
        try {
            Specification<NewAleve> specification = Specifications
                    .<NewAleve>and()
                    .eq("transtype", "5500")  // 活期收益
                    .eq("queryTime", date)  // 日期
                    .build();
            Long count = newAleveService.count(specification);
            if (count <= 0) {
                log.warn(String.format("[当前活期收益]"));
                return;
            }
            int pageSize = 20, pageIndex = 0, pageIndexTotal = 0;
            pageIndexTotal = count.intValue() / pageSize;
            pageIndexTotal = count.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;

            for (; pageIndex < pageIndexTotal; pageIndex++) {
                log.info("执行积分派发" + pageIndex);
                Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
                Page<NewAleve> newAleves = newAleveService.findAll(specification, pageable);
                List<NewAleve> data = newAleves.getContent();
                if (ObjectUtils.isEmpty(data)) {
                    log.warn("当期活期收益数据为空");
                    break;
                }

                for (NewAleve item : data) {
                    String cardnbr = item.getCardnbr();  // 账户
                    String amount = item.getAmount(); // 金额 元
                    String accountId = StringUtils.trimAllWhitespace(cardnbr);
                    String money = StringUtils.trimAllWhitespace(amount);
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(accountId);
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        log.error(String.format("活期收益派发, 开户信息为空: %s", accountId));
                        exceptionEmailHelper.sendErrorMessage("活期收益派发", String.format("查询当前开户账户为空, 数据[ 账户:%s, 时间:%s ]", accountId, date));  // 对于特殊用户进行特殊处理
                        continue;
                    }

                    try {
                        log.info("派发活期" + new Gson().toJson(item));
                        assetBiz.doAssetChangeByCurrentInterest(item, userThirdAccount, money);
                    } catch (Exception e) {
                        log.error("活期收益资金变动异常", e);
                        exceptionEmailHelper.sendException("活期收益派发", e);
                        throw new Exception(e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("派发用户活期收益异常", e);
            exceptionEmailHelper.sendException("派发用户活期收益异常", e);
            throw new Exception(e);
        }
    }

    public static List<Long> PUBLISH_ID = new ArrayList<>(100);

    static {
       PUBLISH_ID.add(22L);
        PUBLISH_ID.add(1L);
        PUBLISH_ID.add(197L);
        PUBLISH_ID.add(375L);
        PUBLISH_ID.add(544L);
        PUBLISH_ID.add(901L);
        PUBLISH_ID.add(1190L);
        PUBLISH_ID.add(1297L);
        PUBLISH_ID.add(1309L);
        PUBLISH_ID.add(1766L);
        PUBLISH_ID.add(1947L);
        PUBLISH_ID.add(2330L);
        PUBLISH_ID.add(2375L);
        PUBLISH_ID.add(2552L);
        PUBLISH_ID.add(2623L);
        PUBLISH_ID.add(3083L);
        PUBLISH_ID.add(3449L);
        PUBLISH_ID.add(3460L);
        PUBLISH_ID.add(3612L);
        PUBLISH_ID.add(3850L);
        // PUBLISH_ID.add(3851L); 已修补
        PUBLISH_ID.add(4022L);
        PUBLISH_ID.add(4213L);
        PUBLISH_ID.add(4632L);
        PUBLISH_ID.add(4811L);
        PUBLISH_ID.add(5930L);
        PUBLISH_ID.add(5948L);
        PUBLISH_ID.add(8628L);
        PUBLISH_ID.add(8663L);
        PUBLISH_ID.add(9769L);
        PUBLISH_ID.add(9789L);
        PUBLISH_ID.add(10513L);
        PUBLISH_ID.add(12489L);
        PUBLISH_ID.add(13345L);
        PUBLISH_ID.add(13378L);
        PUBLISH_ID.add(22002L);
        PUBLISH_ID.add(24313L);
        PUBLISH_ID.add(26622L);
        PUBLISH_ID.add(31323L);
        PUBLISH_ID.add(41231L);
        PUBLISH_ID.add(41737L);
        PUBLISH_ID.add(42334L);
        PUBLISH_ID.add(46908L);
        PUBLISH_ID.add(49824L);
        PUBLISH_ID.add(50692L);
        PUBLISH_ID.add(51450L);
        PUBLISH_ID.add(59087L);
        PUBLISH_ID.add(60002L);
        PUBLISH_ID.add(62654L);
        PUBLISH_ID.add(62844L);
        PUBLISH_ID.add(62959L);
        PUBLISH_ID.add(63588L);
        PUBLISH_ID.add(26108L);
        PUBLISH_ID.add(87134L);
        PUBLISH_ID.add(92571L);
        PUBLISH_ID.add(96632L);
        PUBLISH_ID.add(63469L);
        PUBLISH_ID.add(108377L);
        PUBLISH_ID.add(110825L);
        PUBLISH_ID.add(110866L);
        PUBLISH_ID.add(110907L);
    }


    @Override
    public void simpleDownload(String date) {
        String fileName = String.format("%s-ALEVE%s-%s", bankNo, productNo, date);
        log.info("========================");
        log.info("执行下载文件:" + fileName);
        log.info("========================");
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.error("ALEVE文件下载失败");
        }
    }

    @Override
    public void adminPublishCurrentInterest(String date) throws Exception {
        try {
            Specification<NewAleve> specification = Specifications
                    .<NewAleve>and()
                    .eq("transtype", "5500")  // 活期收益
                    .eq("queryTime", date)  // 日期
                    .build();
            Long count = newAleveService.count(specification);
            if (count <= 0) {
                log.warn(String.format("[当前活期收益]"));
                return;
            }
            int pageSize = 20, pageIndex = 0, pageIndexTotal = 0;
            pageIndexTotal = count.intValue() / pageSize;
            pageIndexTotal = count.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;

            for (; pageIndex < pageIndexTotal; pageIndex++) {
                log.info("后台派发积分派发" + pageIndex);
                Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
                Page<NewAleve> newAleves = newAleveService.findAll(specification, pageable);
                List<NewAleve> data = newAleves.getContent();
                if (ObjectUtils.isEmpty(data)) {
                    log.warn("当期活期收益数据为空");
                    break;
                }

                for (NewAleve item : data) {
                    String cardnbr = item.getCardnbr();  // 账户
                    String amount = item.getAmount(); // 金额 元
                    String accountId = StringUtils.trimAllWhitespace(cardnbr);
                    String money = StringUtils.trimAllWhitespace(amount);
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(accountId);
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        log.error(String.format("活期收益派发, 开户信息为空: %s", accountId));
                        exceptionEmailHelper.sendErrorMessage("活期收益派发", String.format("查询当前开户账户为空, 数据[ 账户:%s, 时间:%s ]", accountId, date));  // 对于特殊用户进行特殊处理
                        continue;
                    }

                    // 判断是否允许派发
                    if (!PUBLISH_ID.contains(userThirdAccount.getUserId())) {
                        log.error("=================================================");
                        log.error("系统不允许派发" + userThirdAccount.getUserId());
                        log.error("=================================================");
                        continue;
                    }

                    try {
                        log.info("派发活期" + new Gson().toJson(item));
                        assetBiz.doAssetChangeByCurrentInterestSpecial(item, userThirdAccount, money);
                    } catch (Exception e) {
                        log.error("活期收益资金变动异常", e);
                        exceptionEmailHelper.sendException("活期收益派发", e);
                        throw new Exception(e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("派发用户活期收益异常", e);
            exceptionEmailHelper.sendException("派发用户活期收益异常", e);
            throw new Exception(e);
        }
    }


}
