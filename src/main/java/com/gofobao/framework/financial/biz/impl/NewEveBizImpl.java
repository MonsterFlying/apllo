package com.gofobao.framework.financial.biz.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.financial.biz.JixinAssetBiz;
import com.gofobao.framework.financial.biz.NewEveBiz;
import com.gofobao.framework.financial.entity.LocalAndRemoteAssetInfo;
import com.gofobao.framework.financial.entity.LocalRecord;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.entity.RemoteRecord;
import com.gofobao.framework.financial.service.JixinAssetService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.project.TranTypeHelper;
import com.gofobao.framework.migrate.FormatHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class NewEveBizImpl implements NewEveBiz {

    @Autowired
    NewAssetLogService newAssetLogService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    JixinFileManager jixinFileManager;

    @Autowired
    private JavaMailSender mailSender;

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
    JixinAssetBiz jixinAssetBiz;

    @Autowired
    JixinAssetService jixinAssetService;

    @Override
    public boolean downloadEveFileAndSaveDB(String date) {
        log.info("===========================================");
        log.info(String.format("EVE调度启动, 时间: %s", date));
        log.info("===========================================");

        try {
            String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, date);
            boolean downloadState = jixinFileManager.download(fileName);
            if (!downloadState) {
                throw new Exception("EVE下载失败");
            }
            importEveDataToDatabase(date, fileName);
            return true;
        } catch (Exception ex) {
            exceptionEmailHelper.sendErrorMessage("EVE文件下载失败", String.format("时间: %s", date));
            log.error("EVE调度执行失败", ex);
        }
        return false;
    }

    /**
     * 导入数据库
     *
     * @param date
     * @param fileName
     * @throws FileNotFoundException
     */
    @Override
    public void importEveDataToDatabase(String date, String fileName) throws FileNotFoundException {
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        Date opDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Date endDate = DateHelper.stringToDate("2017-09-21 00:00:00");
        if (DateHelper.diffInDays(endDate, opDate, false) > 0) {  // 老版本系统
            log.info("======================================");
            log.info("进入老版本");
            log.info("======================================");
            importDatabaseOfEveFor1_1_0(date, bufferedReader);
        } else {
            log.info("======================================");
            log.info("进入新版本");
            log.info("======================================");
            importDatabaseOfEveFor1_1_4(date, bufferedReader);
        }
    }

    /**
     * 传输协议 1.1.4 版本的导入
     * 传输文件时间导入大于2017年9月20号以后启用
     *
     * @param date
     * @param bufferedReader
     */
    private void importDatabaseOfEveFor1_1_4(String date, BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        Calendar calendar = Calendar.getInstance();
        Date opDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        calendar.setTime(opDate);
        int year = calendar.get(Calendar.YEAR);
        lines.forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                try {
                    byte[] bytes = line.getBytes("gbk");
                    String forcode = FormatHelper.getStrForGBK(bytes, 0, 11); // 发送方标识码
                    String seqno = FormatHelper.getStrForGBK(bytes, 11, 17); // 系统跟踪号
                    String cendt = FormatHelper.getStrForGBK(bytes, 17, 27); // 交易传输时间
                    String cardnbr = FormatHelper.getStrForGBK(bytes, 27, 46); // 主账号
                    String amount = FormatHelper.getStrForGBK(bytes, 46, 58); // 交易金额
                    String crflag = FormatHelper.getStrForGBK(bytes, 58, 59); // 交易金额符号
                    String msgtype = FormatHelper.getStrForGBK(bytes, 59, 63); // 消息类型
                    String proccode = FormatHelper.getStrForGBK(bytes, 63, 69); // 交易类型码
                    String orderno = FormatHelper.getStrForGBK(bytes, 69, 109); // 订单号
                    String tranno = FormatHelper.getStrForGBK(bytes, 109, 115); // 内部交易流水号
                    String reserved = FormatHelper.getStrForGBK(bytes, 115, 134); // 内部保留域
                    String ervind = FormatHelper.getStrForGBK(bytes, 134, 135); // 冲正、撤销标志
                    String transtype = FormatHelper.getStrForGBK(bytes, 135, 139); // 主机交易类型

                    amount = MoneyHelper.divide(amount, "100", 2);
                    NewEve newEve = new NewEve();
                    newEve.setQueryTime(date);
                    newEve.setAmount(amount);
                    newEve.setCardnbr(cardnbr);
                    newEve.setCrflag(crflag);
                    newEve.setErvind(ervind);
                    newEve.setForcode(forcode);
                    newEve.setMsgtype(msgtype);
                    newEve.setProccode(proccode);
                    newEve.setSeqno(seqno);
                    newEve.setTranno(tranno);
                    newEve.setTranstype(transtype);
                    newEve.setCendt(cendt);
                    newEve.setOrderno(orderno);
                    newEve.setReserved(reserved);
                    // 当order等于空使用其他信息确定他的唯一性
                    // 防止重复录入
                    if (ObjectUtils.isEmpty(orderno)) {
                        orderno = String.format("%s%s%s", year, cendt, seqno);
                        newEve.setOrderno(orderno);
                    }

                    NewEve existsNewEve = newEveService.findTopByOrdernoAndQueryTime(orderno, date);
                    if (ObjectUtils.isEmpty(existsNewEve)) {
                        newEveService.save(newEve); // 保存交易数据
                        // jixinAssetBiz.record(newEve.getCardnbr(), newEve.getCrflag(), newEve.getAmount());  // 保存即信金额
                    } else {
                        log.error("order重复");
                    }
                } catch (Exception ex) {
                    log.error("eve 保存数据库异常", ex);
                }
            }
        });
    }


    /**
     * 传输协议 1.1.0 版本的导入
     * 传输文件时间导入小于等于2017年9月20号之前启用
     *
     * @param date
     * @param bufferedReader
     */
    private void importDatabaseOfEveFor1_1_0(String date, BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        Date opDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(opDate);
        int year = calendar.get(Calendar.YEAR);
        Gson gson = new Gson();
        lines.forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                try {
                    byte[] bytes = line.getBytes("gbk");
                    String acqcode = FormatHelper.getStrForGBK(bytes, 0, 11); // 受理方标识码
                    String seqno = FormatHelper.getStrForGBK(bytes, 11, 17); // 系统跟踪号
                    String cendt = FormatHelper.getStrForGBK(bytes, 17, 27); // 交易传输时间
                    String cardnbr = FormatHelper.getStrForGBK(bytes, 27, 46); // 主账号
                    String amount = FormatHelper.getStrForGBK(bytes, 46, 58); // 交易金额
                    String crflag = FormatHelper.getStrForGBK(bytes, 58, 59); // 交易金额符号
                    String msgtype = FormatHelper.getStrForGBK(bytes, 59, 63); // 消息类型
                    String proccode = FormatHelper.getStrForGBK(bytes, 63, 69); // 交易类型码
                    String mertype = FormatHelper.getStrForGBK(bytes, 69, 73);
                    String term = FormatHelper.getStrForGBK(bytes, 73, 81);
                    String retseqno = FormatHelper.getStrForGBK(bytes, 81, 93);
                    String conmode = FormatHelper.getStrForGBK(bytes, 93, 95);
                    String autresp = FormatHelper.getStrForGBK(bytes, 95, 101);
                    String forcode = FormatHelper.getStrForGBK(bytes, 101, 112);
                    String clrdate = FormatHelper.getStrForGBK(bytes, 112, 116);
                    String oldseqno = FormatHelper.getStrForGBK(bytes, 117, 122);
                    String openbrno = FormatHelper.getStrForGBK(bytes, 122, 128);
                    String tranbrno = FormatHelper.getStrForGBK(bytes, 128, 134);
                    String ervind = FormatHelper.getStrForGBK(bytes, 134, 135); // 冲正、撤销标志
                    String transtype = FormatHelper.getStrForGBK(bytes, 135, 139); // 主机交易类型

                    amount = MoneyHelper.divide(amount, "100", 2);
                    NewEve newEve = new NewEve();
                    newEve.setQueryTime(date);
                    newEve.setAmount(amount);
                    newEve.setCardnbr(cardnbr);
                    newEve.setCrflag(crflag);
                    newEve.setErvind(ervind);
                    newEve.setMsgtype(msgtype);
                    newEve.setProccode(proccode);
                    newEve.setSeqno(seqno);
                    newEve.setAcqcode(acqcode);
                    newEve.setMertype(mertype);
                    newEve.setTerm(term);
                    newEve.setRetseqno(retseqno);
                    newEve.setConmode(conmode);
                    newEve.setAutresp(autresp);
                    newEve.setForcode(forcode);
                    newEve.setClrdate(clrdate);
                    newEve.setOldseqno(oldseqno);
                    newEve.setOpenbrno(openbrno);
                    newEve.setTranstype(transtype);
                    newEve.setTranbrno(tranbrno);
                    String orderno = String.format("%s%s%s", year, cendt, seqno);
                    newEve.setOrderno(orderno);
                    newEve.setTranstype(transtype);
                    newEve.setCendt(cendt);

                    // 当order等于空使用其他信息确定他的唯一性
                    // 防止重复录入
                    NewEve existsNewEve = newEveService.findTopByOrdernoAndQueryTime(orderno, date);
                    if (ObjectUtils.isEmpty(existsNewEve)) {
                        newEveService.save(newEve); // 保存交易数据
                        //jixinAssetBiz.record(newEve.getCardnbr(), newEve.getCrflag(), amount);  // 保存即信金额
                    } else {
                        log.info("重复数据录入" + gson.toJson(existsNewEve) + " 要插入的数据" + gson.toJson(newEve));
                    }
                } catch (Exception ex) {
                    log.error("eve 保存数据库异常", ex);
                }
            }
        });
    }

    /**
     * 获取文件
     *
     * @param date
     * @param fileName
     * @param preFileType
     * @return
     */
    private String getXlsxAllFile(String date, String fileName, String preFileType) {
        String path = String.format("%s/%s/", filePath, date);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return String.format("%s/%s/%s%s", filePath, date, fileName, preFileType);
    }


    /**
     * 获取文件名
     *
     * @param date
     * @param fileName
     * @param preFileType
     * @return
     */
    private String getXlsxFileName(String date, String fileName, String preFileType) {
        return String.format("%s%s%s", date, fileName, preFileType);
    }


    /**
     * 获取本地资金流水
     *
     * @param date
     * @param localFileName
     * @param preFileType
     * @return
     */
    private boolean printLocalRecord(String date, String localFileName, String preFileType) {
        Workbook localWorkbook = null;
        ExportParams params = new ExportParams();
        params.setTitle(localFileName);
        params.setSheetName("平台本地资金流水");
        Date opDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM);
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(opDate, 1));  // 开始时间
        Date endOfDate = DateHelper.beginOfDate(DateHelper.addDays(opDate, 1));  // 结束时间
        int pageIndex = 0, pageSize = 100, pageIndexTotal = 0;

        do {
            Pageable localPageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id")));  //  分页
            Page<Object[]> localRecordPage = newEveService.findLocalAssetChangeRecord(DateHelper.dateToString(beginDate), DateHelper.dateToString(endOfDate), localPageable);
            pageIndexTotal = localRecordPage.getTotalPages();
            if (pageIndexTotal <= 0) {
                log.warn("本地交易流水为空");
                return false;
            }

            List<Object[]> content = localRecordPage.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            List<LocalRecord> localRecords = new ArrayList<>(content.size());
            LocalRecord temp;
            for (Object[] item : content) {
                temp = new LocalRecord();
                temp.setAccountId(item[8] + "");
                temp.setCreateDate((Date) item[7]);
                temp.setOpMoney(MoneyHelper.divide(item[0] + "", "100", 2));  // 金额
                temp.setPhone(item[6] + "");
                temp.setSeqNo(item[1] + "");
                temp.setTranName(item[2] + "");
                temp.setTranNo(item[4] + "");
                temp.setUserName(item[5] + "");
                temp.setTxFlag(item[3] + "");
                localRecords.add(temp);
            }
            localWorkbook = ExcelExportUtil.exportBigExcel(params, LocalRecord.class, localRecords);
            pageIndex++;
        } while (pageIndex < pageIndexTotal);
        ExcelExportUtil.closeExportBigExcel();  // 获取即信交易流水
        String localXlsxFile = getXlsxAllFile(date, localFileName, preFileType);
        File localRecordfile = new File(localXlsxFile);
        try (FileOutputStream fos = new FileOutputStream(localRecordfile)) {
            localWorkbook.write(fos);
            fos.close();
        } catch (Exception e) {
            log.error("本地流水文件保存失败", e);
            return false;
        }

        return true;
    }


    /**
     * 获取即信流水资金流水
     *
     * @param date
     * @param remoteFileName
     * @param preFileType
     * @return
     */
    private boolean printRemoteRecord(String date, String remoteFileName, String preFileType) {
        ExportParams params = new ExportParams();
        params.setTitle(remoteFileName);
        params.setSheetName("即信交易流水");
        // 发送即信流水
        int pageIndex = 0, pageSize = 100, pageIndexTotal = 0;
        Workbook remoteWorkbook = null;

        do {
            Pageable evePageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "eve.id")));  //  分页
            Page<Object[]> remoteRecordPage = newEveService.findRemoteByQueryTime(date, evePageable);
            pageIndexTotal = remoteRecordPage.getTotalPages();
            if (pageIndexTotal <= 0) {
                log.warn("即信流水为空");
                return false;
            }

            List<Object[]> content = remoteRecordPage.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            List<RemoteRecord> remoteRecords = new ArrayList<>(content.size());
            RemoteRecord temp;
            for (Object[] item : content) {
                temp = new RemoteRecord();
                temp.setUserName(item[0] + "");
                temp.setPhone(item[1] + "");
                temp.setAccountId(item[2] + "");
                temp.setSeqNo(item[3] + "");
                temp.setOpMoney(item[4] + "");
                temp.setTxFlag(item[5] + "");
                temp.setTranName(TranTypeHelper.getMsg(item[6] + ""));
                temp.setTranNo(item[6] + "");
                temp.setErvind(item[7] + "");
                temp.setCendt(item[8] + "");
                remoteRecords.add(temp);
            }
            remoteWorkbook = ExcelExportUtil.exportBigExcel(params, RemoteRecord.class, remoteRecords);
            pageIndex++;
        } while (pageIndex < pageIndexTotal);
        ExcelExportUtil.closeExportBigExcel();
        String remoteXlsxFile = getXlsxAllFile(date, remoteFileName, preFileType);
        try (FileOutputStream fos = new FileOutputStream(remoteXlsxFile)) {
            remoteWorkbook.write(fos);
            fos.close();
        } catch (Exception e) {
            log.error("即信流水文件保存失败", e);
            return false;
        }
        return true;
    }

    /**
     * 获取即信流水资金流水
     *
     * @param date
     * @param assetFileName
     * @param preFileType
     * @return
     */
    private boolean printAssetRecord(String date, String assetFileName, String preFileType) {
        int pageIndex = 0, pageSize = 100, pageIndexTotal = 0;

        ExportParams params = new ExportParams();
        params.setTitle(assetFileName);
        params.setSheetName("平台与即信资金对比");
        Workbook assetWorkbook = null;

        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "account.user_id")));
            Page<Object[]> localAndRemoteAssetInfoPage = jixinAssetService.findAllForPrint(pageable);
            pageIndexTotal = localAndRemoteAssetInfoPage.getTotalPages();
            if (pageIndexTotal <= 0) {
                log.error("用户资金交易记录为空");
                return false;
            }

            List<Object[]> content = localAndRemoteAssetInfoPage.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            List<LocalAndRemoteAssetInfo> remoteRecords = new ArrayList<>(content.size());
            LocalAndRemoteAssetInfo temp;
            for (Object[] item : content) {
                String localMoney = MoneyHelper.divide(item[6] + "", "100", 2);
                String remoteMoney = item[4] + "";
                String diffMoney = MoneyHelper.substract(remoteMoney, localMoney, 2);
                temp = new LocalAndRemoteAssetInfo();
                temp.setUserId(item[0] + "");
                temp.setRealname(item[1] + "");
                temp.setPhone(item[2] + "");
                temp.setAccountId(item[3] + "");
                temp.setRemoteMoney(remoteMoney);
                temp.setRemoteUpdateDatetime(item[5] + "");
                temp.setLocalMoney(localMoney);
                temp.setLocalUpdateDatetime((Date) item[7]);
                temp.setDiffMoney(diffMoney);
                remoteRecords.add(temp);
            }
            assetWorkbook = ExcelExportUtil.exportBigExcel(params, LocalAndRemoteAssetInfo.class, remoteRecords);
            pageIndex++;
        } while (pageIndex < pageIndexTotal);
        ExcelExportUtil.closeExportBigExcel();
        String assetXlsxFile = getXlsxAllFile(date, assetFileName, preFileType);
        try (FileOutputStream fos = new FileOutputStream(assetXlsxFile)) {
            assetWorkbook.write(fos);
            fos.close();
        } catch (Exception e) {
            log.error("本地资金流水文件保存失败", e);
            return false;
        }
        return true;
    }


    @Override
    public void audit(String date) {
        String localFileName = String.format("%s-平台资金流水", date);  // 本地文件名称
        String remoteFileName = String.format("%s-即信资金流水", date); // 即信平台流水名称
        String assetFileName = String.format("%s-双边金额对比", date); // 两边金额对比
        String preFileType = ".xlsx";  // 文件后缀


        // 获取当前用户交易流水
        if (!printLocalRecord(date, localFileName, preFileType)) {
            log.error("获取当天本地流水失败");
        }

        // 获取即信交易流水
        if (!printRemoteRecord(date, remoteFileName, preFileType)) {
            log.error("获取当天即信流水失败");
        }

        // 获取用户 金额
        if (!printAssetRecord(date, assetFileName, preFileType)) {
            log.error("获取当天资金流水失败");
        }

        // ============================
        // 发送邮件
        // ============================
        ImmutableList<String> emails = ImmutableList.of("617775122@qq.com", "595785682@qq.com");
        for (String email : emails) {
            doSendEmail(date, localFileName, remoteFileName, assetFileName, preFileType, email);
        }
    }

    /**
     * 发送对账文件
     * @param date
     * @param localFileName
     * @param remoteFileName
     * @param assetFileName
     * @param preFileType
     * @param email
     */
    private void doSendEmail(String date, String localFileName, String remoteFileName, String assetFileName, String preFileType, String email) {
        String remoteXlsxFile = getXlsxAllFile(date, remoteFileName, preFileType);
        String localXlsxFile = getXlsxAllFile(date, localFileName, preFileType);
        String assetXlsxFile = getXlsxAllFile(date, assetFileName, preFileType);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("service@gofobao.com");
            helper.setTo(email);
            helper.setSubject("对账系统");
            helper.setText(date + "对账附件");
            if (new File(localXlsxFile).exists()) {
                FileSystemResource localFSR = new FileSystemResource(new File(localXlsxFile));  // 本地文件
                helper.addAttachment("本地流水.xlsx", localFSR);
            }

            if (new File(remoteXlsxFile).exists()) {
                FileSystemResource remoteFSR = new FileSystemResource(new File(remoteXlsxFile));  // 即信文件
                helper.addAttachment("即信流水.xlsx", remoteFSR);
            }

            if (new File(assetXlsxFile).exists()) {
                FileSystemResource assetFSR = new FileSystemResource(new File(assetXlsxFile));  // 即信文件
                helper.addAttachment("资金流水.xlsx", assetFSR);

            }
            mailSender.send(mimeMessage);
            log.info("==========================================");
            log.info("发送邮件成功");
            log.info("==========================================");
        } catch (Exception e) {
            log.error("对账文件", e);
        }
    }

    @Override
    public void simpleDownload(String date) {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, date);
        log.info("========================");
        log.info("执行下载文件:" + fileName);
        log.info("========================");
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.error("EVE文件下载失败");
        }
    }
}
