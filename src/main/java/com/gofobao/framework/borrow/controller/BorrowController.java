package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoAddNetWorthBorrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
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
    @Autowired
    private BorrowBiz borrowBiz;

    @ApiOperation(value = "首页标列表")
    @PostMapping("/list")
    public ResponseEntity<List<VoViewBorrowListRes>> borrowList(@ModelAttribute VoBorrowListReq voBorrowListReq) {
        List<VoViewBorrowListRes> listResList = new ArrayList<>();
        try {
            listResList = borrowService.findAll(voBorrowListReq);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("BorrowController borrowList Exception ", e);
            return ResponseEntity.badRequest().body(Collections.EMPTY_LIST);
        }
        return ResponseEntity.ok(listResList);
    }


    @ApiOperation("标信息")
    @PostMapping("/info")
    public VoBorrowByIdRes getByBorrowId(@ModelAttribute VoBorrowByIdReq req) {
        VoBorrowByIdRes voBorrowByIdRes = new VoBorrowByIdRes();
        try {
            voBorrowByIdRes = borrowService.findByBorrowId(req);
        } catch (Exception e) {
            log.error("BorrowController borrowList Exception ", e);
        }
        return voBorrowByIdRes;
    }


    /**
     * 新增净值借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    @PostMapping("/addNetWorth")
    @ApiOperation("发布净值借款")
    public ResponseEntity<VoBaseResp> addNetWorth(@Valid @ModelAttribute VoAddNetWorthBorrow voAddNetWorthBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAddNetWorthBorrow.setUserId(userId);
        return borrowBiz.addNetWorth(voAddNetWorthBorrow);
    }

}
