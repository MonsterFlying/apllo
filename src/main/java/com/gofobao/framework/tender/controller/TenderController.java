package com.gofobao.framework.tender.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Max on 17/5/16.
 */
@Api(description = "投标相关接口")
@RequestMapping("/tender")
@RestController
@Slf4j
public class TenderController {

    @Autowired
    private TenderBiz tenderBiz;


    @ApiOperation("标的详情-投标记录")
    @GetMapping("/v2/user/list/{pageIndex}/{pageSize}/{borrowId}")
    public ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser(@PathVariable Integer pageIndex,
                                                                              @PathVariable Integer pageSize,
                                                                              @PathVariable Long borrowId) {
        TenderUserReq borrowTenderList = new TenderUserReq();
        borrowTenderList.setPageSize(pageSize);
        borrowTenderList.setPageIndex(pageIndex);
        borrowTenderList.setBorrowId(borrowId);
        return tenderBiz.findBorrowTenderUser(borrowTenderList);
    }

    @ApiOperation("借款投标")
    @PostMapping("/v2/create")
    public ResponseEntity<VoBaseResp> tender(@ModelAttribute @Valid VoCreateTenderReq voCreateTenderReq,
                                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voCreateTenderReq.setUserId(userId);
        voCreateTenderReq.setSource(0);//pc端
        return tenderBiz.tender(voCreateTenderReq);
    }


}
