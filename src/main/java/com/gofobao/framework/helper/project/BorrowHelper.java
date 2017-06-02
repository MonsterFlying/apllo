package com.gofobao.framework.helper.project;

/**
 * Created by Max on 2017/3/27.
 */
public class BorrowHelper {

    /**
     * 获取借款url
     *
     * @param borrowId
     * @param borrowName
     * @return
     */
    public static String getBorrowLink(Long borrowId, String borrowName) {
        return "<a href=\"/borrow/" + borrowId + "\" target=\"_blank\">" + borrowName + "</a>";
    }

}
