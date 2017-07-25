package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.vo.request.VoAddNetWorthBorrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListWarpRes;
import com.gofobao.framework.borrow.vo.response.VoViewVoBorrowDescWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */

@RestController
@Slf4j
@Api(description = "首页标接口")
public class BorrowController {

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private ThymeleafHelper thymeleafHelper;
    @Autowired
    private JwtTokenHelper jwtTokenHelper;
    @Value("${jwt.header}")
    private String tokenHeader;
    @Value("${jwt.prefix}")
    private String prefix;


    @ApiOperation(value = "首页标列表; type:  -1：全部;   0：车贷标；  1：净值标；  2：秒标；  4：渠道标;  5: 流转标")
    @GetMapping("/pub/borrow/v2/list/{type}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBorrowListWarpRes> borrowList(@PathVariable Integer pageIndex,
                                                              @PathVariable Integer pageSize,
                                                              @PathVariable Integer type) {
        VoBorrowListReq voBorrowListReq = new VoBorrowListReq();
        voBorrowListReq.setPageIndex(pageIndex);
        voBorrowListReq.setPageSize(pageSize);
        voBorrowListReq.setType(type);
        return borrowBiz.findAll(voBorrowListReq);
    }


    @ApiOperation("标信息")
    @GetMapping("/pub/borrow/v2/info/{borrowId}")
    public ResponseEntity<BorrowInfoRes> getByBorrowId(@PathVariable Long borrowId) {
        return borrowBiz.info(borrowId);
    }


    @ApiOperation("标简介")
    @GetMapping("/pub/borrow/v2/desc/{borrowId}")
    public ResponseEntity<VoViewVoBorrowDescWarpRes> desc(@PathVariable Long borrowId) {
        return borrowBiz.desc(borrowId);
    }


    @ApiOperation(value = "标合同")
    @GetMapping(value = "/borrow/pub/borrowProtocol/{borrowId}")
    public ResponseEntity<String> takeRatesDesc(@PathVariable Long borrowId, HttpServletRequest request) {
        Long userId = 0L;
        String authToken = request.getHeader(this.tokenHeader);
        if (!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))) {
            authToken = authToken.substring(7);
        }
        String username = jwtTokenHelper.getUsernameFromToken(authToken);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userId = jwtTokenHelper.getUserIdFromToken(authToken);
        }

        String content = "";
        Map<String, Object> paramMaps = borrowBiz.contract(borrowId, userId);
        try {
            content = thymeleafHelper.build("borrowProtocol", paramMaps);
        } catch (Throwable e) {
            log.error("BorrowController->takeRatesDesc fail", e);
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }

    /**
     * 新增净值借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    @PostMapping("/borrow/addNetWorth")
    @ApiOperation("发布净值借款")
    public ResponseEntity<VoBaseResp> addNetWorth(@Valid @ModelAttribute VoAddNetWorthBorrow voAddNetWorthBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voAddNetWorthBorrow.setUserId(userId);
        return borrowBiz.addNetWorth(voAddNetWorthBorrow);
    }


    /**
     * 取消借款
     *
     * @param voCancelBorrow
     * @return
     */
    @PostMapping("/borrow/cancelBorrow")
    @ApiOperation("取消借款")
    public ResponseEntity<VoBaseResp> cancelBorrow(@Valid @ModelAttribute VoCancelBorrow voCancelBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voCancelBorrow.setUserId(userId);
        return borrowBiz.cancelBorrow(voCancelBorrow);
    }

    /**
     * 复审
     *
     * @param voDoAgainVerifyReq
     * @return
     */
    @PostMapping("/borrow/doAgainVerify")
    @ApiOperation("复审")
    public ResponseEntity<VoBaseResp> doAgainVerify(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        return borrowBiz.doAgainVerify(voDoAgainVerifyReq);
    }


}
