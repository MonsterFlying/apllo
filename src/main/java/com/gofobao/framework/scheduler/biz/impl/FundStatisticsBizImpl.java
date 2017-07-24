package com.gofobao.framework.scheduler.biz.impl;

import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

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

    /**
     * 序号	字段中文名称	字段英文名称	字段类型	长度	起始位置	截止位置	必填	备注
     * 1	受理方标识码	ACQCODE	N	11	1	11	M
     * 2	系统跟踪号	    SEQNO	N	6	12	17	M
     * 3	交易传输时间	CENDT	N	10	18	27	M
     * 4	主账号	        CARDNBR	N	19	28	46	M
     * 5	交易金额	    AMOUNT	N	12	47	58	M
     * 6	交易金额符号	CRFLAG	A	1	59	59	M	小于零等于C；大于零等于D；
     * 7	消息类型	    MSGTYPE	N	4	60	63	M
     * 8	交易类型码	    PROCCODE	N	6	64	69	M
     * 9	商户类型	    MERTYPE	N	4	70	73	M
     * 10	受卡机终端标识码	TERM	A	8	74	81	M
     * 11	检索参考号	    RETSEQNO	A	12	82	93	M
     * 12	服务点条件码	CONMODE	N	2	94	95	M
     * 13	授权应答码	    AUTRESP	A	6	96	101	M
     * 14	发送方标识码	FORCODE	N	11	102	112	M
     * 15	清算日期	    CLRDATE	N	4	113	116	M
     * 16	原始交易的系统跟踪号	OLDSEQNO	N	6	117	122	M	对于冲正交易,原始交易指被冲正的交易
     * 17	发卡网点号	    OPENBRNO	N	6	123	128	M
     * 18	交易网点	    TRANBRNO	N	6	129	134	M
     * 19	冲正、撤销标志	ERVIND	N 	1	135	135	M	1-已撤销/冲正;空或0-正常交易
     * 20	主机交易类型	TRANSTYPE	N	4	136	139	M
     *
     * @return
     * @throws Exception
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doEVE() throws Exception {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, jixinTxDateHelper.getSubDateStr(1));
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.info(String.format("EVE: %s下载失败", fileName));
            return false;
        }
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        List<String> strings = Files.readLines(file, Charsets.UTF_8);
        Gson gson = new Gson();

        List<Map<String, String>> dataList = new ArrayList<>(strings.size());
        for (String line : strings) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("ACQCODE", line.substring(0, 11));
            dataMap.put("SEQNO", line.substring(11, 17));
            dataMap.put("CENDT", line.substring(17, 27));
            dataMap.put("CARDNBR", line.substring(27, 46));
            dataMap.put("AMOUNT", line.substring(46, 58));
            dataMap.put("CRFLAG", line.substring(58, 59));
            dataMap.put("MSGTYPE", line.substring(59, 63));
            dataMap.put("PROCCODE", line.substring(64, 69));
            dataMap.put("MERTYPE", line.substring(69, 73));
            dataMap.put("TERM", line.substring(73, 81));
            dataMap.put("RETSEQNO", line.substring(81, 93));
            dataMap.put("CONMODE", line.substring(93, 95));
            dataMap.put("AUTRESP", line.substring(95, 101));
            dataMap.put("FORCODE", line.substring(101, 112));
            dataMap.put("CLRDATE", line.substring(112, 116));
            dataMap.put("OLDSEQNO", line.substring(116, 122));
            dataMap.put("OPENBRNO", line.substring(122, 128));
            dataMap.put("TRANBRNO", line.substring(128, 134));
            dataMap.put("ERVIND", line.substring(134, 135));
            dataMap.put("TRANSTYPE", line.substring(135, 139));
            System.err.println(gson.toJson(dataMap));
            dataList.add(dataMap);
        }
        return false;
    }
}
