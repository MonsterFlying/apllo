package com.gofobao.framework.starfire.borrow.controller;

import com.gofobao.framework.starfire.borrow.biz.StarFireBorrowBiz;
import com.gofobao.framework.starfire.tender.biz.StarFireTenderBiz;
import com.gofobao.framework.starfire.tender.vo.request.BorrowRepaymentQuery;
import com.gofobao.framework.starfire.tender.vo.request.BorrowsQuery;
import com.gofobao.framework.starfire.tender.vo.response.BidRepaymentInfoRes;
import com.gofobao.framework.starfire.tender.vo.response.BorrowQueryRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by master on 2017/9/29.
 * @author  王果
 */
@RestController
@RequestMapping("pub/starfire/borrow")
public class StarFireBorrowController {

    @Autowired
    private StarFireBorrowBiz starFireBorrowBiz;


    @Autowired
    private StarFireTenderBiz starFireTenderBiz;

    /**
     * 对接平台标的列表查询
     *
     * @param queryBorrow
     * @return
     */
    @RequestMapping("list")
    public BorrowQueryRes borrows(BorrowsQuery queryBorrow) {
        return starFireBorrowBiz.queryBorrows(queryBorrow);
    }

    /**
     * 标的回款信息查询接口
     *
     * @return
     */
    @RequestMapping("repay/list")
    public BidRepaymentInfoRes repayList(BorrowRepaymentQuery borrowRepaymentQuery) {
        return starFireTenderBiz.repaymentList(borrowRepaymentQuery);
    }

}
