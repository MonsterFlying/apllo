package com.gofobao.framework.product.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.vo.request.VoCancelCollectProduct;
import com.gofobao.framework.product.vo.request.VoCollectProduct;
import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import com.gofobao.framework.product.vo.response.VoViewFindProductPlanListRes;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/21.
 */
public interface ProductCollectBiz {
    /**
     * 收藏列表
     */
    ResponseEntity<VoViewFindProductCollectListRes> findProductCollectList(VoFindProductCollectList voFindProductCollectList);

    /**
     * 收藏商品
     */

    /**
     * 收藏商品
     */
    ResponseEntity<VoBaseResp> collectProduct(VoCollectProduct voCollectProduct);

    /**
     * 取消收藏商品
     */
    ResponseEntity<VoBaseResp> cancelCollectProduct(VoCancelCollectProduct voCancelCollectProduct);
}

