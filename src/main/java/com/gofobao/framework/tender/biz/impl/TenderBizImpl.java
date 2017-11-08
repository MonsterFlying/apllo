package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.FrzFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndReq;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndResp;
import com.gofobao.framework.api.model.batch_credit_end.CreditEnd;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryItem;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.JixinTenderRecordHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.VoSaveThirdTender;
import com.gofobao.framework.tender.vo.request.*;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import com.gofobao.framework.wheel.borrow.biz.WheelBorrowBiz;
import com.gofobao.framework.windmill.borrow.biz.WindmillTenderBiz;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/5/31.
 */
@Service
@Slf4j
public class TenderBizImpl implements TenderBiz {

    @Autowired
    private UserService userService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    private WindmillTenderBiz windmillTenderBiz;
    @Autowired
    private LendService lendService;
    @Autowired
    private JixinTenderRecordHelper jixinTenderRecordHelper;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Value("${gofobao.javaDomain}")
    String javaDomain;
    private static Gson gson = new GsonBuilder().create();

    @Autowired
    private WheelBorrowBiz wheelBorrowBiz;

    /**
     * 新版投标
     *
     * @param voCreateTenderReq
     * @return
     * @throws Exception
     */
    @Override
    public ResponseEntity<VoBaseResp> createTender(VoCreateTenderReq voCreateTenderReq) throws Exception {
        Gson gson = new Gson();
        log.info(String.format("马上投资: 起步: %s", gson.toJson(voCreateTenderReq)));
        Borrow borrow = borrowService.findByIdLock(voCreateTenderReq.getBorrowId());
        Preconditions.checkNotNull(borrow, "投标: 标的信息为空!");
        if (!ObjectUtils.isEmpty(borrow.getLendId()) && borrow.getLendId() > 0) {
            Lend lend = lendService.findByIdLock(borrow.getLendId());
            // 对待有草出借,只能是出草人投
            if (voCreateTenderReq.getUserId().intValue() != lend.getUserId().intValue()) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "非常抱歉, 当前标的为有草出借标的, 只有出草人才能投!"));
            }
        }

        Users user = userService.findByIdLock(voCreateTenderReq.getUserId());
        Preconditions.checkNotNull(user, "投标: 用户信息为空!");

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voCreateTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "当前用户未开户！", VoBaseResp.class));
        }

        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoBaseResp.class));
        }

        Asset asset = assetService.findByUserIdLock(voCreateTenderReq.getUserId());
        Preconditions.checkNotNull(asset, "投标: 资金记录为空!");

        UserCache userCache = userCacheService.findByUserIdLock(voCreateTenderReq.getUserId());
        Preconditions.checkNotNull(userCache, "投标: 用户缓存信息为空!");

        Multiset<String> extendMessage = HashMultiset.create();
        boolean state = verifyBorrowInfo4Borrow(borrow, user, voCreateTenderReq, extendMessage);  // 标的判断
        if (!state) {
            Set<String> errorSet = extendMessage.elementSet();
            Iterator<String> iterator = errorSet.iterator();
            String msg =  iterator.next();
            log.error("标判断不通过"+msg);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        state = verifyUserInfo4Borrow(user, borrow, asset, voCreateTenderReq, extendMessage); // 借款用户资产判断
        Set<String> errorSet = extendMessage.elementSet();
        Iterator<String> iterator = errorSet.iterator();
        if (!state) {
            log.error("标的判断资产不通过");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, iterator.next()));
        }

        Date nowDate = new Date();
        long validateMoney = Long.parseLong(iterator.next());
        Tender borrowTender = createBorrowTenderRecord(voCreateTenderReq, user, nowDate, validateMoney);    // 生成投标记录
        borrowTender = registerJixinTenderRecord(borrow, borrowTender);  // 投标的存管报备
        if (ObjectUtils.isEmpty(borrowTender)) {
            log.error("标的报备失败");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "非常抱歉, 自动投标存管申报失败"));
        }

        // 扣除用户投标金额
        updateAssetByTender(borrow, borrowTender);
        borrow.setMoneyYes(borrow.getMoneyYes() + validateMoney);
        borrow.setTenderCount((borrow.getTenderCount() + 1));
        borrow.setId(borrow.getId());
        borrow.setUpdatedAt(nowDate);
        borrowService.save(borrow);  // 更改标的信息

        if (borrow.getMoneyYes() >= borrow.getMoney()) {   // 对于投标金额等于招标金额触发复审
            log.info("标的满标");
            //更新满标时间
            if (ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                borrow.setSuccessAt(nowDate);
                borrowService.save(borrow);
            }

            //复审
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
            mqConfig.setTag(MqTagEnum.AGAIN_VERIFY);
            mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 1));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            log.info(String.format("tenderBizImpl tender send mq %s", gson.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        }

        //如果当前用户是风车理财用户
        if (!StringUtils.isEmpty(user.getWindmillId())) {
            try {
                windmillTenderBiz.tenderNotify(borrowTender);
            } catch (Exception e) {
                log.error("推送风车理财异常", e);
            }
        }
        if (!StringUtils.isEmpty(user.getWheelId())) {
            wheelBorrowBiz.investNotice(borrowTender);
        }
        if (borrow.getIsWindmill()) {
            wheelBorrowBiz.borrowUpdateNotice(borrow);
        }
        try {
            // 触发新手标活动派发
            if (borrow.getIsNovice() && userCache.isNovice() && (!borrow.getIsFinance())) {
                MarketingData marketingData = new MarketingData();
                marketingData.setTransTime(DateHelper.dateToString(new Date()));
                marketingData.setUserId(borrowTender.getUserId().toString());
                marketingData.setSourceId(borrowTender.getId().toString());
                marketingData.setMarketingType(MarketingTypeContants.TENDER);
                try {
                    String json = gson.toJson(marketingData);
                    Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setMsg(data);
                    mqConfig.setTag(MqTagEnum.MARKETING_TENDER);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("投资营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("投资营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        } catch (Exception e) {
            log.error("触发派发失败新手红包失败", e);
        }
        return ResponseEntity.ok(VoBaseResp.ok("投资成功"));
    }

    /**
     * 用户投标冻结
     *
     * @param borrow
     * @param borrowTender
     * @throws Exception
     */
    private void updateAssetByTender(Borrow borrow, Tender borrowTender) throws Exception {
        AssetChange assetChange = new AssetChange();
        assetChange.setForUserId(borrowTender.getUserId());
        assetChange.setUserId(borrowTender.getUserId());
        assetChange.setType(AssetChangeTypeEnum.freeze);
        assetChange.setRemark(String.format("成功投资标的[%s]冻结资金%s元", borrow.getName(), StringHelper.formatDouble(borrowTender.getValidMoney() / 100D, true)));
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setMoney(borrowTender.getValidMoney());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSourceId(borrowTender.getId());
        assetChangeProvider.commonAssetChange(assetChange);
    }

    /**
     * 登记即信标的
     *
     * @param borrow
     * @param borrowTender
     * @return
     * @throws Exception
     */
    private Tender registerJixinTenderRecord(Borrow borrow, Tender borrowTender) throws Exception {
        if (!borrow.isTransfer()) {
            String txAmount = StringHelper.formatDouble(borrowTender.getValidMoney(), 100, false);
            /* 投标orderId */
            String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_PREFIX);

            log.info(String.format("马上投资: 投资报备: %s", new Gson().toJson(borrowTender)));
            VoCreateThirdTenderReq voCreateThirdTenderReq = new VoCreateThirdTenderReq();
            voCreateThirdTenderReq.setAcqRes(String.valueOf(borrowTender.getId()));
            voCreateThirdTenderReq.setUserId(borrowTender.getUserId());
            voCreateThirdTenderReq.setTxAmount(txAmount);
            voCreateThirdTenderReq.setProductId(borrow.getProductId());
            voCreateThirdTenderReq.setOrderId(orderId);
            voCreateThirdTenderReq.setFrzFlag(FrzFlagContant.FREEZE);
            voCreateThirdTenderReq.setAcqRes(borrowTender.getId() + "");
            ResponseEntity<VoBaseResp> resp = tenderThirdBiz.createThirdTender(voCreateThirdTenderReq);
            if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) {
                log.info("马上投资: 投资报备失败");
                return null;
            } else { // 保存即信投标申请到redis
                log.info("马上投资: 投资报备成功");
                UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(borrowTender.getUserId());
                VoSaveThirdTender voSaveThirdTender = new VoSaveThirdTender();
                voSaveThirdTender.setAccountId(userThirdAccount.getAccountId());
                voSaveThirdTender.setTxAmount(txAmount);
                voSaveThirdTender.setProductId(borrow.getProductId()); // productId
                voSaveThirdTender.setOrderId(orderId);
                voSaveThirdTender.setIsAuto(borrowTender.getIsAuto());
                jixinTenderRecordHelper.saveJixinTenderInRedis(voSaveThirdTender);
            }
        }

        borrowTender = tenderService.findById(borrowTender.getId());
        borrowTender.setIsThirdRegister(true);
        borrowTender.setUpdatedAt(new Date());
        return tenderService.save(borrowTender);
    }


    /**
     * 生成投标记录
     *
     * @param voCreateTenderReq
     * @param user
     * @param nowDate
     * @param validateMoney
     * @return
     */
    private Tender createBorrowTenderRecord(VoCreateTenderReq voCreateTenderReq, Users user, Date nowDate, long validateMoney) {
        Tender borrowTender = new Tender();
        borrowTender.setUserId(user.getId());
        borrowTender.setType(0);
        borrowTender.setBorrowId(voCreateTenderReq.getBorrowId());
        borrowTender.setStatus(1);
        borrowTender.setMoney(voCreateTenderReq.getTenderMoney().longValue());
        borrowTender.setValidMoney(validateMoney);
        Integer requestSource = 0;
        try {
            requestSource = Integer.valueOf(voCreateTenderReq.getRequestSource());
        } catch (Exception e) {
            requestSource = 0;
        }

        borrowTender.setSource(requestSource);
        Integer autoOrder = voCreateTenderReq.getAutoOrder();
        borrowTender.setAutoOrder(ObjectUtils.isEmpty(autoOrder) ? 0 : autoOrder);
        borrowTender.setIsAuto(voCreateTenderReq.getIsAutoTender());
        borrowTender.setUpdatedAt(nowDate);
        borrowTender.setCreatedAt(nowDate);
        borrowTender.setTransferFlag(0);
        borrowTender.setState(1);
        borrowTender = tenderService.save(borrowTender);
        return borrowTender;
    }


    /**
     * 借款用户审核检查
     * <p>
     * 主要做一下教研:
     * 1. 用户是否锁定
     * 2.投标是否满足最小投标原则
     * 3.有效金额是否大于自动投标设定的最大投标金额
     * 4.存管金额匹配
     * 4.账户有效金额匹配
     *
     * @param user
     * @param borrow
     * @param asset
     * @param voCreateTenderReq
     * @param extendMessage     @return
     */
    private boolean verifyUserInfo4Borrow(Users user, Borrow borrow, Asset asset, VoCreateTenderReq voCreateTenderReq, Multiset<String> extendMessage) {
        // 判断用户是否已经锁定
        if (user.getIsLock()) {
            extendMessage.add("当前用户属于锁定状态, 如有问题请联系客服!");
            log.error("当前用户属于锁定状态, 如有问题请联系客服!");
            return false;
        }

        // 判断最小投标金额
        long realTenderMoney = borrow.getMoney() - borrow.getMoneyYes();  // 剩余金额
        int minLimitTenderMoney = ObjectUtils.isEmpty(borrow.getLowest()) ? 50 * 100 : borrow.getLowest();  // 最小投标金额
        long realMiniTenderMoney = Math.min(realTenderMoney, minLimitTenderMoney);  // 获取最小投标金额
        if (realMiniTenderMoney > voCreateTenderReq.getTenderMoney()) {
            extendMessage.add("小于标的最小投标金额!");
            log.error("小于标的最小投标金额!");
            return false;
        }

        // 真实有效投标金额
        long invaildataMoney = Math.min(realTenderMoney, voCreateTenderReq.getTenderMoney().intValue());
        if (voCreateTenderReq.getIsAutoTender()) {
            // 对于设置最大自动投标金额进行判断
            if (borrow.getMostAuto() > 0) {
                invaildataMoney = Math.min(borrow.getMostAuto() - borrow.getMoneyYes(), invaildataMoney);
            }

            if ((invaildataMoney <= 0) || (invaildataMoney < minLimitTenderMoney)) {
                extendMessage.add("该借款已达到自投限额!");
                log.error("该借款已达到自投限额!");
                return false;
            }
        }

        if (invaildataMoney > asset.getUseMoney()) {
            extendMessage.add("您的账户可用余额不足,请先充值!");
            log.error("您的账户可用余额不足,请先充值!");
            return false;
        }

        // 查询存管系统资金
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            extendMessage.add("当前网络不稳定,请稍后重试!");
            log.error("当前网络不稳定,请稍后重试!");
            return false;
        }


        String availBal1 = balanceQueryResponse.getAvailBal();
        long availBal = MoneyHelper.yuanToFen(NumberHelper.toDouble(availBal1));
        long useMoney = asset.getUseMoney().longValue();
        if (availBal < useMoney) {
            log.error(String.format("资金账户未同步userId:%s:本地:%s 即信:%s", user.getId(), useMoney, availBal));
            extendMessage.add("资金账户未同步，请先在个人中心进行资金同步操作!");
            return false;
        }

        extendMessage.add(String.valueOf(invaildataMoney));
        return true;
    }


    /**
     * 验证标的信息是否符合投标要求
     * 主要从:
     * 1.标的状态
     * 2.招标开始时间
     * 3.标的结束时间
     * 4.是否频繁投标
     * 5.投标密码判断
     * 6.债转转让是否与投标同一个人
     * 注意当标的已经过了招标时间, 会进行取消借款操作
     *
     * @param borrow
     * @param user
     * @param voCreateTenderReq
     * @param errerMessage      @return
     */
    private boolean verifyBorrowInfo4Borrow(Borrow borrow, Users user, VoCreateTenderReq voCreateTenderReq, Multiset<String> errerMessage) throws Exception {
        if (!(borrow.getStatus() == 1 && borrow.getMoneyYes() < borrow.getMoney())) {
            errerMessage.add("标的未在招标状态， 如有疑问请联系客服!");
            return false;
        }

        Date nowDate = new Date();
        Date releaseAt = borrow.getReleaseAt();
        boolean isAutoTender = voCreateTenderReq.getIsAutoTender();

        if (borrow.getIsNovice()) {  // 新手
            releaseAt = DateHelper.max(DateHelper.addHours(DateHelper.beginOfDate(releaseAt), 20), borrow.getReleaseAt());
        }

        //判断是否是理财计划专用借款
        long financeTenderUserId = 22002L;
        if (borrow.getIsFinance()) {
            //判断投标用户是否是22002
            if (user.getId().longValue() != financeTenderUserId) {
                errerMessage.add("此标的暂时无法投递!");
                return false;
            }
        }

        UserCache userCache = userCacheService.findById(user.getId());
        if (ObjectUtils.isEmpty(borrow.getLendId()) && releaseAt.getTime() > nowDate.getTime() && !userCache.isNovice()) {
            log.info(String.valueOf(ObjectUtils.isEmpty(borrow.getLendId())));
            log.info(String.valueOf(releaseAt.getTime() > nowDate.getTime()));
            log.info(String.valueOf(!userCache.isNovice()));
            if (borrow.getIsNovice()) {
                errerMessage.add("老用户可在20:00点后投新手标!");
            } else {
                errerMessage.add("当前标的未到发布时间");
            }

            return false;
        }

        Date endDate = DateHelper.addDays(DateHelper.beginOfDate(releaseAt), borrow.getValidDay() + 1);
        if (endDate.getTime() < nowDate.getTime()) {
            // 流标
            log.info("==========================================");
            log.info(String.format("标的流标操作: %s", gson.toJson(borrow)));
            log.info("==========================================");
            VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
            voCancelBorrow.setBorrowId(borrow.getId());
            voCancelBorrow.setUserId(borrow.getUserId());
            ResponseEntity<VoBaseResp> voBaseRespResponseEntity = borrowBiz.cancelBorrow(voCancelBorrow);
            if (voBaseRespResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                errerMessage.add("当前标的已经超过招标时间");
            } else {
                errerMessage.add(voBaseRespResponseEntity.getBody().getState().getMsg());
            }
            return false;
        }

        // 判断投标频繁
        if (tenderService.checkTenderNimiety(borrow.getId(), user.getId())) {
            errerMessage.add("投标间隔不能小于一分钟!");
            return false;
        }

        // 投标密码判断
        if (!StringUtils.isEmpty(borrow.getPassword())) {
            if (StringUtils.isEmpty(voCreateTenderReq.getBorrowPassword())) {
                errerMessage.add("投标密码不能为空!");
                return false;
            }

            if (!PasswordHelper.verifyPassword(borrow.getPassword(), voCreateTenderReq.getBorrowPassword())) {
                errerMessage.add("借款密码验证失败, 请重新输入!");
                return false;
            }
        }

        // 借款自投判断
        if (borrow.getUserId().equals(user.getId())) {
            errerMessage.add("不能投自己发布借款!");
            return false;
        }

        // 债转自投判断
        if (borrow.isTransfer()) {
            Tender tender = tenderService.findById(borrow.getTenderId());
            Borrow tempBorrow = borrowService.findById(tender.getBorrowId());
            if (user.getId().equals(tempBorrow.getUserId())) {
                errerMessage.add("不能投自己发布或转让的借款!");
                return false;
            }
        }

        if (!isAutoTender) {
            if (!userCache.isNovice() && borrow.getIsLock()) {
                log.info("borrowId -> %s,isLock -> %s,isNovice -> %s", borrow.getId(), borrow.getIsLock(), !userCache.isNovice());
                errerMessage.add("当前标的状态已锁定,请稍后再试吧");
                return false;
            }
        }
        return true;
    }

    /**
     * 投标
     *
     * @param voCreateTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<VoBaseResp> tender(VoCreateTenderReq voCreateTenderReq) throws Exception {
        //投标撤回集合
        String borrowId = String.valueOf(voCreateTenderReq.getBorrowId());
        try {
            ResponseEntity<VoBaseResp> voBaseRespResponseEntity = createTender(voCreateTenderReq);
            if (voBaseRespResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
            } else {
                return voBaseRespResponseEntity;
            }
        } catch (Exception e) {
            //投标撤回
            jixinTenderRecordHelper.cancelJixinTenderByRedisRecord(borrowId, false);
            throw new Exception(e);
        } finally {
            //从redis删除投标申请记录
            jixinTenderRecordHelper.removeJixinTenderRecordInRedis(borrowId, false);
        }
    }

    /**
     * 投标用户
     *
     * @param tenderUserReq
     * @param tenderUserReq
     * @return
     */
    @Override
    public ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser(TenderUserReq tenderUserReq) {
        try {
            List<VoBorrowTenderUserRes> tenderUserRes = tenderService.findBorrowTenderUser(tenderUserReq);
            VoBorrowTenderUserWarpListRes warpListRes = VoBaseResp.ok("查询成功", VoBorrowTenderUserWarpListRes.class);
            warpListRes.setVoBorrowTenderUser(tenderUserRes);
            return ResponseEntity.ok(warpListRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoBorrowTenderUserWarpListRes.class));
        }
    }

    @Override
    public ResponseEntity<VoBaseResp> adminCancelTender(VoAdminCancelTender voAdminCancelTender) {
        log.error("请求用户标的信息");
        String paramStr = voAdminCancelTender.getParamStr();
        if (!SecurityHelper.checkSign(voAdminCancelTender.getSign(), paramStr)) {
            log.error("BorrowBizImpl doAgainVerify error：自动车标不成功");
        }

        Map<String, String> paramMap = gson.fromJson(paramStr, new TypeToken<Map<String, String>>() {
        }.getType());


        String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_CANCEL_PREFIX);
        String accountId = paramMap.get("accountId");
        String txAmount = paramMap.get("txAmount");
        String orgOrderId = paramMap.get("orgOrderId");
        String productId = paramMap.get("productId");
        log.info("标的撤销:" + orgOrderId);
        BidCancelReq request = new BidCancelReq();
        request.setAccountId(accountId);
        request.setTxAmount(txAmount);
        request.setChannel(ChannelContant.HTML);
        request.setOrderId(orderId);
        request.setOrgOrderId(orgOrderId);
        request.setProductId(productId);
        request.setAcqRes(accountId);
        BidCancelResp response = jixinManager.send(JixinTxCodeEnum.BID_CANCEL, request, BidCancelResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            log.error("标的初审" + new Gson().toJson(response));
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        return ResponseEntity.badRequest().body(VoBaseResp.ok("撤销成功"));
    }

    /**
     * pc结束第三方债权接口
     *
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> pcEndThirdTender(VoPcEndThirdTender voPcEndThirdTender) throws Exception {
        String paramStr = voPcEndThirdTender.getParamStr();
        if (!SecurityHelper.checkSign(voPcEndThirdTender.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结束第三方债权 签名验证不通过!"));
        }
        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /*用户id*/
        long userId = NumberHelper.toLong(paramMap.get("userId"));
        return endThirdTender(userId);

    }

    /**
     * @param userId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<VoBaseResp> endThirdTender(long userId) throws Exception {
        //1.用户本地前置校验（例如是否锁定什么的）
        /*用户对象*/
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "结束第三方债权 用户记录不存在!");
        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已锁定，请先解锁再做此项操作!"));
        }
        //2.校验用户本地资金户
        /*用户资金对象*/
        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "结束第三方债权 用户资金记录不存在!");
        //3.校验用户即信资金账户
        /*用户存管记录*/
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        // 查询存管系统资金
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("当前网络不稳定,请稍后重试! %s", balanceQueryResponse.getRetMsg())));
        }

        //可用余额
        long availBal = MoneyHelper.yuanToFen(balanceQueryResponse.getAvailBal());
        //账面余额
        long currBal = MoneyHelper.yuanToFen(balanceQueryResponse.getCurrBal());
        //本地可用金额
        long useMoney = asset.getUseMoney().longValue();
        //本地剩余冻结
        long noUseMoney = asset.getNoUseMoney().longValue();
        //理财计划金额
        long financePlanMoney = asset.getFinancePlanMoney();
        if ((availBal + currBal + useMoney + noUseMoney + financePlanMoney) > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "资金账户还存在余额，请清0后重试!"));
        }
        //待还金额
        long payment = asset.getPayment().longValue();
        //待回金额
        long collection = asset.getCollection();
        if ((payment + collection) > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "资金账户还存在待还/待收金额，请清0后重试!"));
        }
        //4.校验用户本地债权
        //判断是否有未回未转让债权
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("status", 0)
                .eq("transferFlag", 0)
                .eq("userId", userId)
                .build();
        long count = borrowCollectionService.count(bcs);
        if (count > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户还存在未回款债权，暂无法结束债权!"));
        }
        //判断是否有未还债权
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("status", 0)
                .eq("userId", userId)
                .build();
        count = borrowRepaymentService.count(brs);
        if (count > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户还存在未还款债权，暂无法结束债权!"));
        }
        //5.通过即信的债权反查本地债权，然后统一结束
        int max = 20, index = 1, totalItems = 0;
        /*结束债权集合*/
        List<CreditEnd> creditEndList = new ArrayList<>();

        CreditDetailsQueryRequest creditDetailsQueryRequest = new CreditDetailsQueryRequest();
        creditDetailsQueryRequest.setAccountId(String.valueOf(userThirdAccount.getAccountId()));
        creditDetailsQueryRequest.setStartDate("20170801");
        creditDetailsQueryRequest.setEndDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        creditDetailsQueryRequest.setState("0");
        do {
            creditDetailsQueryRequest.setPageNum(String.valueOf(index));
            creditDetailsQueryRequest.setPageSize(String.valueOf(max));
            CreditDetailsQueryResponse creditDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CREDIT_DETAILS_QUERY,
                    creditDetailsQueryRequest,
                    CreditDetailsQueryResponse.class);
            if ((ObjectUtils.isEmpty(creditDetailsQueryResponse)) || !creditDetailsQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("当前网络不稳定,请稍后重试! %s", creditDetailsQueryResponse.getRetMsg())));
            }

            /*查询得到的总记录数量*/
            totalItems = NumberHelper.toInt(creditDetailsQueryResponse.getTotalItems());
            if (totalItems == 0) {
                break;
            }
            /*查询债权的结果 1-投标中 2-计息中 4-本息已返回 9-已撤销 不需要关注 :8-审核中*/
            List<CreditDetailsQueryItem> queryItems = gson.fromJson(creditDetailsQueryResponse.getSubPacks(), new TypeToken<List<CreditDetailsQueryItem>>() {
            }.getType());
            /*投标记录集合*/
            List<Tender> tenderList = new ArrayList<>();
            for (CreditDetailsQueryItem queryItem : queryItems) {
                /*债权状态 1-投标中 2-计息中 4-本息已返回 9-已撤销 不需要关注 :8-审核中*/
                String queryItemState = queryItem.getState();
                //存在投标中债权
                if ("1".equals(queryItemState)) {
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("即信存在投标中的债权，请确认后重试! productId->%s ,orderId->%s",
                                    queryItem.getProductId(), queryItem.getOrderId())));
                }
                /*借款id*/
                String productId = queryItem.getProductId();
                /*债权订单id*/
                String orderId = queryItem.getOrderId();
                //2.计息中是考虑债权迁移
                if (ImmutableSet.of("2", "4").contains(queryItemState)) {
                    Specification<Tender> ts = Specifications
                            .<Tender>and()
                            .eq("thirdTenderOrderId", orderId)
                            .eq("borrowId", productId)
                            .eq("userId", userId)
                            .build();
                    List<Tender> aloneTenderList = tenderService.findList(ts);
                    tenderList.addAll(aloneTenderList);
                }
            }
            //结束债权条件
            /*借款id集合*/
            Set<Long> borrowIds = tenderList.stream().map(Tender::getBorrowId).collect(Collectors.toSet());
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIds.toArray())
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);
            Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
            /*借款人用户id集合*/
            Set<Long> borrowUserIds = borrowList.stream().map(Borrow::getUserId).collect(Collectors.toSet());
            Specification<UserThirdAccount> utas = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", borrowUserIds.toArray())
                    .build();
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(utas);
            Map<Long, UserThirdAccount> userThirdAccountMap = userThirdAccountList.stream().collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));
            //1.已经转让出去
            //2.未转出去的则查询当前的借款是否全部结清
            for (Tender tender : tenderList) {
                //已全部转让债权
                if (tender.getTransferFlag().intValue() == 2) {

                } else if (tender.getStatus().intValue() == 1 && tender.getTransferFlag().intValue() == 0) {
                    //未转让债权再次单独查询
                    brs = Specifications
                            .<BorrowRepayment>and()
                            .eq("borrowId", tender.getBorrowId())
                            .eq("status", 0)
                            .build();
                    count = borrowRepaymentService.count(brs);
                    if (count > 0) {
                        return ResponseEntity
                                .badRequest()
                                .body(VoBaseResp.error(VoBaseResp.ERROR, String.format("查询到未还款债权记录! borrowId -> %s", tender.getBorrowId())));
                    }
                } else {
                    continue;
                }
                /*借款记录*/
                Borrow borrow = borrowMap.get(tender.getBorrowId());
                /*借款用户存管记录*/
                UserThirdAccount borrowUserThirdAccount = userThirdAccountMap.get(borrow.getUserId());
                /* 结束债权orderId */
                String orderId = JixinHelper.getOrderId(JixinHelper.END_CREDIT_PREFIX);
                /* 结束债权 */
                CreditEnd creditEnd = new CreditEnd();
                creditEnd.setAccountId(borrowUserThirdAccount.getAccountId());
                creditEnd.setOrderId(orderId);
                creditEnd.setAuthCode(tender.getAuthCode());
                creditEnd.setForAccountId(userThirdAccount.getAccountId());
                creditEnd.setProductId(borrow.getProductId());
                creditEndList.add(creditEnd);

                //保存orderId
                tender.setThirdCreditEndOrderId(orderId);
            }
            tenderService.save(tenderList);

            //查询页码叠加
            index++;
        } while (totalItems >= max);

        //判断结束债权不为空
        if (CollectionUtils.isEmpty(creditEndList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结束债权集合为空!"));
        }

        //发送批次结束债权
        Date nowDate = new Date();

        //批次号
        String batchNo = JixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("userId", userId);

        BatchCreditEndReq request = new BatchCreditEndReq();
        request.setBatchNo(batchNo);
        request.setTxCounts(String.valueOf(creditEndList.size()));
        request.setNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditend/check");
        request.setRetNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditend/run");
        request.setAcqRes(gson.toJson(acqResMap));
        request.setSubPacks(gson.toJson(creditEndList));

        BatchCreditEndResp creditEndResp = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_END, request, BatchCreditEndResp.class);
        if ((ObjectUtils.isEmpty(creditEndResp)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(creditEndResp.getReceived()))) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(0, 100, false));
            batchCancelReq.setTxCounts(StringHelper.toString(creditEndList.size()));
            batchCancelReq.setChannel(ChannelContant.HTML);
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("即信批次撤销失败!");
            }
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setTxDate(request.getTxDate());
        thirdBatchLog.setTxTime(request.getTxTime());
        thirdBatchLog.setSeqNo(request.getSeqNo());
        thirdBatchLog.setSourceId(userId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_CREDIT_END);
        thirdBatchLog.setAcqRes(gson.toJson(acqResMap));
        thirdBatchLog.setRemark("即信批次结束债权");
        thirdBatchLogService.save(thirdBatchLog);
        return ResponseEntity.ok(VoBaseResp.ok("结束申请成功!"));
    }
}
