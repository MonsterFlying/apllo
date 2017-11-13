package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.product.biz.ProductBiz;
import com.gofobao.framework.product.entity.Product;
import com.gofobao.framework.product.entity.ProductItem;
import com.gofobao.framework.product.entity.ProductItemPlanRef;
import com.gofobao.framework.product.entity.ProductPlan;
import com.gofobao.framework.product.service.ProductItemPlanRefService;
import com.gofobao.framework.product.service.ProductItemService;
import com.gofobao.framework.product.service.ProductPlanService;
import com.gofobao.framework.product.service.ProductService;
import com.gofobao.framework.product.vo.request.VoFindProductPlanList;
import com.gofobao.framework.product.vo.response.VoViewFindProductPlanListRes;
import com.gofobao.framework.product.vo.response.VoProductPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by Zeke on 2017/11/10.
 */
@Service
public class ProductBizImpl implements ProductBiz {

    @Autowired
    private ProductPlanService productPlanService;
    @Autowired
    private ProductItemPlanRefService productItemPlanRefService;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private ProductService productService;
    @Value("${qiniu.domain}")
    private String qiNiuDomain;

    /**
     * 查询首页广富送列表
     *
     * @param voFindProductPlanList
     * @return
     */
    @Override
    public ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(VoFindProductPlanList voFindProductPlanList) {
        VoViewFindProductPlanListRes res = VoBaseResp.ok("查询成功!", VoViewFindProductPlanListRes.class);
        List<VoProductPlan> showProductPlanList = new ArrayList<>();
        res.setProductPlanList(showProductPlanList);

        Date nowDate = new Date();
        //查询商品计划
        //1.必须是在活动时间期间
        //2.必须是开启的
        //3.没有被删除的
        Specification<ProductPlan> pps = Specifications
                .<ProductPlan>and()
                .eq("isOpen", true)
                .eq("isDel", false)
                .predicate(new GeSpecification("startAt", new DataObject(nowDate)))
                .predicate(new LeSpecification("endAt", new DataObject(nowDate)))
                .build();
        List<ProductPlan> productPlanList = productPlanService.findList(pps,
                new PageRequest(voFindProductPlanList.getPageIndex(), voFindProductPlanList.getPageSize()));
        if (!CollectionUtils.isEmpty(productPlanList)) {
            Set<Long> productPlanIds = productPlanList.stream().map(ProductPlan::getId)
                    .collect(Collectors.toSet());
            //查询子商品与商品计划的关联
            Specification<ProductItemPlanRef> piprs = Specifications
                    .<ProductItemPlanRef>and()
                    .in("planId", productPlanIds.toArray())
                    .eq("isDel", false)
                    .build();
            List<ProductItemPlanRef> productItemPlanRefs = productItemPlanRefService.findList(piprs);
            if (!CollectionUtils.isEmpty(productItemPlanRefs)) {
                /*子商品id集合*/
                Set<Long> productItemIds = productItemPlanRefs.stream().map(ProductItemPlanRef::getProductItemId)
                        .collect(Collectors.toSet());
                Specification<ProductItem> pis = Specifications
                        .<ProductItem>and()
                        .in("id", productItemIds.toArray())
                        .eq("isEnable", true)
                        .eq("isDel", false)
                        .build();
                /*子商品记录*/
                List<ProductItem> productItemList = productItemService.findList(pis);
                if (!CollectionUtils.isEmpty(productItemList)) {
                    Set<Long> productIds = productItemList.stream().map(ProductItem::getProductId).collect(Collectors.toSet());
                    Specification<Product> ps = Specifications
                            .<Product>and()
                            .in("id", productIds.toArray())
                            .eq("isDel", false)
                            .build();
                    /*商品记录*/
                    List<Product> productList = productService.findList(ps);
                    if (!CollectionUtils.isEmpty(productList)) {
                        /*子商品集合列表*/
                        Map<Long/*productId*/, List<ProductItem>> productItemMaps = productItemList.stream().collect(groupingBy(ProductItem::getProductId));
                        for (Product product : productList) {
                            List<ProductItem> productItems = productItemMaps.get(product.getId());
                            Collections.sort(productItems, Comparator.comparing(ProductItem::getDiscountPrice));
                            ProductItem productItem = productItems.get(0);
                            VoProductPlan voProductPlan = new VoProductPlan();
                            voProductPlan.setName(product.getName());
                            voProductPlan.setShowPrice(StringHelper.formatDouble(productItem.getDiscountPrice(), 10000 * 100, true));
                            voProductPlan.setImgUrl(qiNiuDomain + productItem.getImgUrl());
                            voProductPlan.setTitle(product.getTitle());
                            showProductPlanList.add(voProductPlan);
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok(res);
    }
}
