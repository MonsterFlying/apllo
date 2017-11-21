package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.product.biz.ProductCollectBiz;
import com.gofobao.framework.product.entity.ProductCollect;
import com.gofobao.framework.product.entity.ProductItem;
import com.gofobao.framework.product.service.ProductCollectService;
import com.gofobao.framework.product.service.ProductItemService;
import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoProductCollect;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/11/21.
 */
@Service
public class ProductCollectBizImpl implements ProductCollectBiz {

    @Autowired
    private ProductCollectService productCollectService;
    @Autowired
    private ProductItemService productItemService;

    /**
     * 收藏列表
     */
    @Override
    public ResponseEntity<VoViewFindProductCollectListRes> FindProductCollectList(VoFindProductCollectList voFindProductCollectList) {
        VoViewFindProductCollectListRes res = new VoViewFindProductCollectListRes();
        List<VoProductCollect> voProductCollectList = new ArrayList<>();
        res.setProductCollectList(voProductCollectList);
        /*用户id*/
        long userId = voFindProductCollectList.getUserId();
        Specification<ProductCollect> pcs = Specifications
                .<ProductCollect>and()
                .eq("userId", userId)
                .build();
        List<ProductCollect> productCollectList = productCollectService.findList(pcs);
        if (!CollectionUtils.isEmpty(productCollectList)) {
            /*子商品id集合*/
            Set<Long> productItemIds = productCollectList.stream().map(ProductCollect::getProductItemId).collect(Collectors.toSet());
            Specification<ProductItem> pis = Specifications
                    .<ProductItem>and()
                    .in("id", productItemIds.toArray())
                    .build();
            List<ProductItem> productItemList = productItemService.findList(pis);
            if (!CollectionUtils.isEmpty(productCollectList)){
                VoProductCollect voProductCollect = new VoProductCollect();
                productCollectList.stream().forEach(productCollect -> {
                   // voProductCollect.set
                });
            }
        }
        return ResponseEntity.ok(res);
    }
}
