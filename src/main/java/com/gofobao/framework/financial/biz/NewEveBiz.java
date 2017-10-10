package com.gofobao.framework.financial.biz;

import java.io.FileNotFoundException;

public interface NewEveBiz {

    /**
     * 下载Eve文件,并且写入数据库
     *
     * @param date
     * @return
     */
    boolean downloadEveFileAndSaveDB(String date);

    /**
     * 导入eve到数据库中
     *
     * @param date
     * @param fileName
     * @throws FileNotFoundException
     */
    void importEveDataToDatabase(String date, String fileName) throws FileNotFoundException;

    /**
     * 进行系统审计
     * @param date
     */
    void audit(String date);
}
