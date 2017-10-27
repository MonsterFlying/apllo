package com.gofobao.framework.wheel.borrow.biz;


import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.wheel.borrow.vo.request.BorrowsReq;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowUpdateRes;
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
     * @return
     */
    BorrowUpdateRes borrowUpdateNotice(Borrow borrow);


}
