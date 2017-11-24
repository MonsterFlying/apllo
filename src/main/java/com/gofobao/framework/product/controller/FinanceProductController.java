package com.gofobao.framework.product.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductBiz;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/10.
 */
@RestController
@RequestMapping("/finance")
@Slf4j
@Api(description = "商品控制器")
public class FinanceProductController {

    @Autowired
    private ProductBiz productBiz;
    @Autowired
    private JwtTokenHelper jwtTokenHelper;
    @Value("${jwt.header}")
    private String tokenHeader;
    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 立即购买
     */
    @PostMapping("/v2/product/buy")
    @ApiOperation("立即购买")
    public ResponseEntity<VoViewBuyProductPlanRes> buyProductPlan(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoBuyProductPlan voBuyProductPlan) {
        voBuyProductPlan.setUserId(userId);
        try {
            return productBiz.buyProductPlan(voBuyProductPlan);
        } catch (Exception e) {
            log.error("广富送立即购买失败：", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "购买失败！", VoViewBuyProductPlanRes.class));
        }
    }

    /**
     * 下单页面
     */
    @PostMapping("/v2/product/bought")
    @ApiOperation("下单页面")
    public ResponseEntity<VoViewBoughtProductPlanRes> boughtProductPlan(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoBoughtProductPlan voBoughtProductPlan) {
        voBoughtProductPlan.setUserId(userId);
        return productBiz.boughtProductPlan(voBoughtProductPlan);
    }

    /**
     * 查询首页广富送列表
     *
     * @param voFindProductPlanList
     * @return
     */
    @PostMapping("/pub/v2/product/plan/list")
    @ApiOperation("查询首页广富送列表")
    public ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(@Valid @ModelAttribute VoFindProductPlanList voFindProductPlanList) {
        return productBiz.findProductPlanList(voFindProductPlanList);
    }


    /**
     * 查询广富送商品详情
     *
     * @param voFindProductDetail
     * @return
     */
    @PostMapping("/pub/v2/product/detail")
    @ApiOperation("查询广富送商品详情")
    public ResponseEntity<VoViewFindProductItemDetailsRes> findProductDetail(@Valid @ModelAttribute VoFindProductDetail voFindProductDetail, HttpServletRequest request) {
        Long userId = 0L;
        String authToken = request.getHeader(this.tokenHeader);
        if (!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))) {
            authToken = authToken.substring(7);
        }
        String username = jwtTokenHelper.getUsernameFromToken(authToken);
        if (!StringUtils.isEmpty(username)) {
            userId = jwtTokenHelper.getUserIdFromToken(authToken);
        }
        voFindProductDetail.setUserId(userId);
        return productBiz.findProductDetail(voFindProductDetail);
    }
}
