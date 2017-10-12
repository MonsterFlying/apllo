package com.gofobao.framework.starfire.tender.controller;

import com.gofobao.framework.starfire.tender.biz.StarFireTenderBiz;
import com.gofobao.framework.starfire.tender.vo.request.BorrowCollectionRecords;
import com.gofobao.framework.starfire.tender.vo.request.UserTenderQuery;
import com.gofobao.framework.starfire.tender.vo.response.UserBorrowCollectionRecordsRes;
import com.gofobao.framework.starfire.tender.vo.response.UserTenderRes;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by master on 2017/9/29.
 */
@RestController
@RequestMapping("pub/starfire/tender")
public class StarFireTenderController {

    @Autowired
    private StarFireTenderBiz starFireTenderBiz;

    @RequestMapping("/userTender/list")
    @ApiOperation("用户投资记录查询")
    public UserTenderRes queryUserTender(UserTenderQuery userTenderQuery) {
        return starFireTenderBiz.userTenderList(userTenderQuery);
    }

    @RequestMapping("/borrowCollection/list")
    @ApiOperation("用户回款记录")
    public UserBorrowCollectionRecordsRes queryUserTender(BorrowCollectionRecords borrowCollectionRecords) {
        return starFireTenderBiz.borrowCollections(borrowCollectionRecords);
    }


}
