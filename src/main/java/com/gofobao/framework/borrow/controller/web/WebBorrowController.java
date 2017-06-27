package com.gofobao.framework.borrow.controller.web;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.*;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */

@RequestMapping("")
@RestController
@Slf4j
@Api(description = "首页标接口")
@SuppressWarnings("all")
public class WebBorrowController {

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


    @ApiOperation(value = "pc:首页标列表; type:-1：全部 0：车贷标；1：净值标；2：秒标；4：渠道标 ; 5流转标")
    @GetMapping("pub/pc/borrow/list/v2/{type}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoPcBorrowListWarpRes> pcList(@PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize,
                                                          @PathVariable Integer type) {
        VoBorrowListReq voBorrowListReq = new VoBorrowListReq();
        voBorrowListReq.setPageIndex(pageIndex);
        voBorrowListReq.setPageSize(pageSize);
        voBorrowListReq.setType(type);
        return borrowBiz.pcFindAll(voBorrowListReq);
    }



    @ApiOperation("标信息")
    @GetMapping("pub/pc/borrow/v2/info/{borrowId}")
    public ResponseEntity<VoViewBorrowInfoWarpRes> pcgetByBorrowId(@PathVariable Long borrowId) {
        return borrowBiz.info(borrowId);
    }


    @ApiOperation("pc：标简介")
    @GetMapping("pub/pc/borrow/v2/desc/{borrowId}")
    public ResponseEntity<VoViewVoBorrowDescWarpRes> pcDesc(@PathVariable Long borrowId) {
        return borrowBiz.desc(borrowId);
    }



    @ApiOperation(value = "pc:标合同")
    @GetMapping(value = "pub/pc/borrow/v2/borrowProtocol/{borrowId}")
    public ResponseEntity<String> pcTakeRatesDesc(HttpServletRequest request, @PathVariable Long borrowId) {
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
        try {
            Map<String, Object> paramMaps = borrowBiz.pcContract(borrowId, userId);
            content = thymeleafHelper.build("borrowProtcol", paramMaps);
        } catch (Exception e) {
            e.printStackTrace();
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }


    @ApiOperation(value = "pc：招标中统计")
    @GetMapping(value = "pub/pc/borrow/v2/statistics")
    public ResponseEntity<VoViewBorrowStatisticsWarpRes> pcStatistics() {
        return borrowBiz.statistics();
    }

}
