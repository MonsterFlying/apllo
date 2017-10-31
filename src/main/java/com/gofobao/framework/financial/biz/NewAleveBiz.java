package com.gofobao.framework.financial.biz;

import java.io.FileNotFoundException;

public interface NewAleveBiz {

    /**
     *  下载aleve文件, 并且入库
     * @param date 文件下载的时间
     * @return
     */
    boolean downloadNewAleveFileAndImportDatabase(String date) ;


    /**
     * 单独将下载好的文件导入数据库
     * @param date 文件下载的时间
     * @param fileName 文件名
     * @throws FileNotFoundException
     */
    void importDatabase(String date, String fileName) throws FileNotFoundException ;

    /**
     * 计算活期利息
     * @param date
     */
    void calculationCurrentInterest(String date) throws Exception;

    void simpleDownload(String date);
}
