package com.gofobao.framework.product.biz;

import com.gofobao.framework.product.vo.request.VoFindProductPlanList;
import com.gofobao.framework.product.vo.response.VoViewFindProductPlanListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/10.
 */
public interface ProductBiz {
    /**
     * 查询广富送商品列表
     */
    ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(VoFindProductPlanList voFindProductPlanList);
}
