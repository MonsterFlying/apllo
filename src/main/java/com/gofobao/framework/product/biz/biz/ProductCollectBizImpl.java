package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.product.biz.ProductCollectBiz;
import com.gofobao.framework.product.entity.*;
import com.gofobao.framework.product.service.*;
import com.gofobao.framework.product.vo.request.VoCancelCollectProduct;
import com.gofobao.framework.product.vo.request.VoCollectProduct;
import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoProductCollect;
import com.gofobao.framework.product.vo.response.VoSku;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Created by Zeke on 2017/11/21.
 */
@Service
public class ProductCollectBizImpl implements ProductCollectBiz {

    @Autowired
    private ProductCollectService productCollectService;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductItemSkuRefService productItemSkuRefService;
    @Autowired
    private ProductSkuClassifyService productSkuClassifyService;
    @Autowired
    private ProductSkuService productSkuService;
    @Autowired
    private ProductPlanService productPlanService;


    /**
     * 取消收藏商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelCollectProduct(VoCancelCollectProduct voCancelCollectProduct) {
        /*用户id*/
        long userId = voCancelCollectProduct.getUserId();
        /*子商品id*/
        long productItemId = voCancelCollectProduct.getProductItemId();
        Specification<ProductCollect> pcs = Specifications
                .<ProductCollect>and()
                .eq("userId", userId)
                .eq("productItemId", productItemId)
                .build();
        List<ProductCollect> productCollectList = productCollectService.findList(pcs);
        Preconditions.checkState(!CollectionUtils.isEmpty(productCollectList), "这个收藏已经不见啦！");
        productCollectList.stream().forEach(productCollect -> {
            productCollectService.del(productCollect);
        });
        return ResponseEntity.ok(VoBaseResp.ok("取消收藏成功!"));
    }

    /**
     * 收藏商品
     */
    @Override
    public ResponseEntity<VoBaseResp> collectProduct(VoCollectProduct voCollectProduct) {
        /*用户id*/
        long userId = voCollectProduct.getUserId();
        /*子商品id*/
        long productItemId = voCollectProduct.getProductItemId();
        ProductItem productItem = productItemService.findById(productItemId);
        Preconditions.checkNotNull(productItem, "商品不存在！");
        //收藏商品
        ProductCollect productCollect = new ProductCollect();
        productCollect.setProductItemId(productItemId);
        productCollect.setUserId(userId);
        productCollectService.save(productCollect);
        return ResponseEntity.ok(VoBaseResp.ok("收藏成功!"));
    }

    /**
     * 收藏列表
     */
    @Override
    public ResponseEntity<VoViewFindProductCollectListRes> findProductCollectList(VoFindProductCollectList voFindProductCollectList) {
        VoViewFindProductCollectListRes res = VoBaseResp.ok("查询成功", VoViewFindProductCollectListRes.class);
        List<VoProductCollect> voProductCollectList = new ArrayList<>();
        res.setProductCollectList(voProductCollectList);
        /*用户id*/
        long userId = voFindProductCollectList.getUserId();
        Specification<ProductCollect> pcs = Specifications
                .<ProductCollect>and()
                .eq("userId", userId)
                .build();
        List<ProductCollect> productCollectList = productCollectService.findList(pcs, new PageRequest(voFindProductCollectList.getPageIndex(), voFindProductCollectList.getPageSize()));
        if (!CollectionUtils.isEmpty(productCollectList)) {
            /*子商品id集合*/
            Set<Long> productItemIds = productCollectList.stream().map(ProductCollect::getProductItemId).collect(Collectors.toSet());
            Specification<ProductItem> pis = Specifications
                    .<ProductItem>and()
                    .in("id", productItemIds.toArray())
                    .build();
            List<ProductItem> productItemList = productItemService.findList(pis);
            if (!CollectionUtils.isEmpty(productItemList)) {
                /*商品id*/
                Set<Long> productIds = productItemList.stream().map(ProductItem::getProductId).collect(Collectors.toSet());
                Specification<Product> ps = Specifications
                        .<Product>and()
                        .in("id", productIds.toArray())
                        .build();
                List<Product> productList = productService.findList(ps);
                Map<Long, Product> productMap = productList.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
                 /*子商品与sku关联*/
                Specification<ProductItemSkuRef> pisrs = Specifications
                        .<ProductItemSkuRef>and()
                        .in("productItemId", productItemIds.toArray())
                        .build();
                List<ProductItemSkuRef> productItemSkuRefList = productItemSkuRefService.findList(pisrs);
                Map<Long/*子商品id*/, List<ProductItemSkuRef>> productItemSkuRefMaps = productItemSkuRefList.stream().collect(groupingBy(ProductItemSkuRef::getProductItemId));
                /*skuId集合*/
                Set<Long> skuIds = productItemSkuRefList.stream().map(ProductItemSkuRef::getSkuId).collect(toSet());
                Specification<ProductSku> pss = Specifications
                        .<ProductSku>and()
                        .in("id", skuIds.toArray())
                        .build();
                List<ProductSku> productSkuList = productSkuService.findList(pss);
                Set<Long> skuClassifyIds = productSkuList.stream().map(ProductSku::getScId).collect(Collectors.toSet());
                Map<Long, ProductSku> productSkuMap = productSkuList.stream().collect(Collectors.toMap(ProductSku::getId, Function.identity()));
                /*sku分类集合*/
                Specification<ProductSkuClassify> pscs = Specifications
                        .<ProductSkuClassify>and()
                        .in("id", skuClassifyIds.toArray())
                        .build();
                List<ProductSkuClassify> productSkuClassifyList = productSkuClassifyService.findList(pscs);
                Map<Long, ProductSkuClassify> productSkuClassifyMap = productSkuClassifyList.stream().collect(Collectors.toMap(ProductSkuClassify::getId, Function.identity()));
                //查询商品计划

                productItemList.stream().forEach(productItem -> {
                    List<VoSku> skuList = new ArrayList<>();
                    /*子商品sku关联集合*/
                    List<ProductItemSkuRef> productItemSkuRefs = productItemSkuRefMaps.get(productItem.getId());
                    /*计划*/
                    ProductPlan productPlan = getProductPlan(productItem.getId());
                    /*商品对象*/
                    productItemSkuRefs.stream().forEach(productItemSkuRef -> {
                        ProductSku productSku = productSkuMap.get(productItemSkuRef.getSkuId());
                        ProductSkuClassify productSkuClassify = productSkuClassifyMap.get(productSku.getScId());
                        //sku 对象
                        VoSku sku = new VoSku();
                        sku.setId(String.valueOf(productItemSkuRef.getId()));
                        sku.setClassId(String.valueOf(productSkuClassify.getId()));
                        sku.setName(productSku.getName());
                        sku.setClassNo(String.valueOf(productSkuClassify.getNo()));
                        sku.setClassName(productSkuClassify.getName());
                        sku.setNo(String.valueOf(productSku.getNo()));
                        skuList.add(sku);
                    });

                    Product product = productMap.get(productItem.getProductId());
                    VoProductCollect voProductCollect = new VoProductCollect();
                    voProductCollect.setProductItemId(productItem.getId());
                    voProductCollect.setIsEnable(productItem.getIsEnable());
                    voProductCollect.setName(product.getName());
                    voProductCollect.setTitle(product.getTitle());
                    voProductCollect.setImgUrl(ObjectUtils.isEmpty(productItem.getImgUrl()) ? product.getImgUrl() : productItem.getImgUrl());
                    voProductCollect.setSkuList(skuList);
                    voProductCollect.setLowest(formatPrice(productPlan.getLowest()));
                    voProductCollectList.add(voProductCollect);
                });
            }
        }
        return ResponseEntity.ok(res);
    }

    /**
     * 通过子商品id获取广富送计划
     *
     * @param productItemId
     * @return
     */
    private ProductPlan getProductPlan(long productItemId) {
         /*查询sku*/
        Specification<ProductItemSkuRef> piss = Specifications
                .<ProductItemSkuRef>and()
                .eq("productItemId", productItemId)
                .build();
        List<ProductItemSkuRef> productItemSkuRefList = productItemSkuRefService.findList(piss);
        Preconditions.checkState(!CollectionUtils.isEmpty(productItemSkuRefList), "这个商品活动还未开始啦，请稍后。");
        /*查询套餐*/
        /*skuId集合*/
        Set<Long> skuIds = productItemSkuRefList.stream().map(ProductItemSkuRef::getSkuId).collect(Collectors.toSet());
        Specification<ProductSku> pss = Specifications
                .<ProductSku>and()
                .in("id", skuIds.toArray())
                .eq("type", 1)
                .build();
        List<ProductSku> productSkuList = productSkuService.findList(pss);
        Preconditions.checkState(!CollectionUtils.isEmpty(productSkuList), "这个商品活动还未开始啦，请稍后。");
        ProductSku productSku = productSkuList.get(0);
        /*广富送计划id*/
        long planId = productSku.getPlanId();
        /*广富送计划记录*/
        ProductPlan productPlan = productPlanService.findById(planId);
        return productPlan;
    }

    private String formatPrice(long price) {
        if ((price / 10000 * 100) < 0) {
            return StringHelper.formatDouble(price, 100, true) + "元";
        } else {
            return StringHelper.formatDouble(price, 10000 * 100, true) + "万元";
        }
    }
}
