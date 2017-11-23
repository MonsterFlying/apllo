package com.gofobao.framework.product.biz.biz;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.product.biz.ProductOrderBiz;
import com.gofobao.framework.product.entity.*;
import com.gofobao.framework.product.service.*;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Created by Zeke on 2017/11/22.
 */
@Service
public class ProductOrderBizImpl implements ProductOrderBiz {

    @Autowired
    private ProductLogisticsService productLogisticsService;
    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductOrderService productOrderService;
    @Autowired
    private ProductPlanService productPlanService;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductOrderBuyLogService productOrderBuyLogService;
    @Autowired
    private ProductSkuService productSkuService;
    @Autowired
    private ProductItemSkuRefService productItemSkuRefService;
    @Autowired
    private ProductSkuClassifyService productSkuClassifyService;

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

    /**
     * 删除订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> delOrder(VoDelOrder voDelOrder) throws Exception {
         /*用户id*/
        long userId = voDelOrder.getUserId();
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "资产记录不存在!");
        /*订单id*/
        String orderNumber = voDelOrder.getOrderNumber();
        ProductOrder productOrder = productOrderService.findByOrderNumberLock(orderNumber);
        Preconditions.checkNotNull(productOrder, "订单记录不存在!");
        //验证订单是否可以删除
        int orderStatus = productOrder.getStatus();
        Set<Integer> passStatusSet = ImmutableSet.of(5, 6);
        if (!passStatusSet.contains(orderStatus)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "请先取消订单!"));
        }
        //取消订单
        productOrder.setIsDel(true);
        productOrder.setUpdatedAt(new Date());
        productOrderService.save(productOrder);
        return ResponseEntity.ok(VoBaseResp.ok("删除成功!"));
    }

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
        String querySql = "select distinct t1.type,t1.status,t3.img_url as itemImgUrl,t4.img_url as imgUrl,t4.name,t4.title,t1.pay_money as payMoney,t1.order_number as orderNumber" +
                " from gfb_product_order t1 " +
                " inner join gfb_product_order_buy_log t2 on t1.id = t2.product_order_id " +
                " inner join gfb_product_item t3 on t2.product_item_id = t3.id" +
                " inner join gfb_product t4 on t3.product_id = t4.id where t1.user_id =" + userId + " and t1.is_del = 0 ";
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

        //分页
        Integer pageIndex = voFindProductOrderList.getPageIndex();
        Integer pageSize = voFindProductOrderList.getPageSize();
        querySql += " limit " + pageIndex * pageSize + "," + pageSize;

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(querySql);
        ;
        if (!CollectionUtils.isEmpty(resultList)) {
            Set<String> orderNumberSet = new HashSet<>();
            resultList.stream().forEach(objMap -> {
                String imgUrl = StringHelper.toString(objMap.get("imgUrl"));
                String itemImgUrl = StringHelper.toString(objMap.get("itemImgUrl"));
                String orderNumber = StringHelper.toString(objMap.get("orderNumber"));
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
}
