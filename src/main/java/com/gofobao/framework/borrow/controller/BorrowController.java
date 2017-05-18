package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
           log.error("BorrowController borrowList Exception ", e);
           return ResponseEntity.badRequest().body(Collections.EMPTY_LIST) ;
        }
        return ResponseEntity.ok(listResList);
    }

}
