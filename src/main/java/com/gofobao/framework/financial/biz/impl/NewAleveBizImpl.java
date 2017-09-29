package com.gofobao.framework.financial.biz.impl;

import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.migrate.FormatHelper;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class NewAleveBizImpl implements NewAleveBiz {

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

        Stream<String> lines = bufferedReader.lines();
        lines.forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                try {
                    byte[] bytes = line.getBytes("gbk") ;
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
                    amount = MoneyHelper.divide(amount, "100", 2) ;  // 将分转成元
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

                    NewAleve existsNewAleve = newAleveService.findTopByQueryTimeAndTranno(date, tranno);
                    if (ObjectUtils.isEmpty(existsNewAleve)) {
                        newAleveService.save(newAleve);
                    }
                } catch (Exception ex) {
                    log.error("aleve 保存数据库异常", ex);
                }
            }
        });
    }
}
