package com.gofobao.framework.tender.controller;

import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.tender.service.TenderService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@ApiModel("投标相关接口")
@RequestMapping("/tender")
@RestController
@Slf4j
public class TenderController {


    @Autowired
    private TenderService tenderService;

    @ApiOperation("投标用户列表")
    @PostMapping("/user/list")
    public List<VoBorrowTenderUserRes> findBorrowTenderUser(@ModelAttribute VoBorrowByIdReq req) {
        List<VoBorrowTenderUserRes> tenderUserResList=new ArrayList<>();
        try {
             tenderUserResList = tenderService.findBorrowTenderUser(req);
        }catch (Exception e){
        log.error("tender/user/list",e);
        }
        return tenderUserResList;
    }

}
