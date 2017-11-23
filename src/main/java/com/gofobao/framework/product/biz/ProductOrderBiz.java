package com.gofobao.framework.product.biz;

import com.gofobao.framework.product.vo.request.VoFindOrderLogisticsDetail;
import com.gofobao.framework.product.vo.response.VoViewFindOrderLogisticsDetailRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/22.
 */
public interface ProductOrderBiz {
    /**
     * 查看物流
     */
    ResponseEntity<VoViewFindOrderLogisticsDetailRes> findOrderLogisticsDetail(VoFindOrderLogisticsDetail voFindOrderLogisticsDetail);
}
