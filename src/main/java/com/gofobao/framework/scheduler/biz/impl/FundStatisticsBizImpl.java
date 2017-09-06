package com.gofobao.framework.scheduler.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.service.AleveService;
import com.gofobao.framework.financial.service.EveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.FormatHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FundStatisticsBizImpl implements FundStatisticsBiz {

    @Autowired
    NewAssetLogService newAssetLogService;

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
    EveService eveService;

    @Autowired
    AleveService aleveService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    UserService userService;

    @Autowired
    AssetService assetService;

    @Autowired
    JixinManager jixinManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doEve(String date) throws Exception {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, date);
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            throw new Exception(String.format("EVE: %s 下载失败", fileName));
        }
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        // 保存进数据库
        Date nowDate = new Date();
        bufferedReader.lines().forEach(line -> {
            try {
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                String acqcode = FormatHelper.getStrForUTF8(bytes, 0, 11);
                String seqno = FormatHelper.getStrForUTF8(bytes, 11, 17);
                String sendt = FormatHelper.getStrForUTF8(bytes, 17, 27);
                String cardnbr = FormatHelper.getStrForUTF8(bytes, 27, 46);
                String amount = FormatHelper.getStrForUTF8(bytes, 46, 58);
                String crflag = FormatHelper.getStrForUTF8(bytes, 58, 59);
                String msgtype = FormatHelper.getStrForUTF8(bytes, 59, 63);
                String proccode = FormatHelper.getStrForUTF8(bytes, 63, 69);
                String mertype = FormatHelper.getStrForUTF8(bytes, 69, 73);
                String term = FormatHelper.getStrForUTF8(bytes, 73, 81);
                String retseqno = FormatHelper.getStrForUTF8(bytes, 81, 93);
                String conmode = FormatHelper.getStrForUTF8(bytes, 93, 95);
                String autresp = FormatHelper.getStrForUTF8(bytes, 95, 101);
                String forcode = FormatHelper.getStrForUTF8(bytes, 101, 112);
                String clrdate = FormatHelper.getStrForUTF8(bytes, 112, 116);
                String oldseqno = FormatHelper.getStrForUTF8(bytes, 117, 122);
                String openbrno = FormatHelper.getStrForUTF8(bytes, 122, 128);
                String tranbrno = FormatHelper.getStrForUTF8(bytes, 128, 134);
                String ervind = FormatHelper.getStrForUTF8(bytes, 134, 135);
                String transtype = FormatHelper.getStrForUTF8(bytes, 135, 139);
                Eve eve = new Eve();
                eve.setAcqcode(acqcode);
                eve.setSeqno(seqno);
                eve.setSendt(sendt);
                eve.setCardnbr(cardnbr);
                eve.setAmount(new Double(new Long(amount) / 100D).toString());  //保证元的问题
                eve.setCrflag(crflag);
                eve.setMsgtype(msgtype);
                eve.setProccode(proccode);
                eve.setMertype(mertype);
                eve.setTerm(term);
                eve.setRetseqno(retseqno);
                eve.setConmode(conmode);
                eve.setAutresp(autresp);
                eve.setForcode(forcode);
                eve.setClrdate(clrdate);
                eve.setOldseqno(oldseqno);
                eve.setOpenbrno(openbrno);
                eve.setTranbrno(tranbrno);
                eve.setErvind(ervind);
                eve.setTranstype(transtype);
                eve.setQueryDate(date);
                eve.setCreateAt(nowDate);
                List<Eve> eveList = eveService.findByRetseqnoAndSeqno(eve.getRetseqno(), eve.getSeqno());
                if (!CollectionUtils.isEmpty(eveList)) {
                    log.error(String.format("EVE重复插入: %s", line));
                    return;
                }
                eveService.save(eve);
            } catch (Exception e) {
                log.error("保存eve到数据库失败", e);
            }
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doAleve(String date) throws Exception {
        String fileName = String.format("%s-ALEVE%s-%s", bankNo, productNo, date);
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.error(String.format("ALEVE: %s下载失败", fileName));
            return false;
        }
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        // 保存进数据库
        Date nowDate = new Date();
        bufferedReader.lines().forEach(line -> {
            try {
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                String bank = FormatHelper.getStrForUTF8(bytes, 0, 4);
                String cardnbr = FormatHelper.getStrForUTF8(bytes, 4, 23);
                String amount = FormatHelper.getStrForUTF8(bytes, 23, 40);
                String curNum = FormatHelper.getStrForUTF8(bytes, 40, 43);
                String crflag = FormatHelper.getStrForUTF8(bytes, 43, 44);
                String valdate = FormatHelper.getStrForUTF8(bytes, 44, 52);
                String inpdate = FormatHelper.getStrForUTF8(bytes, 52, 60);
                String reldate = FormatHelper.getStrForUTF8(bytes, 60, 68);
                String inptime = FormatHelper.getStrForUTF8(bytes, 68, 76);
                String tranno = FormatHelper.getStrForUTF8(bytes, 76, 82);
                String oriTranno = FormatHelper.getStrForUTF8(bytes, 82, 88);
                String transtype = FormatHelper.getStrForUTF8(bytes, 88, 92);
                String desline = FormatHelper.getStrForUTF8(bytes, 92, 134);
                String currBal = FormatHelper.getStrForUTF8(bytes, 134, 151);
                String forcardnbr = FormatHelper.getStrForUTF8(bytes, 151, 170);
                String revind = FormatHelper.getStrForUTF8(bytes, 170, 171);
                String resv = FormatHelper.getStrForUTF8(bytes, 171, 371);

                Aleve aleve = new Aleve();
                aleve.setBank(bank);
                aleve.setCardnbr(cardnbr);
                aleve.setAmount(new Double(new Long(amount) / 100D).toString());  // 保证元的问题
                aleve.setCurNum(curNum);
                aleve.setCrflag(crflag);
                aleve.setValdate(valdate);
                aleve.setInpdate(inpdate);
                aleve.setReldate(reldate);
                aleve.setInptime(inptime);
                aleve.setTranno(tranno);
                aleve.setOriTranno(oriTranno);
                aleve.setTranstype(transtype);
                aleve.setTranstype(transtype);
                aleve.setDesline(desline);
                aleve.setCurrBal(new Double(new Long(currBal) / 100D).toString()); // 保证元的问题
                aleve.setForcardnbr(forcardnbr);
                aleve.setRevind(revind);
                aleve.setResv(resv);
                aleve.setCreateAt(nowDate);
                aleve.setQueryDate(date);
                List<Aleve> aleves = aleveService.findByTranno(aleve.getTranno());
                if (!CollectionUtils.isEmpty(aleves)) {
                    log.error(String.format("ALEVE 重数据: %s", line));
                }
                aleveService.save(aleve);
            } catch (Exception e) {
                log.error("保存eve到数据库失败", e);
            }
        });

        return true;
    }

    @Override
    public void downFundFile(HttpServletResponse httpServletResponse, String date) throws Exception {
        Date nowDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Date startDate = DateHelper.beginOfDate(nowDate);
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(startDate, 1));

        log.info("获取本地资金流水");
        // 获取用户
        Specification<NewAssetLog> assetLogSpecification = Specifications.
                <NewAssetLog>and()
                .between("createTime", new Range<>(startDate, endDate))
                .build();

        Long assetAccount = newAssetLogService.count(assetLogSpecification);
        if (assetAccount == 0) {
            log.error("资金记录为空");
            return;
        }

        // 创建本地资金流水
        XSSFWorkbook xwb = new XSSFWorkbook();//创建excel表格的工作空间
        int pageSize = 1000, pageIndex = 0;
        int pageIndexTotal = assetAccount.intValue() / pageSize;
        pageIndexTotal = assetAccount.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        XSSFSheet localSheet = xwb.createSheet("本地资金流水");
        createLocalTitle(localSheet);  // 创建本地title标题
        int localIndex = 0;
        log.info(String.format("资金记录总条数: %s, 总遍历条数: %s", assetAccount, pageIndexTotal));
        for (; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id")));
            Page<NewAssetLog> page = newAssetLogService.findAll(assetLogSpecification, pageable);
            List<NewAssetLog> content = page.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }

            for (NewAssetLog newAssetLog : content) {
                localIndex++;
                long userId = newAssetLog.getUserId();
                UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
                Preconditions.checkNotNull(userThirdAccount, "FundStaticsBizImpl.downFundFile: userThirdAccount is null");
                XSSFRow tempRow = localSheet.createRow(localIndex);
                tempRow.createCell(0).setCellValue(userThirdAccount.getName());
                tempRow.createCell(1).setCellValue(userThirdAccount.getAccountId());
                tempRow.createCell(2).setCellValue(newAssetLog.getLocalSeqNo());
                tempRow.createCell(3).setCellValue(StringHelper.formatDouble(newAssetLog.getOpMoney() / 100D, false));
                tempRow.createCell(4).setCellValue(StringHelper.formatDouble(newAssetLog.getCurrMoney() / 100D, false));
                tempRow.createCell(5).setCellValue(newAssetLog.getLocalType());
                tempRow.createCell(6).setCellValue(newAssetLog.getOpName());
                tempRow.createCell(7).setCellValue(newAssetLog.getPlatformType());
                tempRow.createCell(8).setCellValue(newAssetLog.getTxFlag());
                tempRow.createCell(9).setCellValue(DateHelper.dateToString(newAssetLog.getCreateTime()));
            }
        }

        log.info("即信资金流水");
        XSSFSheet jixinSheet = xwb.createSheet("即信资金流水");
        createJixinTitle(jixinSheet);
        // 获取用户
        Specification<Aleve> aleveSpecification = Specifications.
                <Aleve>and()
                .eq("queryDate", date)
                .build();

        Long aleveAount = aleveService.count(aleveSpecification);
        pageSize = 1000;
        pageIndex = 0;
        pageIndexTotal = aleveAount.intValue() / pageSize;
        pageIndexTotal = aleveAount.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        localIndex = 0;
        log.info(String.format("即信资金流水总条数: %s, 总遍历条数: %s", aleveAount, pageIndexTotal));
        for (; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            Page<Aleve> page = aleveService.findAll(aleveSpecification, pageable);
            List<Aleve> content = page.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            for (Aleve aleve : content) {
                localIndex++;
                String accountId = aleve.getCardnbr();
                UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(accountId);
                Preconditions.checkNotNull(userThirdAccount, "FundStaticsBizImpl.downFundFile: userThirdAccount is null");
                XSSFRow tempRow = jixinSheet.createRow(localIndex);
                tempRow.createCell(0).setCellValue(userThirdAccount.getName());
                tempRow.createCell(1).setCellValue(userThirdAccount.getAccountId());
                tempRow.createCell(2).setCellValue(String.format("%s%s%s", aleve.getInpdate(), aleve.getInptime(), aleve.getTranno()));
                tempRow.createCell(3).setCellValue(StringHelper.formatDouble(new Double(aleve.getAmount()) / 100D, false));
                tempRow.createCell(4).setCellValue(StringHelper.formatDouble(new Double(aleve.getCurrBal()) / 100D, false));
                tempRow.createCell(5).setCellValue(aleve.getTranstype());
                tempRow.createCell(6).setCellValue(aleve.getDesline());
                tempRow.createCell(7).setCellValue(aleve.getCrflag());
                tempRow.createCell(8).setCellValue(aleve.getRevind());
                tempRow.createCell(9).setCellValue(DateHelper.dateToString(aleve.getCreateAt()));
            }
        }

        log.info("资金比对");
        XSSFSheet assetSheel = xwb.createSheet("资金比对");
        createAssetTitle(assetSheel);

        // 用户资金比对
        Specification<UserThirdAccount> userThirdAccountSpecification = Specifications
                .<UserThirdAccount>and()
                .build();
        Long userCount = userThirdAccountService.count(userThirdAccountSpecification);
        pageSize = 2000;
        pageIndex = 0;
        pageIndexTotal = userCount.intValue() / pageSize;
        pageIndexTotal = userCount.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        localIndex = 0;
        for (; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            Page<UserThirdAccount> all = userThirdAccountService.findAll(pageable);
            if (CollectionUtils.isEmpty(all.getContent())) {
                break;
            }
            List<UserThirdAccount> content = all.getContent();
            for (UserThirdAccount userThirdAccount : content) {
                localIndex++;
                Asset asset = assetService.findByUserIdLock(userThirdAccount.getUserId());
                XSSFRow tempRow = jixinSheet.createRow(localIndex);
                tempRow.createCell(0).setCellValue(userThirdAccount.getName());
                tempRow.createCell(1).setCellValue(userThirdAccount.getAccountId());
                tempRow.createCell(2).setCellValue(StringHelper.formatDouble(asset.getUseMoney() / 100D, false));
                tempRow.createCell(3).setCellValue(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney()) / 100D, false));

                BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
                balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
                BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
                if (!ObjectUtils.isEmpty(balanceQueryResponse) && JixinResultContants.SUCCESS.equals(balanceQueryResponse)) {
                    tempRow.createCell(4).setCellValue(balanceQueryResponse.getAvailBal());
                    tempRow.createCell(5).setCellValue(balanceQueryResponse.getCurrBal());
                } else {
                    tempRow.createCell(4).setCellValue("查询超时");
                    tempRow.createCell(4).setCellValue("查询超时");
                }
            }
        }

        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(date.getBytes("utf-8"), "iso8859-1"));// 设置头信息
        httpServletResponse.setContentType("application/ynd.ms-excel;charset=UTF-8");
        OutputStream out = httpServletResponse.getOutputStream();
        xwb.write(out);// 进行输出，下载到本地
        out.flush();
        out.close();
    }

    private void createAssetTitle(XSSFSheet assetSheel) {
        XSSFRow titleRow = assetSheel.createRow(0);
        XSSFCell realName = titleRow.createCell(0);
        realName.setCellValue("真实姓名");

        XSSFCell accountId = titleRow.createCell(1);
        accountId.setCellValue("电子账户");

        XSSFCell amount = titleRow.createCell(2);
        amount.setCellValue("平台可用金额");

        XSSFCell totalAmount = titleRow.createCell(3);
        totalAmount.setCellValue("平台账户账户总额");

        XSSFCell jixinAmount = titleRow.createCell(4);
        jixinAmount.setCellValue("存管可用金额");

        XSSFCell jixinTotalAmount = titleRow.createCell(5);
        jixinTotalAmount.setCellValue("存管账户账户总额");
    }

    private void createJixinTitle(XSSFSheet jixinSheet) {
        XSSFRow titleRow = jixinSheet.createRow(0);
        XSSFCell realName = titleRow.createCell(0);
        realName.setCellValue("真实姓名");

        XSSFCell accountId = titleRow.createCell(1);
        accountId.setCellValue("电子账户");

        XSSFCell seqNo = titleRow.createCell(2);
        seqNo.setCellValue("变动流水");

        XSSFCell amount = titleRow.createCell(3);
        amount.setCellValue("操作金额");

        XSSFCell totalAmount = titleRow.createCell(4);
        totalAmount.setCellValue("账户总额");

        XSSFCell jixinType = titleRow.createCell(5);
        jixinType.setCellValue("即信变动类型");

        XSSFCell localTypeName = titleRow.createCell(6);
        localTypeName.setCellValue("即信变动类型名称");

        XSSFCell flag = titleRow.createCell(7);
        flag.setCellValue("资金变动标识");

        XSSFCell returnCell = titleRow.createCell(8);
        returnCell.setCellValue("是否拨正");

        XSSFCell createTime = titleRow.createCell(9);
        createTime.setCellValue("操作时间");
    }

    private void createLocalTitle(XSSFSheet localSheet) {
        XSSFRow titleRow = localSheet.createRow(0);
        XSSFCell realName = titleRow.createCell(0);
        realName.setCellValue("真实姓名");

        XSSFCell accountId = titleRow.createCell(1);
        accountId.setCellValue("电子账户");

        XSSFCell seqNo = titleRow.createCell(2);
        seqNo.setCellValue("变动流水");

        XSSFCell amount = titleRow.createCell(3);
        amount.setCellValue("操作金额");

        XSSFCell totalAmount = titleRow.createCell(4);
        totalAmount.setCellValue("账户总额");

        XSSFCell localType = titleRow.createCell(5);
        localType.setCellValue("平台变动类型类型");

        XSSFCell localTypeName = titleRow.createCell(6);
        localTypeName.setCellValue("平台变动类型类型名称");

        XSSFCell jixinType = titleRow.createCell(7);
        jixinType.setCellValue("即信变动类型");

        XSSFCell flag = titleRow.createCell(8);
        flag.setCellValue("资金变动标识");

        XSSFCell createTime = titleRow.createCell(9);
        createTime.setCellValue("操作时间");
    }
}
