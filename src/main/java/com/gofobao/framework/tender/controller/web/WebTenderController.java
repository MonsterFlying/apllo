package com.gofobao.framework.tender.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoPcEndThirdTender;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by Max on 17/5/16.
 */
@Api(description = "pc:投标相关接口")
@RequestMapping("")
@RestController
@Slf4j
@SuppressWarnings("all")
public class WebTenderController {

    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private JwtTokenHelper jwtTokenHelper;


    @ApiOperation("pc:投标用户列表")
    @GetMapping("/pub/tender/pc/v2/user/list/{pageIndex}/{pageSize}/{borrowId}")
    public ResponseEntity<VoBorrowTenderUserWarpListRes> pcFindBorrowTenderUser(@PathVariable Integer pageIndex,
                                                                                @PathVariable Integer pageSize,
                                                                                @PathVariable Long borrowId,
                                                                                HttpServletRequest request,
                                                                                HttpServletResponse response) {
        TenderUserReq tenderUserReq = new TenderUserReq();
        try {
            String token = jwtTokenHelper.getToken(request);
            if (!cn.jiguang.common.utils.StringUtils.isEmpty(token)) {
                jwtTokenHelper.validateSign(token);
                Long userId = jwtTokenHelper.getUserIdFromToken(token);  // 用户ID
                tenderUserReq.setUserId(userId);
            }
        } catch (Exception e) {
            log.info("当前用户未登录");
        }
        tenderUserReq.setPageSize(pageSize);
        tenderUserReq.setPageIndex(pageIndex);
        tenderUserReq.setBorrowId(borrowId);
        return tenderBiz.findBorrowTenderUser(tenderUserReq);
    }

    @ApiOperation("借款投标")
    @PostMapping("tender/pc/v2/create")
    public ResponseEntity<VoBaseResp> pcTender(@ModelAttribute @Valid VoCreateTenderReq voCreateTenderReq,
                                               HttpServletRequest request,
                                               @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {

        String requestSource = request.getHeader("requestSource");
        voCreateTenderReq.setRequestSource(requestSource);
        voCreateTenderReq.setUserId(userId);
        return tenderBiz.tender(voCreateTenderReq);
    }

    /**
     * 结束普通第三方债权接口
     */
    @ApiOperation("结束普通第三方债权接口")
    @PostMapping("/pub/tender/pc/v2/third/end")
    public ResponseEntity<VoBaseResp> pcEndThirdTender(@ModelAttribute VoPcEndThirdTender voPcEndThirdTender) {
        return tenderBiz.pcEndThirdTender(voPcEndThirdTender);
    }
}
