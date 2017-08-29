package com.gofobao.framework.scheduler.biz.impl;

import com.gofobao.framework.api.helper.JixinFileManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.migrate.FormatHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doEVE() throws Exception {
        String fileName = String.format("%s-EVE%s-%s", bankNo, productNo, jixinTxDateHelper.getSubDateStr(1));
        boolean downloadState = jixinFileManager.download(fileName);
        if (!downloadState) {
            log.error(String.format("EVE: %s下载失败", fileName));
            return false;
        }
        File file = new File(String.format("%s%s%s", filePath, File.separator, fileName));
        BufferedReader bufferedReader = Files.newReader(file, StandardCharsets.UTF_8);
        // 保存进数据库
        List<Eve> eveList = new ArrayList<>(1000) ;
        bufferedReader.lines().forEach(line ->{
            try{
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                String bank = FormatHelper.getStr(bytes, 0, 4) ;
            }catch (Exception e){
                log.error("eee", e);
            }

        });


        XSSFWorkbook xwb = new XSSFWorkbook();//创建excel表格的工作空间
        XSSFSheet sheet = xwb.createSheet("data");
        CellStyle numberCellStyle = xwb.createCellStyle();
        DataFormat format = xwb.createDataFormat();
        numberCellStyle.setDataFormat(format.getFormat("0.00"));
        final int[] index = {1};
        createXlsxTitel(sheet);

        bufferedReader.lines().forEach(line -> {
            XSSFRow row = sheet.createRow(index[0]);
            XSSFCell cell1 = row.createCell(0);
            cell1.setCellValue(line.substring(0, 11));

            XSSFCell cell2 = row.createCell(1);
            cell2.setCellValue(line.substring(11, 17));

            XSSFCell cell3 = row.createCell(2);
            cell3.setCellValue(line.substring(17, 27));

            XSSFCell cell4 = row.createCell(3);
            cell4.setCellValue(line.substring(27, 46));

            XSSFCell cell5 = row.createCell(4);
            cell5.setCellStyle(numberCellStyle);
            cell5.setCellValue( Long.parseLong(line.substring(46, 58)) /  100D );
            XSSFCell cell6 = row.createCell(5);
            cell6.setCellValue(line.substring(58, 59));

            XSSFCell cell7 = row.createCell(6);
            cell7.setCellValue(line.substring(59, 63));

            XSSFCell cell8 = row.createCell(7);
            cell8.setCellValue(line.substring(64, 69));

            XSSFCell cell9 = row.createCell(8);
            cell9.setCellValue(line.substring(69, 73));

            XSSFCell cell10 = row.createCell(9) ;
            cell10.setCellValue(line.substring(73, 81));

            XSSFCell cell11 = row.createCell(10);
            cell11.setCellValue(line.substring(81, 93));

            XSSFCell cell12 = row.createCell(11);
            cell12.setCellValue(line.substring(93, 95));

            XSSFCell cell13 = row.createCell(12);
            cell13.setCellValue(line.substring(95, 101));

            XSSFCell cell14 = row.createCell(13);
            cell14.setCellValue(line.substring(101, 112));

            XSSFCell cell15 = row.createCell(14);
            cell15.setCellValue(line.substring(112, 116));

            XSSFCell cell16 = row.createCell(15);
            cell16.setCellValue(line.substring(116, 122));

            XSSFCell cell17 = row.createCell(16);
            cell17.setCellValue(line.substring(122, 128));

            XSSFCell cell18 = row.createCell(17);
            cell18.setCellValue(line.substring(128, 134));

            XSSFCell cell19 = row.createCell(18);
            cell19.setCellValue(line.substring(134, 135));

            XSSFCell cell20 = row.createCell(19);
            cell20.setCellValue(line.substring(135, 139));
            index[0] = index[0] + 1;
        });

        File xlsFilePathDir = new File(filePath);
        String xlsName = String.format("%s%s", fileName, ".xlsx");
        File xlsFile = new File(xlsFilePathDir, xlsName);
        OutputStream outputStream = new FileOutputStream(xlsFile);
        xwb.write(outputStream);
        return true;
    }

    private void createXlsxTitel(XSSFSheet sheet) {
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell cell1 = titleRow.createCell(0);
        cell1.setCellValue("受理方标识码");

        XSSFCell cell2 = titleRow.createCell(1);
        cell2.setCellValue("系统跟踪号");

        XSSFCell cell3 = titleRow.createCell(2);
        cell3.setCellValue("交易传输时间");

        XSSFCell cell4 = titleRow.createCell(3);
        cell4.setCellValue("主账号");

        XSSFCell cell5 = titleRow.createCell(4);
        cell5.setCellValue("交易金额");

        XSSFCell cell6 = titleRow.createCell(5);
        cell6.setCellValue("交易金额符号");

        XSSFCell cell7 = titleRow.createCell(6);
        cell7.setCellValue("交易类型码");

        XSSFCell cell8 = titleRow.createCell(7);
        cell8.setCellValue("系统跟踪号");

        XSSFCell cell9 = titleRow.createCell(8);
        cell9.setCellValue("商户类型");

        XSSFCell cell10 = titleRow.createCell(9);
        cell10.setCellValue("受卡机终端标识码");

        XSSFCell cell11 = titleRow.createCell(10);
        cell11.setCellValue("检索参考号");

        XSSFCell cell12 = titleRow.createCell(11);
        cell12.setCellValue("服务点条件码");

        XSSFCell cell13 = titleRow.createCell(12);
        cell13.setCellValue("授权应答码");

        XSSFCell cell14 = titleRow.createCell(13);
        cell14.setCellValue("发送方标识码");

        XSSFCell cell15 = titleRow.createCell(14);
        cell15.setCellValue("清算日期");

        XSSFCell cell16 = titleRow.createCell(15);
        cell16.setCellValue("原始交易的系统跟踪号");

        XSSFCell cell17 = titleRow.createCell(16);
        cell17.setCellValue("发卡网点号");

        XSSFCell cell18 = titleRow.createCell(17);
        cell18.setCellValue("交易网点");

        XSSFCell cell19 = titleRow.createCell(18);
        cell19.setCellValue("冲正、撤销标志");

        XSSFCell cell20 = titleRow.createCell(19);
        cell20.setCellValue("主机交易类型");
    }
}
