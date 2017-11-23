package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductOrderBiz;
import com.gofobao.framework.product.entity.ProductLogistics;
import com.gofobao.framework.product.service.ProductLogisticsService;
import com.gofobao.framework.product.vo.request.VoFindOrderLogisticsDetail;
import com.gofobao.framework.product.vo.response.VoViewFindOrderLogisticsDetailRes;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zeke on 2017/11/22.
 */
@Service
public class ProductOrderBizImpl implements ProductOrderBiz {

    @Autowired
    private ProductLogisticsService productLogisticsService;

    /**
     * 查看物流
     */
    @Override
    public ResponseEntity<VoViewFindOrderLogisticsDetailRes> findOrderLogisticsDetail(VoFindOrderLogisticsDetail voFindOrderLogisticsDetail) {
        VoViewFindOrderLogisticsDetailRes res = VoBaseResp.ok("查询成功!", VoViewFindOrderLogisticsDetailRes.class);
        /*userId*/
        long userId = voFindOrderLogisticsDetail.getUserId();
        /*订单号*/
        String orderNumber = voFindOrderLogisticsDetail.getOrderNumber();
        Specification<ProductLogistics> pls = Specifications
                .<ProductLogistics>and()
                .eq("userId", userId)
                .eq("orderNumber", orderNumber)
                .build();
        List<ProductLogistics> productLogisticsList = productLogisticsService.findList(pls);
        Preconditions.checkState(!CollectionUtils.isEmpty(productLogisticsList), "订单不存在!");
        ProductLogistics productLogistics = productLogisticsList.get(0);
        res.setExpressName(productLogistics.getExpressName());
        res.setExpressNumber(productLogistics.getExpressNumber());
        res.setExpressmanName("");
        res.setExpressmanPhone("");
        res.setStatus("0");
        res.setProductLogisticsList(new ArrayList<>());
        return ResponseEntity.ok(res);
    }
}
