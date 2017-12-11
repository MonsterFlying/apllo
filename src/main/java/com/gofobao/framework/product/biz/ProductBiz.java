package com.gofobao.framework.product.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/10.
 */
public interface ProductBiz {

    /**
     * 下单页面
     */
    ResponseEntity<VoViewBoughtProductPlanRes> boughtProductPlan(VoBoughtProductPlan voBoughtProductPlan);

    /**
     * 立即购买
     */
    ResponseEntity<VoViewBuyProductPlanRes> buyProductPlan(VoBuyProductPlan voBuyProductPlan) throws Exception;

    /**
     * 查询广富送商品列表
     */
    ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(VoFindProductPlanList voFindProductPlanList);

    /**
     * 查询广富送商品详情
     *
     * @param voFindProductDetail
     * @return
     */
    ResponseEntity<VoViewFindProductItemDetailsRes> findProductDetail(VoFindProductDetail voFindProductDetail);

    /**
     * 查询广富送计划详情
     *
     * @param voFindProductPlanDetail
     * @return
     */
    ResponseEntity<VoViewFindProductPlanDetailRes> findProductPlanDetail(VoFindProductPlanDetail voFindProductPlanDetail);
}
