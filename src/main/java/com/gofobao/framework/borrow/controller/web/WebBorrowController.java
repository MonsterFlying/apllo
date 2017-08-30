package com.gofobao.framework.borrow.controller.web;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.VoPcBorrowList;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowStatisticsWarpRes;
import com.gofobao.framework.borrow.vo.response.VoViewVoBorrowDescWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.response.VoViewRepayCollectionLogWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.tender.biz.TransferBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */

@RequestMapping("")
@RestController
@Slf4j
@Api(description = "pc:首页标接口")
@SuppressWarnings("all")
public class WebBorrowController {

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private ThymeleafHelper thymeleafHelper;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;

    @Autowired
    private RepaymentBiz repaymentBiz;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String prefix;


    @Autowired
    private TransferBiz transferBiz;


    @ApiOperation(value = "pc:首页标列表; type:-1：全部 0：车贷标；1：净值标；2：秒标；4：渠道标 ; 5流转标")
    @GetMapping("pub/borrow/pc/v2/list/{type}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoPcBorrowList> pcList(@PathVariable Integer pageIndex,
                                                 @PathVariable Integer pageSize,
                                                 @PathVariable Integer type) {
        VoBorrowListReq voBorrowListReq = new VoBorrowListReq();
        voBorrowListReq.setPageIndex(pageIndex);
        voBorrowListReq.setPageSize(pageSize);
        voBorrowListReq.setType(type);
        return borrowBiz.pcFindAll(voBorrowListReq);
    }

    @ApiOperation("流转标信息")
    @GetMapping("pub/transfer/pc/v2/info/{transferId}")
    public ResponseEntity<BorrowInfoRes> pcgetByTransferId(@PathVariable Long transferId) {
        return transferBiz.transferInfo(transferId);
    }

    @ApiOperation("普通标信息")
    @GetMapping("pub/borrow/pc/v2/info/{borrowId}")
    public ResponseEntity<BorrowInfoRes> pcgetByBorrowId(@PathVariable Long borrowId) {
        return borrowBiz.info(borrowId);
    }

    @ApiOperation("pc：标简介")
    @GetMapping("pub/borrow/pc/v2/desc/{borrowId}")
    public ResponseEntity<VoViewVoBorrowDescWarpRes> pcDesc(@PathVariable Long borrowId) {
        return borrowBiz.desc(borrowId);
    }


    @ApiOperation(value = "pc:标合同")
    @GetMapping(value = "pub/borrow/pc/v2/borrowProtocol/{borrowId}")
    public ResponseEntity<String> pcTakeRatesDesc(HttpServletRequest request, @PathVariable Long borrowId) {
        Long userId = 0L;
        String authToken = request.getHeader(this.tokenHeader);
        if (!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))) {
            authToken = authToken.substring(7);
        }
        String username = jwtTokenHelper.getUsernameFromToken(authToken);
        if (!StringUtils.isEmpty(username != null)) {
            userId = jwtTokenHelper.getUserIdFromToken(authToken);
        }
        String content = "";
        try {
            Map<String, Object> paramMaps = borrowBiz.pcContract(borrowId, userId);
            content = thymeleafHelper.build("borrowProtcol", paramMaps);
        } catch (Throwable e) {
            log.info(" WebBorrowController -> pcTakeRatesDesc  fail", e);
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "pub/borrow/pc/v2/repayment/logs/{borrowId}", method = RequestMethod.GET)
    @ApiOperation("还款记录")
    public ResponseEntity<VoViewRepayCollectionLogWarpRes> info(@PathVariable("borrowId") Long borrowId) {
        return repaymentBiz.logs(borrowId);
    }

    @ApiOperation(value = "pc：招标中统计")
    @GetMapping(value = "pub/borrow/pc/v2/statistics")
    public ResponseEntity<VoViewBorrowStatisticsWarpRes> pcStatistics() {
        return borrowBiz.statistics();
    }

    /**
     * pc 取消借款
     *
     * @param voPcCancelThirdBorrow
     * @return
     */
    @PostMapping("borrow/pc/cancelBorrow")
    @ApiOperation("pc取消借款")
    public ResponseEntity<VoBaseResp> pcCancelBorrow(@Valid @ModelAttribute VoCancelBorrow voCancelBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voCancelBorrow.setUserId(userId);
        return borrowBiz.cancelBorrow(voCancelBorrow);
    }

    /**
     * 后台结束借款
     * @param voPcCancelThirdBorrow
     * @return
     */
    @PostMapping("/pub/pc/borrow/cancelBorrow")
    @ApiOperation("pc取消借款")
    public ResponseEntity<VoBaseResp> cancelBorrow(@Valid @ModelAttribute VoPcCancelThirdBorrow voPcCancelThirdBorrow) {
        try {
            return borrowBiz.pcCancelBorrow(voPcCancelThirdBorrow);
        } catch (Exception e) {
            log.error("后台结束借款异常：",e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!"));
        }
    }

    /**
     * pc 新增净值借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    @PostMapping("/borrow/pc/addNetWorth")
    @ApiOperation("发布净值借款")
    public ResponseEntity<VoBaseResp> addNetWorth(@Valid @ModelAttribute VoAddNetWorthBorrow voAddNetWorthBorrow, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voAddNetWorthBorrow.setUserId(userId);
        return borrowBiz.addNetWorth(voAddNetWorthBorrow);
    }

    /**
     * 登记官方借款（车贷标、渠道标）
     *
     * @param voRegisterOfficialBorrow
     * @return
     */
    @PostMapping("/pub/pc/borrow/official/register")
    @ApiOperation("登记官方借款（车贷标、渠道标、转让标）")
    public ResponseEntity<VoHtmlResp> registerOfficialBorrow(HttpServletRequest request, @ModelAttribute @Valid VoRegisterOfficialBorrow voRegisterOfficialBorrow) {
        return borrowBiz.registerOfficialBorrow(voRegisterOfficialBorrow, request);
    }


    /**
     * 初审
     *
     * @param voPcDoFirstVerity
     * @return
     * @throws Exception
     */
    @PostMapping("/pub/borrow/pc/verify/first")
    @ApiOperation("pc初审")
    public ResponseEntity<VoBaseResp> pcFirstVerify(@ModelAttribute VoPcDoFirstVerity voPcDoFirstVerity) throws Exception {
        return borrowBiz.pcFirstVerify(voPcDoFirstVerity);
    }

}
