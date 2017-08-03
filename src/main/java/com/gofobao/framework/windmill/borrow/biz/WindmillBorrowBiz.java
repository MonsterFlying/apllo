package com.gofobao.framework.windmill.borrow.biz;

import com.gofobao.framework.windmill.borrow.vo.response.BorrowTenderList;
import com.gofobao.framework.windmill.borrow.vo.response.BySomeDayRes;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by admin on 2017/8/2.
 */
public interface WindmillBorrowBiz {

    /**
     * 标列表
     * @param request
     * @return
     */
    InvestListRes list(HttpServletRequest request);

    /**
     *标的的投标记录
     * @param request
     * @return
     */
    BorrowTenderList tenderList(HttpServletRequest request);


    /**
     * 5.3查询某天投资情况
     * @param request
     * @return
     */
    BySomeDayRes bySomeDayTenders(HttpServletRequest request);


}
