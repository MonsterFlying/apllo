package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.member.entity.UserAddress;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserAddressService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.product.biz.ProductBiz;
import com.gofobao.framework.product.entity.*;
import com.gofobao.framework.product.service.*;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Created by Zeke on 2017/11/10.
 */
@Service
public class ProductBizImpl implements ProductBiz {

    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductItemSkuRefService productItemSkuRefService;
    @Autowired
    private ProductSkuService productSkuService;
    @Autowired
    private ProductPlanService productPlanService;
    @Autowired
    private UserAddressService userAddressService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductSkuClassifyService productSkuClassifyService;
    @Autowired
    private ProductOrderService productOrderService;
    @Autowired
    private ProductLogisticsService productLogisticsService;
    @Autowired
    private ProductOrderBuyLogService productOrderBuyLogService;
    @Autowired
    private ProductCollectService productCollectService;

    /**
     * 立即购买
     *
     * @// TODO: 2017/11/24 如果sku没有套餐则算作现金购买
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoViewBuyProductPlanRes> buyProductPlan(VoBuyProductPlan voBuyProductPlan) throws Exception {
        VoViewBuyProductPlanRes res = VoBaseResp.ok("购买成功!", VoViewBuyProductPlanRes.class);
        Date nowDate = new Date();
        /*用户id*/
        long userId = voBuyProductPlan.getUserId();
        /*用户资产记录*/
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "用户资产记录不存在!");
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在!");
        /*子商品id*/
        long productItemId = voBuyProductPlan.getProductItemId();
        ProductItem productItem = productItemService.findByIdLock(productItemId);
        Preconditions.checkNotNull(productItem, "子商品记录不存在!");
        /*广富送计划id*/
        long planId = voBuyProductPlan.getPlanId();
        ProductPlan productPlan = productPlanService.findById(planId);
        Preconditions.checkNotNull(productPlan, "广富送计划记录不存在!");
        /*用户收货地址id*/
        long addressId = voBuyProductPlan.getAddressId();
        UserAddress userAddress = userAddressService.findById(addressId);
        Preconditions.checkNotNull(userAddress, "用户地址记录不存在!");
        /*用户支付方式*/
        int payMode = voBuyProductPlan.getPayMode();
        //验证用户
        //1.验证用户是否锁定
        if (users.getIsLock()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "您的账户已被冻结，详情请联系客服人员!", VoViewBuyProductPlanRes.class));
        }
        //2.用户资金是否充足
        if (payMode == 0 && asset.getUseMoney() < productPlan.getLowest()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "您的可用余额不足，请先充值!", VoViewBuyProductPlanRes.class));
        }
        //验证广富送计划
        //1.验证广富送是否在活动时间内
        Date startAt = productPlan.getStartAt();
        Date endAt = productPlan.getEndAt();
        if (startAt.getTime() > nowDate.getTime() || endAt.getTime() < nowDate.getTime()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "商品未到购买时间!", VoViewBuyProductPlanRes.class));
        }
        if (!productPlan.getIsOpen()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "商品购买还未开始!", VoViewBuyProductPlanRes.class));
        }
        //2.验证子商品是否上架
        if (!productItem.getIsEnable()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "商品未上架!", VoViewBuyProductPlanRes.class));
        }
        //3.判断库存是否充足
        if (productItem.getInventory() <= 0) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "商品库存不足!", VoViewBuyProductPlanRes.class));
        }

        //计算额外收益
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(productPlan.getLowest()), new Double(productPlan.getApr()), productPlan.getTimeLimit(), null);
        Map<String, Object> rsMap = borrowCalculatorHelper.ycxhbfx();
        long earnings = NumberHelper.toLong(rsMap.get("earnings"));
        //生成订单
        String orderNumber = RandomHelper.generateNumberCode(18);
        ProductOrder order = new ProductOrder();
        order.setUserId(userId);
        order.setDiscountsMoney(0L);
        order.setEarnings(earnings);
        order.setFee(0L);
        order.setType(productPlan.getType());
        order.setIsDel(false);
        order.setOrderNumber(orderNumber);
        order.setProductMoney(productItem.getDiscountPrice());
        order.setStatus(1);
        order.setUpdatedAt(nowDate);
        order.setCreatedAt(nowDate);
        productOrderService.save(order);
        //绑定订单与购买子商品
        ProductOrderBuyLog productOrderBuyLog = new ProductOrderBuyLog();
        productOrderBuyLog.setProductItemId(productItemId);
        productOrderBuyLog.setProductOrderId(order.getId());
        productOrderBuyLog.setProductMoney(productItem.getDiscountPrice());
        productOrderBuyLog.setPlanId(planId);
        productOrderBuyLog.setPayMoney(productPlan.getLowest());
        productOrderBuyLog.setProductMoney(productItem.getDiscountPrice());
        productOrderBuyLog.setDiscountsMoney(0L);
        productOrderBuyLog.setUpdatedAt(nowDate);
        productOrderBuyLog.setCreatedAt(nowDate);
        productOrderBuyLogService.save(productOrderBuyLog);
        //新增物流收货记录
        ProductLogistics logistics = new ProductLogistics();
        logistics.setUserId(userId);
        logistics.setOrderNumber(orderNumber);
        logistics.setState(0);
        logistics.setName(userAddress.getName());
        logistics.setCity(userAddress.getCity());
        logistics.setCountry(userAddress.getCountry());
        logistics.setDetailedAddress(userAddress.getDetailedAddress());
        logistics.setDistrict(userAddress.getDistrict());
        logistics.setPhone(userAddress.getPhone());
        logistics.setProvince(userAddress.getProvince());
        logistics.setUpdateAt(nowDate);
        logistics.setCreateAt(nowDate);
        productLogisticsService.save(logistics);

        res.setOrderNumber(orderNumber);
        return ResponseEntity.ok(res);
    }


    /**
     * 下单页面
     */
    @Override
    public ResponseEntity<VoViewBoughtProductPlanRes> boughtProductPlan(VoBoughtProductPlan voBoughtProductPlan) {
        VoViewBoughtProductPlanRes res = VoBaseResp.ok("查询成功!", VoViewBoughtProductPlanRes.class);
        /*用户id*/
        long userId = voBoughtProductPlan.getUserId();
        /*子商品id*/
        long productItemId = voBoughtProductPlan.getProductItemId();
        /* 用户资产记录 */
        Asset asset = assetService.findByUserId(userId);
        Preconditions.checkNotNull(asset, "用户资产记录不存在!");
        /*子商品记录*/
        ProductItem productItem = productItemService.findById(productItemId);
        Preconditions.checkNotNull(productItem, "你看到的商品消失啦!");
        /*主商品记录*/
        Product product = productService.findById(productItem.getProductId());
        Preconditions.checkNotNull(product, "你看到的商品消失啦!");
        /*广富送计划记录*/
        ProductPlan productPlan = getProductPlan(productItemId);
        Preconditions.checkNotNull(productItem, "你看到的商品消失啦!");

        /*用户收货地址*/
        VoUserAddress voUserAddress = new VoUserAddress();
        /*是否存在收货地址*/
        boolean existAddress = false;
        /*查询用户收货地址*/
        Specification<UserAddress> uas = Specifications
                .<UserAddress>and()
                .eq("userId", userId)
                .eq("isDel", false)
                .build();
        List<UserAddress> userAddressList = userAddressService.findList(uas, new Sort(Sort.Direction.DESC, "isDefault", "updateAt"));
        if (!CollectionUtils.isEmpty(userAddressList)) {
            UserAddress userAddress = userAddressList.get(0);
            /*地址字符串*/
            StringBuffer addressStr = new StringBuffer();
            addressStr.append(ObjectUtils.isEmpty(userAddress.getCountry()) ? "" : (userAddress.getCountry() + " "));
            addressStr.append(ObjectUtils.isEmpty(userAddress.getProvince()) ? "" : (userAddress.getProvince() + " "));
            addressStr.append(ObjectUtils.isEmpty(userAddress.getCity()) ? "" : (userAddress.getCity() + " "));
            addressStr.append(ObjectUtils.isEmpty(userAddress.getDistrict()) ? "" : (userAddress.getDistrict()));

            voUserAddress.setAddressId(StringHelper.toString(userAddress.getId()));
            voUserAddress.setName(userAddress.getName());
            voUserAddress.setPhone(userAddress.getPhone());
            voUserAddress.setAddress(addressStr.toString());

            existAddress = true;
        } else {
            voUserAddress.setAddressId("");
            voUserAddress.setName("");
            voUserAddress.setPhone("");
            voUserAddress.setAddress("");
        }

        /*子商品与sku关联*/
        Specification<ProductItemSkuRef> pisrs = Specifications
                .<ProductItemSkuRef>and()
                .eq("productItemId", productItem.getId())
                .build();
        List<ProductItemSkuRef> productItemSkuRefList = productItemSkuRefService.findList(pisrs);
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

        /*子商品与sku的关联*/
        List<VoSku> skuList = new ArrayList<>();
        productItemSkuRefList.stream().forEach(productItemSkuRef -> {
            ProductSku productSku = productSkuMap.get(productItemSkuRef.getSkuId());
            ProductSkuClassify productSkuClassify = productSkuClassifyMap.get(productSku.getScId());
            //sku 对象
            VoSku sku = new VoSku();
            sku.setId(String.valueOf(productItemSkuRef.getId()));
            sku.setClassId(String.valueOf(productSkuClassify.getId()));
            sku.setName(productSku.getName());
            sku.setClassNo(String.valueOf(productSkuClassify.getNo()));
            sku.setNo(String.valueOf(productSku.getNo()));
            sku.setClassName(productSkuClassify.getName());
            skuList.add(sku);
        });


        /*计算额外收益*/
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(productPlan.getLowest()), new Double(productPlan.getApr()), productPlan.getTimeLimit(), null);
        Map<String, Object> rsMap = borrowCalculatorHelper.ycxhbfx();

        res.setPlanId(String.valueOf(productPlan.getId()));
        res.setProductItemId(String.valueOf(productItem.getId()));
        res.setName(product.getName());
        res.setSkuList(skuList);
        res.setExistAddress(existAddress);
        res.setTitle(product.getTitle());
        res.setImgUrl(!ObjectUtils.isEmpty(productItem.getImgUrl()) ? product.getImgUrl() : productItem.getImgUrl());
        res.setLowest(StringHelper.formatDouble(productPlan.getLowest(), 100, false));
        res.setShowLowest(StringHelper.formatDouble(productPlan.getLowest(), 100, true));
        res.setPlanName(productPlan.getName());
        res.setUseMoney(StringHelper.formatDouble(asset.getUseMoney(), 100, false));
        res.setShowUseMoney(StringHelper.formatDouble(asset.getUseMoney(), 100, true));
        res.setEarnings(StringHelper.formatDouble(NumberHelper.toDouble(rsMap.get("earnings")), 100, true));
        res.setUserAddress(voUserAddress);
        return ResponseEntity.ok(res);
    }

    /**
     * 查询广富送计划详情
     *
     * @param voFindProductPlanDetail
     * @return
     */
    @Override
    public ResponseEntity<VoViewFindProductPlanDetailRes> findProductPlanDetail(VoFindProductPlanDetail voFindProductPlanDetail) {
        VoViewFindProductPlanDetailRes res = VoBaseResp.ok("查询成功!", VoViewFindProductPlanDetailRes.class);
        /*子商品id*/
        long productItemId = voFindProductPlanDetail.getProductItemId();
        /*子商品记录*/
        ProductItem productItem = productItemService.findById(productItemId);
        Preconditions.checkNotNull(productItem, "你看到的商品消失啦!");
        /*广富送计划记录*/
        ProductPlan productPlan = getProductPlan(productItemId);
        Preconditions.checkNotNull(productItem, "你看到的商品消失啦!");

        res.setApr(StringHelper.formatDouble(productPlan.getApr(), 100, false) + "%");
        res.setLower(StringHelper.formatDouble(productPlan.getLowest(), 100, true) + "元");
        res.setFeeRatio(StringHelper.formatDouble(productPlan.getFeeRatio(), 100, false) + "%");
        res.setTimeLimit(String.valueOf(productPlan.getTimeLimit()));
        res.setRepayPattern("还款方式");
        res.setSafety("江西银行存管、车辆抵质押");
        res.setStartDate("投标后次日起息");
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

    /**
     * 查询广富送商品详情
     *
     * @param voFindProductDetail
     * @return
     */
    @Override
    public ResponseEntity<VoViewFindProductItemDetailsRes> findProductDetail(VoFindProductDetail voFindProductDetail) {
        VoViewFindProductItemDetailsRes voViewFindProductItemDetailsRes = VoBaseResp.ok("查询成功!", VoViewFindProductItemDetailsRes.class);
        List<SkuSiftKey> siftKey = new ArrayList<>();
        List<VoProductItemDetail> skuData = new ArrayList<>();
        voViewFindProductItemDetailsRes.setSkuData(skuData);
        voViewFindProductItemDetailsRes.setSiftKey(siftKey);
        /*用户id*/
        long userId = voFindProductDetail.getUserId();
        /*子商品id*/
        long productItemId = voFindProductDetail.getProductItemId();
        ProductItem productItem = productItemService.findById(productItemId);
        long productId = productItem.getProductId();
        /*商品记录*/
        Product product = productService.findById(productId);
        if (!ObjectUtils.isEmpty(product)) {
            /*查询子商品记录*/
            Specification<ProductItem> pis = Specifications
                    .<ProductItem>and()
                    .eq("productId", productId)
                    .eq("isEnable", true)
                    .build();
            List<ProductItem> productItemList = productItemService.findList(pis);
            if (!CollectionUtils.isEmpty(productItemList)) {
                /*子商品id集合*/
                Set<Long> productItemIds = productItemList.stream().map(ProductItem::getId).collect(toSet());
                Specification<ProductItemSkuRef> pisrs = Specifications
                        .<ProductItemSkuRef>and()
                        .in("productItemId", productItemIds.toArray())
                        .build();
                List<ProductItemSkuRef> productItemSkuRefList = productItemSkuRefService.findList(pisrs);
                if (CollectionUtils.isEmpty(productItemSkuRefList)) {
                    return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "商品未上架!", VoViewFindProductItemDetailsRes.class));
                }
                Map<Long, List<ProductItemSkuRef>> productItemSkuRefsMap = productItemSkuRefList.stream().collect(groupingBy(ProductItemSkuRef::getProductItemId));
                /*skuId集合*/
                Set<Long> skuIds = productItemSkuRefList.stream().map(ProductItemSkuRef::getSkuId).collect(toSet());
                Specification<ProductSku> pss = Specifications
                        .<ProductSku>and()
                        .in("id", skuIds.toArray())
                        .build();
                List<ProductSku> productSkuList = productSkuService.findList(pss, new Sort(Sort.Direction.DESC, "no"));
                Set<Long> skuClassifyIds = productSkuList.stream().map(ProductSku::getScId).collect(Collectors.toSet());
                Map<Long, ProductSku> productSkuMap = productSkuList.stream().collect(Collectors.toMap(ProductSku::getId, Function.identity()));
                /*sku分类集合*/
                Specification<ProductSkuClassify> pscs = Specifications
                        .<ProductSkuClassify>and()
                        .in("id", skuClassifyIds.toArray())
                        .build();
                List<ProductSkuClassify> productSkuClassifyList = productSkuClassifyService.findList(pscs, new Sort(Sort.Direction.DESC, "no"));

                productSkuClassifyList.stream().forEach(skuClassify -> {
                    SkuSiftKey skuSiftKey = new SkuSiftKey();
                    skuSiftKey.setSkuClassId(String.valueOf(skuClassify.getId()));
                    skuSiftKey.setSkuClassName(skuClassify.getName());
                    List<SkuSiftKey.Sku> skuList = new ArrayList<>();
                    productSkuList.stream().forEach(productSku -> {
                        SkuSiftKey.Sku sku = skuSiftKey.new Sku();
                        if (skuClassify.getId().intValue() == productSku.getScId().intValue()) {
                            sku.setSkuName(productSku.getName());
                            sku.setSkuId(String.valueOf(productSku.getId()));
                            skuList.add(sku);
                        }
                    });
                    skuSiftKey.setSkuList(skuList);
                    siftKey.add(skuSiftKey);
                });

                //装填子商品列表
                for (ProductItem item : productItemList) {
                    /*子商品与sku的关联*/
                    List<ProductItemSkuRef> productItemSkuRefs = productItemSkuRefsMap.get(item.getId());
                    /*子商品sku字符串*/
                    StringBuffer skuStr = new StringBuffer();
                    productItemSkuRefs.stream().forEach(productItemSkuRef -> {
                        ProductSku productSku = productSkuMap.get(productItemSkuRef.getSkuId());
                        skuStr.append(productSku.getId()).append(";");
                    });

                    VoProductItemDetail voProductItemDetail = new VoProductItemDetail();
                    voProductItemDetail.setName(product.getName());
                    voProductItemDetail.setImgUrl(item.getImgUrl());
                    voProductItemDetail.setTitle(product.getTitle());
                    voProductItemDetail.setDetails(ObjectUtils.isEmpty(item.getDetails()) ? product.getDetails() : item.getDetails());
                    voProductItemDetail.setDiscountPrice(StringHelper.formatDouble(item.getDiscountPrice(), 100, true));
                    voProductItemDetail.setPrice(StringHelper.formatDouble(item.getPrice(), 100, true));
                    voProductItemDetail.setInventory(String.valueOf(item.getInventory()));
                    voProductItemDetail.setProductItemId(item.getId());
                    voProductItemDetail.setQAndA(ObjectUtils.isEmpty(item.getQAndA()) ? product.getQAndA() : item.getQAndA());
                    voProductItemDetail.setAfterSalesService(ObjectUtils.isEmpty(item.getAfterSalesService()) ? product.getAfterSalesService() : item.getAfterSalesService());
                    //判断是否收藏
                    voProductItemDetail.setIsCollect(isCollect(userId, item.getId()));
                    voProductItemDetail.setSkuIds(skuStr.substring(0, skuStr.length() - 1));

                    skuData.add(voProductItemDetail);
                }
            }
        }

        return ResponseEntity.ok(voViewFindProductItemDetailsRes);
    }

    /**
     * 判断商品是否收藏
     *
     * @param userId
     * @param productItemId
     * @return
     */
    private boolean isCollect(long userId, long productItemId) {
        if (userId < 1 || productItemId < 1) {
            return false;
        }

        Specification<ProductCollect> pcs = Specifications
                .<ProductCollect>and()
                .eq("userId", userId)
                .eq("productItemId", productItemId)
                .build();
        long count = productCollectService.count(pcs);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

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
                .predicate(new LeSpecification("startAt", new DataObject(nowDate)))
                .predicate(new GeSpecification("endAt", new DataObject(nowDate)))
                .build();
        List<ProductPlan> productPlanList = productPlanService.findList(pps,
                new PageRequest(voFindProductPlanList.getPageIndex(), voFindProductPlanList.getPageSize()));
        if (!CollectionUtils.isEmpty(productPlanList)) {
            Set<Long> productPlanIds = productPlanList.stream().map(ProductPlan::getId)
                    .collect(Collectors.toSet());
            //查询子商品与商品计划的关联
            Specification<ProductSku> piprs = Specifications
                    .<ProductSku>and()
                    .in("planId", productPlanIds.toArray())
                    .eq("type", 1)
                    .eq("isDel", false)
                    .build();
            List<ProductSku> productSkuList = productSkuService.findList(piprs);
            if (!CollectionUtils.isEmpty(productSkuList)) {
                /*skuId集合*/
                Set<Long> skuIds = productSkuList.stream().map(ProductSku::getId).collect(Collectors.toSet());
                Specification<ProductItemSkuRef> ptrs = Specifications
                        .<ProductItemSkuRef>and()
                        .in("skuId", skuIds.toArray())
                        .build();
                List<ProductItemSkuRef> productItemSkuRefList = productItemSkuRefService.findList(ptrs);
                /*子商品id集合*/
                Set<Long> productItemIds = productItemSkuRefList.stream().map(ProductItemSkuRef::getProductItemId)
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
                            voProductPlan.setProductItemId(productItem.getId());
                            voProductPlan.setName(product.getName());
                            voProductPlan.setShowPrice(formatPrice(productItem.getDiscountPrice()));
                            voProductPlan.setImgUrl(productItem.getImgUrl());
                            voProductPlan.setTitle(product.getTitle());
                            showProductPlanList.add(voProductPlan);
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok(res);
    }

    private String formatPrice(long price) {
        if ((price / 10000 * 100) < 0) {
            return StringHelper.formatDouble(price, 100, true) + "元";
        } else {
            return StringHelper.formatDouble(price, 10000 * 100, true) + "万元";
        }
    }
}
