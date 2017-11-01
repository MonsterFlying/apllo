package com.gofobao.framework.wheel.borrow.biz;


import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.wheel.borrow.vo.request.BorrowsReq;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowsRes;


/**
 * Created by master on 2017/10/27.
 */
public interface WheelBorrowBiz {

    /**
     * 5.1标的查询接口
     *
     * @param borrowId
     * @return 返回 标的列表
     */
    BorrowsRes borrows(BorrowsReq borrowId);

    /**

     * 4.2 标的变化接口通知接口
     *
     * @param borrow
     */
    void borrowUpdateNotice(Borrow borrow);

    /**
     * 4.1投资通知
     * @param tender
     *
     */
    void investNotice(Tender tender);


}
