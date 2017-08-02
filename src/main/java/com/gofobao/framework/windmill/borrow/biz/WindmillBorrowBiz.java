package com.gofobao.framework.windmill.borrow.biz;

import com.gofobao.framework.windmill.borrow.vo.response.BorrowTenderList;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;

/**
 * Created by admin on 2017/8/2.
 */
public interface WindmillBorrowBiz {

    /**
     * 标列表
     * @param id
     * @return
     */
    InvestListRes list(Long id);

    /**
     *
     * @param borrowId
     * @param date
     * @return
     */
    BorrowTenderList tenderList(Long borrowId,String date);

}
