package com.gofobao.framework.common.jxl;

/**
 * Created by admin on 2017/7/13.
 */
public class ExcelException extends Exception {

    public ExcelException() {}

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(Throwable cause) {
        super(cause);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
