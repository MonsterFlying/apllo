package com.gofobao.framework.product.biz;

import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import com.gofobao.framework.product.vo.response.VoViewFindProductPlanListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/21.
 */
public interface ProductCollectBiz {
    /**
     * 收藏列表
     */
    ResponseEntity<VoViewFindProductCollectListRes> FindProductCollectList(VoFindProductCollectList voFindProductCollectList);

    /**
     * 收藏商品
     */

    /**
     * 取消收藏商品
     */
}
