package com.gofobao.framework.tender.controller.web;

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

import javax.validation.Valid;

/**
 * Created by Max on 17/5/16.
 */
@Api(description = "pc:投标相关接口")
@RequestMapping("tender/pc")
@RestController
@Slf4j
@SuppressWarnings("all")
public class WebTenderController {

    @Autowired
    private TenderBiz tenderBiz;


    @ApiOperation("pc:投标用户列表")
    @GetMapping("v2/user/list/{pageIndex}/{pageSize}/{borrowId}")
    public ResponseEntity<VoBorrowTenderUserWarpListRes> pcFindBorrowTenderUser(@PathVariable Integer pageIndex,
                                                                                @PathVariable Integer pageSize,
                                                                                @PathVariable Long borrowId){
        TenderUserReq tenderUserReq=new TenderUserReq();
        tenderUserReq.setPageSize(pageSize);
        tenderUserReq.setPageIndex(pageIndex);
        tenderUserReq.setBorrowId(borrowId);
        return tenderBiz.findBorrowTenderUser(tenderUserReq);
    }

    @ApiOperation("借款投标")
    @PostMapping("v2/create")
    public ResponseEntity<VoBaseResp> pcTender(@ModelAttribute @Valid VoCreateTenderReq voCreateTenderReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voCreateTenderReq.setUserId(userId);
        return tenderBiz.tender(voCreateTenderReq);
    }
}
