package com.gofobao.framework.wheel.borrow.controller;

import com.gofobao.framework.wheel.borrow.biz.WheelBorrowBiz;
import com.gofobao.framework.wheel.borrow.vo.request.BorrowsReq;
import com.gofobao.framework.wheel.borrow.vo.response.BorrowsRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author master
 * @date 2017/10/27
 */
@RestController
@RequestMapping("/pub/wheel")
@Slf4j
public class WheelBorrowController {

    @Autowired
    private WheelBorrowBiz wheelBorrowBiz;

    /**
     * 标的列表
     *
     * @param investId
     * @return
     */
    @RequestMapping(value = "/borrow/list", method = RequestMethod.GET)
    public BorrowsRes borrows(@RequestAttribute(name = "invest_id",required = false) String investId) {
        BorrowsReq borrowsReq = new BorrowsReq();
        borrowsReq.setInvest_id(investId);
        return wheelBorrowBiz.borrows(borrowsReq);
    }

}
