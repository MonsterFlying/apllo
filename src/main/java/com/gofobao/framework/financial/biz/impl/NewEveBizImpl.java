package com.gofobao.framework.financial.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.financial.biz.NewEveBiz;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.migrate.FormatHelper;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class NewEveBizImpl implements NewEveBiz {

    @Autowired
    NewEveService newEveService;

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

    public void importEveDataToDatabase(String date, String fileName) throws FileNotFoundException {
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);

        Stream<String> lines = bufferedReader.lines();
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
                    NewEve existsNewEve = null ;
                    if(ObjectUtils.isEmpty(orderno)){
                        existsNewEve = newEveService.findTopByCendtAndTranno(cendt, tranno) ;
                    }else{
                        existsNewEve = newEveService.findTopByOrdernoAndQueryTime(orderno, date);
                    }

                    if (ObjectUtils.isEmpty(existsNewEve)) {
                        newEveService.save(newEve);
                    }
                } catch (Exception ex) {
                    log.error("eve 保存数据库异常", ex);
                }
            }
        });
    }


}
