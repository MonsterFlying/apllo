package com.gofobao.framework.starfire.borrow.controller;
import com.gofobao.framework.starfire.borrow.biz.StarFireBorrowBiz;
import com.gofobao.framework.starfire.tender.vo.request.BorrowsQuery;
import com.gofobao.framework.starfire.tender.vo.response.BorrowQueryRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by master on 2017/9/29.
 */
@RestController
@RequestMapping("pub/starFire/borrow")
public class StarFireBorrowController {

    @Autowired
    private StarFireBorrowBiz starFireBorrowBiz;

    /**
     * 对接平台标的列表查询
     * @param queryBorrow
     * @return
     */
    @RequestMapping("list")
    public  BorrowQueryRes borrows(BorrowsQuery queryBorrow){
        return  starFireBorrowBiz.queryBorrows(queryBorrow);
   };

}
