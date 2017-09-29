package com.gofobao.framework.financial.biz;

public interface NewEveBiz {

    /**
     * 下载Eve文件,并且写入数据库
     * @param date
     * @return
     */
    boolean downloadEveFileAndSaveDB(String date ) ;
}
