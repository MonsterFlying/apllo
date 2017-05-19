package com.gofobao.framework.borrow.controller;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.io.netty.util.internal.ObjectUtil;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Max on 17/5/16.
 */

@RequestMapping("/borrow")
@RestController
@Slf4j
@Api("首页标接口")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @ApiOperation(value = "首页标列表")
    @PostMapping("/list")
    public ResponseEntity<List<VoViewBorrowListRes>> borrowList(@ModelAttribute VoBorrowListReq voBorrowListReq) {
        List<VoViewBorrowListRes> listResList = new ArrayList<>();
        try {
            listResList = borrowService.findAll(voBorrowListReq);
        } catch (Exception e) {
            e.printStackTrace();
           log.error("BorrowController borrowList Exception ", e);
           return ResponseEntity.badRequest().body(Collections.EMPTY_LIST) ;
        }
        return ResponseEntity.ok(listResList);
    }


    @ApiOperation("标信息")
    @PostMapping("/info")
    public VoBorrowByIdRes getByBorrowId(@ModelAttribute VoBorrowByIdReq req){
        VoBorrowByIdRes voBorrowByIdRes=new VoBorrowByIdRes();
        try {
            voBorrowByIdRes = borrowService.findByBorrowId(req);
        }catch (Exception e){
            log.error("BorrowController borrowList Exception ", e);
        }
        return  voBorrowByIdRes;
    }

   /* @ApiOperation("标简介")
    @PostMapping("/details")
    public*/


}
