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
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private ProductOrderBuyLogService productOrderBuyLogService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelOrder(VoCancelOrder voCancelOrder) throws Exception {
        /*用户id*/
        long userId = voCancelOrder.getUserId();
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "资产记录不存在!");
        /*订单id*/
        String orderNumber = voCancelOrder.getOrderNumber();
        ProductOrder productOrder = productOrderService.findByOrderNumberLock(orderNumber);
        Preconditions.checkNotNull(productOrder, "订单记录不存在!");
        /*商品计划*/
        ProductPlan productPlan = productPlanService.findById(productOrder.getPlanId());
        Preconditions.checkNotNull(productOrder, "订单记录不存在!");
        //验证订单是否可以取消
        int orderStatus = productOrder.getStatus();
        Set<Integer> passStatusSet = ImmutableSet.of(1, 2);
        if (!passStatusSet.contains(orderStatus)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "订单已在备货状态，如果要取消请联系客服人员!"));
        }
        //判断是否付款，已付款需要解冻购买资金
        if (orderStatus == 2) {
            Long payMoney = productOrder.getPayMoney();
            /*交易流水号*/
            String seqNo = assetChangeProvider.getSeqNo();
            //已付款
            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.buyProductFreeze);
            assetChange.setUserId(userId);
            assetChange.setMoney(payMoney);
            assetChange.setRemark(String.format("取消购买产品[%s],冻结可用资金%s元", productPlan.getName(), StringHelper.formatDouble(payMoney / 100D, true)));
            assetChange.setSourceId(productOrder.getId());
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);
        }
        //取消订单
        productOrder.setStatus(6);
        productOrder.setUpdatedAt(new Date());
        productOrderService.save(productOrder);
        return ResponseEntity.ok(VoBaseResp.ok("取消成功!"));
    }

    /**
     * 我的订单列表页面
     */
    @Override
    public ResponseEntity<VoViewProductOrderListRes> findProductOrderList(VoFindProductOrderList voFindProductOrderList) {
        VoViewProductOrderListRes res = VoBaseResp.ok("查询成功!", VoViewProductOrderListRes.class);
        List<VoProductOrder> productOrderList = new ArrayList<>();
        res.setProductOrderList(productOrderList);
        /*用户id*/
        long userId = voFindProductOrderList.getUserId();
        /*订单状态*/
        int status = voFindProductOrderList.getStatus();
        /*搜索关键字*/
        String searchStr = voFindProductOrderList.getSearchStr();
        /*查询sql*/
        String querySql = "select t1.type,t1.status,t3.img_url as itemImgUrl,t4.img_url as imgUrl,t4.name,t4.title,t1.pay_money as payMoney,t1.order_number as orderNumber" +
                " from gfb_product_order t1 " +
                " left join gfb_product_order_buy_log t2 on t1.id = t2.product_order_id " +
                " left join gfb_product_item t3 on t2.product_item_id = t3.id" +
                " left join gfb_product t4 on t3.product_id = t4.id where t1.user_id =" + userId + " and t1.is_del = 0 ";
        if (!StringUtils.isEmpty(searchStr)) {
            querySql += "and ( t1.order_number like '%" + searchStr + "%' or t4.name like '%" + searchStr + "%') ";
        }
        //订单状态：0全部 1.待付款 2.待收货 3.已完成 4.已取消
        switch (status) {
            case 1:
                querySql += " and t1.status = 1";
                break;
            case 2:
                querySql += "and t1.status in (2,3,4)";
                break;
            case 3:
                querySql += "and t1.status = 5";
                break;
            case 4:
                querySql += "and t1.status = 6";
                break;
            default:
        }


        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(querySql);;
        if (!CollectionUtils.isEmpty(resultList)) {
            Set<String> orderNumberSet = new HashSet<>();
            resultList.stream().forEach(objMap -> {
                String imgUrl = StringHelper.toString(objMap.get("imgUrl"));
                String itemImgUrl = StringHelper.toString(objMap.get("itemImgUrl"));
                String orderNumber = StringHelper.toString(objMap.get("orderNumber"));

                ProductOrder productOrder = productOrderService.findByOrderNumber(orderNumber);

                if (!orderNumberSet.contains(orderNumber)) {
                    //填充订单商品
                    List<VoProductItem> productItems = new ArrayList<>();
                    VoProductItem productItem = new VoProductItem();
                    productItem.setImgUrl(ObjectUtils.isEmpty(itemImgUrl) ? imgUrl : itemImgUrl);
                    productItem.setProductName(StringHelper.toString(objMap.get("name")));
                    productItem.setProductTitle(StringHelper.toString(objMap.get("title")));
                    productItem.setSkuList(new ArrayList<>());
                    productItems.add(productItem);
                    //填充订单显示
                    VoProductOrder voProductOrder = new VoProductOrder();
                    voProductOrder.setType(NumberHelper.toInt(objMap.get("type")));
                    voProductOrder.setStatus(NumberHelper.toInt(objMap.get("status")));
                    voProductOrder.setPayMoney(StringHelper.formatDouble(NumberHelper.toDouble(objMap.get("payMoney")), 100, true));
                    voProductOrder.setNumber(productItems.size());
                    voProductOrder.setProductItemList(productItems);
                    voProductOrder.setOrderNumber(orderNumber);
                    productOrderList.add(voProductOrder);
                } else {
                    Map<String, VoProductOrder> voProductOrderMap = productOrderList.stream().collect(Collectors.toMap(VoProductOrder::getOrderNumber, Function.identity()));
                    VoProductOrder voProductOrder = voProductOrderMap.get(orderNumber);
                    VoProductItem productItem = new VoProductItem();
                    List<VoProductItem> productItems = voProductOrder.getProductItemList();
                    productItem.setImgUrl(ObjectUtils.isEmpty(itemImgUrl) ? imgUrl : itemImgUrl);
                    productItem.setProductName(StringHelper.toString(objMap.get("name")));
                    productItem.setProductTitle(StringHelper.toString(objMap.get("title")));
                    productItem.setSkuList(new ArrayList<>());
                    productItems.add(productItem);
                    voProductOrder.setNumber(productItems.size());
                }
            });
        }
        return ResponseEntity.ok(res);
    }

    /**
     * 订单详情页面
     */
    @Override
    public ResponseEntity<VoViewProductOrderDetailRes> findProductOrderDetail(VoFindProductOrderDetail
                                                                                      voFindProductOrderDetail) {
        VoViewProductOrderDetailRes res = VoBaseResp.ok("查询成功!", VoViewProductOrderDetailRes.class);
        /*订单编号*/
        String orderNumber = voFindProductOrderDetail.getOrderNumber();
        /*订单记录*/
        ProductOrder productOrder = productOrderService.findByOrderNumber(orderNumber);
        Preconditions.checkNotNull(productOrder, "订单记录不存在!");
        /*用户id*/
        long userId = voFindProductOrderDetail.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在!");
        //查询收货人信息
        Specification<ProductLogistics> pls = Specifications
                .<ProductLogistics>and()
                .eq("userId", userId)
                .eq("state", 0)
                .eq("orderNumber", orderNumber)
                .build();
        List<ProductLogistics> productLogisticsList = productLogisticsService.findList(pls);
        Preconditions.checkState(!CollectionUtils.isEmpty(productLogisticsList), "查询失败，收货人信息不存在!");
        ProductLogistics productLogistics = productLogisticsList.get(0);
        //查询订单商品记录
        Specification<ProductOrderBuyLog> pobls = Specifications
                .<ProductOrderBuyLog>and()
                .eq("productOrderId", productOrder.getId())
                .eq("isDel", false)
                .build();
        List<ProductOrderBuyLog> productOrderBuyLogList = productOrderBuyLogService.findList(pobls);
        Preconditions.checkState(!CollectionUtils.isEmpty(productOrderBuyLogList), "查询失败，订单商品不存在!");
        Set<Long> productItemIds = productOrderBuyLogList.stream().map(ProductOrderBuyLog::getProductItemId).collect(toSet());
         /*子商品记录*/
        Specification<ProductItem> pis = Specifications
                .<ProductItem>and()
                .in("id", productItemIds.toArray())
                .build();
        List<ProductItem> productItemList = productItemService.findList(pis);
        Preconditions.checkState(!CollectionUtils.isEmpty(productItemList), "查询失败，订单商品不存在!");
        Set<Long> productIds = productItemList.stream().map(ProductItem::getProductId).collect(toSet());
        /*商品记录*/
        Specification<Product> ps = Specifications
                .<Product>and()
                .in("id", productIds.toArray())
                .build();
        List<Product> productList = productService.findList(ps);
        Preconditions.checkState(!CollectionUtils.isEmpty(productList), "查询失败，订单商品不存在!");
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
        /*子商品与sku的关联*/
        List<VoProductItem> viewProductItemList = new ArrayList<>();
        productItemList.stream().forEach(productItem -> {
            VoProductItem voProductItem = new VoProductItem();
            List<VoSku> skuList = new ArrayList<>();
            /*子商品sku关联集合*/
            List<ProductItemSkuRef> productItemSkuRefs = productItemSkuRefMaps.get(productItem.getId());
            /*商品对象*/
            Product product = productMap.get(productItem.getProductId());
            productItemSkuRefs.stream().forEach(productItemSkuRef -> {
                ProductSku productSku = productSkuMap.get(productItemSkuRef.getSkuId());
                ProductSkuClassify productSkuClassify = productSkuClassifyMap.get(productSku.getScId());
                //sku 对象
                VoSku sku = new VoSku();
                sku.setName(productSku.getName());
                sku.setClassNo(String.valueOf(productSkuClassify.getNo()));
                sku.setClassName(productSkuClassify.getName());
                sku.setNo(String.valueOf(productSku.getNo()));
                skuList.add(sku);
            });
            voProductItem.setImgUrl(!ObjectUtils.isEmpty(productItem.getImgUrl()) ? product.getImgUrl() : productItem.getImgUrl());
            voProductItem.setProductName(product.getName());
            voProductItem.setProductTitle(product.getTitle());
            voProductItem.setSkuList(skuList);
            viewProductItemList.add(voProductItem);
        });

        res.setStatus(productOrder.getStatus());
        res.setOrderNumber(productOrder.getOrderNumber());
        res.setEarnings(StringHelper.formatDouble(productOrder.getEarnings(), 100, true));
        res.setPayNumber(productOrder.getPayNumber());
        res.setLogisticsAddress(getAddressStr(productLogistics));
        res.setBuyMoney(StringHelper.formatDouble(productOrder.getPlanMoney(), 100, true));
        res.setExpressName(productLogistics.getExpressName());
        res.setExpressNumber(productLogistics.getExpressNumber());
        res.setFee(StringHelper.formatDouble(productOrder.getFee(), 100, true));
        res.setPayAt(DateHelper.dateToString(productOrder.getPayAt()));
        res.setPayMoney(StringHelper.formatDouble(productOrder.getPayMoney(), 100, true));
        res.setLogisticsPhone(productLogistics.getPhone());
        res.setProductItemList(viewProductItemList);
        res.setLogisticsName(productLogistics.getName());
        res.setCreateAt(DateHelper.dateToString(productOrder.getCreatedAt()));
        return ResponseEntity.ok(res);
    }

    /**
     * 获取地址字符串
     *
     * @param productLogistics
     * @return
     */

    private String getAddressStr(ProductLogistics productLogistics) {
        StringBuffer addressStr = new StringBuffer();
        if (StringUtils.isEmpty(productLogistics.getCountry())) {
            addressStr.append(addressStr).append(" ");
        }
        if (StringUtils.isEmpty(productLogistics.getProvince())) {
            addressStr.append(productLogistics.getProvince()).append(" ");
        }
        if (StringUtils.isEmpty(productLogistics.getCity())) {
            addressStr.append(productLogistics.getCity()).append(" ");
        }
        if (StringUtils.isEmpty(productLogistics.getDistrict())) {
            addressStr.append(productLogistics.getDistrict()).append(" ");
        }
        if (StringUtils.isEmpty(productLogistics.getDetailedAddress())) {
            addressStr.append(productLogistics.getDetailedAddress());
        }
        return addressStr.toString();
    }

    /**
     * 付款接口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> orderPay(VoFindOrderPay voFindOrderPay) throws Exception {
        /*用户id*/
        long userId = voFindOrderPay.getUserId();
        /*用户资产记录*/
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "资产记录不存在!支付失败!");
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(asset, "用户记录不存在!支付失败!");
        /*订单编号*/
        String orderNumber = voFindOrderPay.getOrderNumber();
        /*订单记录*/
        ProductOrder productOrder = productOrderService.findByOrderNumberLock(orderNumber);
        Preconditions.checkNotNull(productOrder, "订单不存在!支付失败!");
        /*广富送计划*/
        ProductPlan productPlan = productPlanService.findById(productOrder.getPlanId());
        /*订单需要支付金额*/
        long payMoney = productOrder.getPayMoney();
        //验证订单状态
        //验证订单状态是否改变
        if (productOrder.getStatus().intValue() != 1) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "支付失败，订单状态已变更，请刷新后重试!", VoViewBuyProductPlanRes.class));
        }
        //验证用户
        //1.验证用户是否锁定
        if (users.getIsLock()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "您的账户已被冻结，详情请联系客服人员!", VoViewBuyProductPlanRes.class));
        }
        //2.判断用户金额是否充足
        if (asset.getUseMoney() < payMoney) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "支付失败，您的可用余额不足，请先充值!", VoViewBuyProductPlanRes.class));
        }
        if (userId != productOrder.getUserId().longValue()) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "订单不存在，请刷新重试!", VoViewBuyProductPlanRes.class));
        }

        /*支付方式 0在线支付*/
        int payType = voFindOrderPay.getPayType();

        /*交易流水号*/
        String seqNo = assetChangeProvider.getSeqNo();
        if (payType == 0) {
            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.buyProductFreeze);
            assetChange.setUserId(userId);
            assetChange.setMoney(payMoney);
            assetChange.setRemark(String.format("购买产品[%s],冻结可用资金%s元", productPlan.getName(), StringHelper.formatDouble(payMoney / 100D, true)));
            assetChange.setSourceId(productOrder.getId());
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            //更新订单状态
            Date nowDate = new Date();
            productOrder.setStatus(2);
            productOrder.setUpdatedAt(nowDate);
            productOrder.setPayAt(nowDate);
            productOrder.setPayMoney(payMoney);
            productOrder.setPayNumber(seqNo);
            productOrder.setPayType(payType);
        } else {
            //预留给其它支付方式
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "支付失败，目前仅支持在线支付!", VoViewBuyProductPlanRes.class));
        }
        return ResponseEntity.ok(VoBaseResp.ok("支付成功!"));
    }

    /**
     * 立即购买
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
        order.setPlanMoney(productPlan.getLowest());
        order.setFee(0L);
        order.setType(productPlan.getType());
        order.setIsDel(false);
        order.setOrderNumber(orderNumber);
        order.setPlanId(productPlan.getId());
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
        List<VoProductItemDetail> productItemDetailList = new ArrayList<>();
        voViewFindProductItemDetailsRes.setProductItemDetailList(productItemDetailList);
        /*商品id*/
        long productId = voFindProductDetail.getProductId();
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
                //装填子商品列表
                for (ProductItem productItem : productItemList) {
                    /*子商品与sku的关联*/
                    List<ProductItemSkuRef> productItemSkuRefs = productItemSkuRefsMap.get(productItem.getId());
                    List<VoSku> skuList = new ArrayList<>();
                    productItemSkuRefs.stream().forEach(productItemSkuRef -> {
                        ProductSku productSku = productSkuMap.get(productItemSkuRef.getSkuId());
                        ProductSkuClassify classify = productSkuClassifyMap.get(productSku.getScId());
                        VoSku voSku = new VoSku();
                        voSku.setName(productSku.getName());
                        voSku.setClassNo(String.valueOf(classify.getNo()));
                        voSku.setNo(String.valueOf(productSku.getNo()));
                        voSku.setClassName(classify.getName());
                        skuList.add(voSku);
                    });

                    VoProductItemDetail voProductItemDetail = new VoProductItemDetail();
                    voProductItemDetail.setName(product.getName());
                    voProductItemDetail.setImgUrl(productItem.getImgUrl());
                    voProductItemDetail.setTitle(product.getTitle());
                    voProductItemDetail.setDetails(ObjectUtils.isEmpty(productItem.getDetails()) ? product.getDetails() : productItem.getDetails());
                    voProductItemDetail.setDiscountPrice(StringHelper.formatDouble(productItem.getDiscountPrice(), 100, true));
                    voProductItemDetail.setPrice(StringHelper.formatDouble(productItem.getPrice(), 100, true));
                    voProductItemDetail.setInventory(String.valueOf(productItem.getInventory()));
                    voProductItemDetail.setSkuList(skuList);
                    voProductItemDetail.setProductItemId(productItem.getId());
                    voProductItemDetail.setQAndA(ObjectUtils.isEmpty(productItem.getQAndA()) ? product.getQAndA() : productItem.getQAndA());
                    voProductItemDetail.setAfterSalesService(ObjectUtils.isEmpty(productItem.getAfterSalesService()) ? product.getAfterSalesService() : productItem.getAfterSalesService());
                    productItemDetailList.add(voProductItemDetail);
                }
            }
        }

        return ResponseEntity.ok(voViewFindProductItemDetailsRes);
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
                            voProductPlan.setProductId(product.getId());
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
