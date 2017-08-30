package com.gofobao.framework.scheduler.biz.impl;

import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.service.AleveService;
import com.gofobao.framework.financial.service.EveService;
import com.gofobao.framework.migrate.FormatHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FundStatisticsBizImpl implements FundStatisticsBiz {

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doEve(String date) throws Exception {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, date);
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            throw new Exception(String.format("EVE: %s 下载失败", fileName)) ;
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
                String cendt = FormatHelper.getStrForUTF8(bytes, 17, 27);
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
                eve.setCendt(cendt);
                eve.setCardnbr(cardnbr);
                eve.setAmount(amount);
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
    public boolean doAleve() throws Exception {
        String fileName = String.format("%s-ALEVE%s-%s", bankNo, productNo, jixinTxDateHelper.getSubDateStr(1));
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
                aleve.setAmount(amount);
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
                aleve.setCurrBal(currBal);
                aleve.setForcardnbr(forcardnbr);
                aleve.setRevind(revind);
                aleve.setResv(resv);
                aleve.setCreateAt(nowDate) ;
                List<Aleve> aleves = aleveService.findByTranno(aleve.getTranno());
                if (!CollectionUtils.isEmpty(aleves)) {
                    log.error(String.format("ALEVE 重数据: %s", line));
                }
                aleveService.save(aleve) ;
            } catch (Exception e) {
                log.error("保存eve到数据库失败", e);
            }
        });

        return true;
    }


}
