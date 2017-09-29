package com.gofobao.framework.starfire.borrow.biz;

import com.gofobao.framework.starfire.tender.vo.request.BorrowsQuery;
import com.gofobao.framework.starfire.tender.vo.response.BorrowQueryRes;

/**
 * Created by master on 2017/9/29.
 */
public interface StarFireBorrowBiz {

    /**
     * 对接平台标的列表查询
     * @param queryBorrow
     * @return
     */
    BorrowQueryRes queryBorrows(BorrowsQuery queryBorrow);

}
