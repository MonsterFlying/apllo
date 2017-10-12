package com.gofobao.framework.scheduler.biz.impl;

import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.financial.biz.JixinAssetBiz;
import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.service.AleveService;
import com.gofobao.framework.financial.service.EveService;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.FormatHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    @Autowired
    JixinAssetBiz jixinAssetBiz ;

    @Override
    public boolean doEve(String date) throws Exception {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, date);
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            throw new Exception(String.format("EVE: %s 下载失败", fileName));
        }

        boolean b = importEveDataToDatabase(date, fileName);
        if (!b) {
            log.error("入库失败");
        }
        return true;
    }

    /**
     * eve数据入库
     *
     * @param date
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public boolean importEveDataToDatabase(String date, String fileName) throws Exception {
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        // 保存进数据库
        Date nowDate = new Date();
        bufferedReader.lines().forEach(line -> {
            try {
                byte[] bytes = line.getBytes("gbk");
                String acqcode = FormatHelper.getStrForGBK(bytes, 0, 11);
                String seqno = FormatHelper.getStrForGBK(bytes, 11, 17);
                String sendt = FormatHelper.getStrForGBK(bytes, 17, 27);
                String cardnbr = FormatHelper.getStrForGBK(bytes, 27, 46);
                String amount = FormatHelper.getStrForGBK(bytes, 46, 58);
                String crflag = FormatHelper.getStrForGBK(bytes, 58, 59);
                String msgtype = FormatHelper.getStrForGBK(bytes, 59, 63);
                String proccode = FormatHelper.getStrForGBK(bytes, 63, 69);
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
                String ervind = FormatHelper.getStrForGBK(bytes, 134, 135);
                String transtype = FormatHelper.getStrForGBK(bytes, 135, 139);
                Eve eve = new Eve();
                eve.setAcqcode(acqcode);
                eve.setSeqno(seqno);
                eve.setSendt(sendt);
                eve.setCardnbr(cardnbr);
                eve.setAmount(MoneyHelper.divide(amount, "100", 2));  //保证元的问题
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
                // 对资金录入进行变动
                eveService.save(eve);
                jixinAssetBiz.record(eve.getCardnbr(), eve.getCrflag(), eve.getAmount());  // 保存即信金额
            } catch (Exception e) {
                log.error("保存eve到数据库失败", e);
            }
        });

        return false;
    }


    @Override
    public boolean doAleve(String date) throws Exception {
        String fileName = String.format("%s-ALEVE%s-%s", bankNo, productNo, date);
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.error(String.format("ALEVE: %s下载失败", fileName));
            return false;
        }

        return importAleveDataToDatabase(date, fileName);
    }

    @Override
    public boolean importAleveDataToDatabase(String date, String fileName) throws Exception {
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        // 保存进数据库
        Date nowDate = new Date();
        bufferedReader.lines().forEach(line -> {
            try {
                byte[] bytes = line.getBytes("gbk");
                String bank = FormatHelper.getStrForGBK(bytes, 0, 4);
                String cardnbr = FormatHelper.getStrForGBK(bytes, 4, 23);
                String amount = FormatHelper.getStrForGBK(bytes, 23, 40);
                String curNum = FormatHelper.getStrForGBK(bytes, 40, 43);
                String crflag = FormatHelper.getStrForGBK(bytes, 43, 44);
                String valdate = FormatHelper.getStrForGBK(bytes, 44, 52);
                String inpdate = FormatHelper.getStrForGBK(bytes, 52, 60);
                String reldate = FormatHelper.getStrForGBK(bytes, 60, 68);
                String inptime = FormatHelper.getStrForGBK(bytes, 68, 76);
                String tranno = FormatHelper.getStrForGBK(bytes, 76, 82);
                String oriTranno = FormatHelper.getStrForGBK(bytes, 82, 88);
                String transtype = FormatHelper.getStrForGBK(bytes, 88, 92);
                String desline = FormatHelper.getStrForGBK(bytes, 92, 134);
                String currBal = FormatHelper.getStrForGBK(bytes, 134, 151);
                String forcardnbr = FormatHelper.getStrForGBK(bytes, 151, 170);
                String revind = FormatHelper.getStrForGBK(bytes, 170, 171);
                String resv = FormatHelper.getStrForGBK(bytes, 171, 371);

                Aleve aleve = new Aleve();
                aleve.setBank(bank);
                aleve.setCardnbr(cardnbr);
                aleve.setAmount(MoneyHelper.divide(amount, "100", 2));  // 保证元的问题
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
                aleve.setCurrBal(MoneyHelper.divide(currBal, "100", 2)); // 保证元的问题
                aleve.setForcardnbr(forcardnbr);
                aleve.setRevind(revind);
                aleve.setResv(resv);
                aleve.setCreateAt(nowDate);
                aleve.setQueryDate(date);

                aleveService.save(aleve);
            } catch (Exception e) {
                log.error("保存eve到数据库失败", e);
            }
        });
        return true;
    }
}
