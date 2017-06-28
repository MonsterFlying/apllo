package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListWarpRes;
import com.gofobao.framework.borrow.vo.response.VoViewVoBorrowDescWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
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
@SuppressWarnings("all")
public class BorrowController {

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private ThymeleafHelper thymeleafHelper;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;

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


    // TODO 去掉包裹
    @ApiOperation("标信息")
    @GetMapping("/pub/borrow/v2/info/{borrowId}")
    public ResponseEntity<BorrowInfoRes> getByBorrowId(@PathVariable Long borrowId) {
        return borrowBiz.info(borrowId);
    }


    // TODO 去掉包裹
    @ApiOperation("标简介")
    @GetMapping("/pub/borrow/v2/desc/{borrowId}")
    public ResponseEntity<VoViewVoBorrowDescWarpRes> desc(@PathVariable Long borrowId) {
        return borrowBiz.desc(borrowId);
    }


    // TODO 返回改为  ResponseEntity<VoHtmlResp>
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
        } catch (Exception e) {

            log.error("BorrowController->takeRatesDesc fail",e);
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
     * pc取消借款
     *
     * @param voPcCancelThirdBorrow
     * @return
     */
    @PostMapping("/pub/borrow/pc/cancelBorrow")
    @ApiOperation("pc取消借款")
    public ResponseEntity<VoBaseResp> pcCancelBorrow(@Valid @ModelAttribute VoPcCancelThirdBorrow voPcCancelThirdBorrow) {
        return borrowBiz.pcCancelBorrow(voPcCancelThirdBorrow);
    }

    /**
     * 取消借款
     *
     * @param voCancelBorrow
     * @return
     */
    @PostMapping("/borrow/cancelBorrow")
    @ApiOperation("取消借款")
    public ResponseEntity<VoBaseResp> cancelBorrow(@Valid @ModelAttribute VoCancelBorrow voCancelBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCancelBorrow.setUserId(userId);
        return borrowBiz.cancelBorrow(voCancelBorrow);
    }

    @PostMapping("/borrow/repayAll")
    @ApiOperation("提前还款")
    public ResponseEntity<VoBaseResp> repayAll(@Valid @ModelAttribute VoRepayAllReq voRepayAllReq) {
        return borrowThirdBiz.thirdBatchRepayAll(voRepayAllReq);
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

    /**
     * 登记官方借款（车贷标、渠道标）
     *
     * @param voRegisterOfficialBorrow
     * @return
     */
    @PostMapping("/borrow/pub/pc/official/register")
    @ApiOperation("登记官方借款（车贷标、渠道标）")
    public ResponseEntity<VoHtmlResp> registerOfficialBorrow(@ModelAttribute @Valid VoRegisterOfficialBorrow voRegisterOfficialBorrow) {
        return borrowBiz.registerOfficialBorrow(voRegisterOfficialBorrow);
    }
}
