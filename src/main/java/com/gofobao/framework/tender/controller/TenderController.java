package com.gofobao.framework.tender.controller;

import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
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
    private TenderBiz tenderBiz;

    @ApiOperation("投标用户列表")
    @GetMapping("/v2/user/list/{borrowId}")
    public ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser( @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                              @PathVariable Long borrowId ) {
        return tenderBiz.findBorrowTenderUser(borrowId);
    }

    @ApiOperation("借款投标")
    @PostMapping("/v2/create")
    public ResponseEntity<VoBaseResp> tender(@ModelAttribute @Valid VoCreateTenderReq voCreateTenderReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCreateTenderReq.setUserId(userId);
        return tenderBiz.tender(voCreateTenderReq);
    }
}
