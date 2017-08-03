package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.JixinContants;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.entity.*;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoBuyTransfer;
import com.gofobao.framework.tender.vo.request.VoPcFirstVerityTransfer;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.*;
import com.gofobao.framework.tender.vo.response.web.TransferBuy;
import com.gofobao.framework.tender.vo.response.web.VoViewTransferBuyWarpRes;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Created by admin on 2017/6/12.
 */
@Slf4j
@Service
public class TransferBizImpl implements TransferBiz {

    @Autowired
    private TransferService transferService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private StatisticBiz statisticBiz;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private IncrStatisticBiz incrStatisticBiz;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictItemService dictItemService;
    @Autowired
    private DictValueService dictValueService;

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueService.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });

    @Value("${gofobao.imageDomain}")
    private String imageDomain;

    final Gson GSON = new GsonBuilder().create();

    public static final String MSG = "msg";
    public static final String LEFT_MONEY = "leftMoney";


    /**
     * 债权转让复审
     *
     * @param transferId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> againVerifyTransfer(long transferId, String batchNo) throws Exception {
        Date nowDate = new Date();
        /*
        1.查询债权转让记录与购买记录
        2并且判断债权转让购买记录是否成功购买（在存管登记），
        3.生成tender记录，将购买债权转让信息对应加入tender
        4.生成新的borrowCollection记录,并添加新的待收
        5.将资金扣减

         */
        Transfer transfer = transferService.findByIdLock(transferId);/* 债权转让记录 */
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        Tender parentTender = tenderService.findById(transfer.getTenderId());/* 债权转让原投资记录 */
        Preconditions.checkNotNull(parentTender, "债权转让源投资记录不存在!tenderId:" + transfer.getTenderId());
        Borrow parentBorrow = borrowService.findById(parentTender.getBorrowId());
        Preconditions.checkNotNull(parentBorrow, "原始借款记录不存在!");
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("transferId", transfer.getId())
                .eq("state", 0)
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);/* 购买债权转让记录 */
        Preconditions.checkNotNull(transferBuyLogList, "购买债权转让记录不存在!");
        long failure = transferBuyLogList.stream().filter(transferBuyLog -> BooleanHelper.isFalse(transferBuyLog.getThirdTransferFlag())).count();/* 登记即信存管失败条数 */
        if (failure > 0) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "存在未登记即信存管的购买" + failure + "失败记录"));
        }

        //新增子级投标记录
        List<Tender> childTenderList = addChildTender(nowDate, transfer, parentTender, transferBuyLogList);
        //生成子级债权回款记录，标注老债权回款已经转出
        addChildTenderCollection(nowDate, transfer, parentBorrow, childTenderList);
        //发放债权转让资金
        batchAssetChange(transferId, batchNo);
        // 发送投资成功站内信
        sendNoticsByBuyTransfer(transfer, childTenderList);
        // 用户投标信息和每日统计
        userTenderStatistic(childTenderList);
        // 复审事件
        //如果是流转标则扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
        updateUserCacheByTransferReview(parentBorrow, transfer);
        //更新全网网站统计
        updateStatisticByTransferReview(transfer);

        return ResponseEntity.ok(VoBaseResp.ok("债权转让复审成功!"));
    }

    /**
     * 发放债权转让资金
     *
     * @param transferId
     * @param batchNo
     */
    private void batchAssetChange(long transferId, String batchNo) {
        Specification<BatchAssetChange> bacs = Specifications
                .<BatchAssetChange>and()
                .eq("sourceId", transferId)
                .eq("type", BatchAssetChangeContants.BATCH_CREDIT_INVEST)
                .eq("batchNo", batchNo)
                .build();
        List<BatchAssetChange> batchAssetChangeList = batchAssetChangeService.findList(bacs);
        Preconditions.checkNotNull(batchAssetChangeList, batchNo + "债权转让资金变动记录不存在!");
        BatchAssetChange batchAssetChange = batchAssetChangeList.get(0);/* 债权转让资金变动记录 */

        Specification<BatchAssetChangeItem> bacis = Specifications
                .<BatchAssetChangeItem>and()
                .eq("batchAssetChangeId", batchAssetChange.getId())
                .eq("state", 0)
                .build();
        List<BatchAssetChangeItem> batchAssetChangeItemList = batchAssetChangeItemService.findList(bacis);
        Preconditions.checkNotNull(batchAssetChangeItemList, batchNo + "债权转让资金变动子记录不存在!");
        batchAssetChangeItemList.stream().forEach(batchAssetChangeItem -> {
            //发送存管红包
            if (BooleanHelper.isTrue(batchAssetChangeItem.getSendRedPacket())) {
                UserThirdAccount transferUserThirdAccount = userThirdAccountService.findByUserId(batchAssetChangeItem.getUserId()); /* 债权转让人存管账号 */
                //通过红包账户发放
                //调用即信发放债权转让人应收利息
                //查询红包账户
                DictValue dictValue = null;
                try {
                    dictValue = jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                } catch (ExecutionException e) {
                    log.error("transferBizImpl batchAssetChange 获取存管红包账户失败：", e);
                }
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                voucherPayRequest.setTxAmount(StringHelper.toString(batchAssetChangeItem.getMoney()));//扣除手续费
                voucherPayRequest.setForAccountId(transferUserThirdAccount.getAccountId());
                voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                voucherPayRequest.setDesLine(batchAssetChangeItem.getRemark());
                voucherPayRequest.setChannel(ChannelContant.HTML);
                VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                    log.error("BorrowRepaymentThirdBizImpl 调用即信发送发放债权转让人应收利息异常:" + msg);
                }
            }
            //扣减本地资金
            CapitalChangeEntity capitalChangeEntity = GSON.fromJson(GSON.toJson(batchAssetChangeItem), new TypeToken<CapitalChangeEntity>() {
            }.getType());
            try {
                capitalChangeHelper.capitalChange(capitalChangeEntity);
            } catch (Exception e) {
                log.error("transferBizImpl batchAssetChange assetChange error:", e);
            }
        });
    }

    /**
     * 发送投资成功站内信
     *
     * @param transfer
     * @param tenderList
     */
    private void sendNoticsByBuyTransfer(Transfer transfer, List<Tender> tenderList) {
        Gson gson = new Gson();
        log.info(String.format("发送投标成功站内信开始: %s", gson.toJson(tenderList)));
        Date nowDate = new Date();
        Set<Long> userIdSet = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());
        for (Long userId : userIdSet) {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userId);
            notices.setRead(false);
            notices.setName("投资的借款满标审核通过");
            notices.setContent("您所投资的债权转让借款[" + transfer.getTitle() + " 已满标审核通过");
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            //发送站内信
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("transferBizImpl sendNoticsByTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("transferBizImpl sendNoticsByTender send mq exception", e);
            }
        }
        log.info(String.format("发送投标成功站内信结束:  %s", gson.toJson(tenderList)));
    }

    /**
     * 用户投标统计
     *
     * @param tenderList
     */
    private void userTenderStatistic(List<Tender> tenderList) throws Exception {
        Gson gson = new Gson();
        for (Tender tender : tenderList) {
            log.info(String.format("投标统计: %s", gson.toJson(tender)));

            UserCache userCache = userCacheService.findById(tender.getUserId());

            IncrStatistic incrStatistic = new IncrStatistic();
            if ((!userCache.getTenderTransfer()) && (!userCache.getTenderTuijian()) && (!userCache.getTenderJingzhi()) && (!userCache.getTenderMiao()) && (!userCache.getTenderQudao())) {
                incrStatistic.setTenderCount(1);
                incrStatistic.setTenderTotal(1);
            }

            incrStatistic.setTenderLzCount(1);
            incrStatistic.setTenderLzTotalCount(1);
            userCache.setTenderTransfer(true);

            userCacheService.save(userCache);
            if (!ObjectUtils.isEmpty(incrStatistic)) {
                incrStatisticBiz.caculate(incrStatistic);
            }
        }
    }

    /**
     * 如果是流转标则扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
     *
     * @param transfer
     */
    private void updateUserCacheByTransferReview(Borrow parentBorrow, Transfer transfer) throws Exception {
        UserCache userCache = userCacheService.findById(transfer.getUserId());
        userCache.setUserId(userCache.getUserId());
        if (parentBorrow.getType() == 0) {
            userCache.setTjWaitCollectionPrincipal(userCache.getTjWaitCollectionPrincipal() - transfer.getPrincipal());
            userCache.setTjWaitCollectionInterest(userCache.getTjWaitCollectionInterest() - transfer.getAlreadyInterest());
        } else if (parentBorrow.getType() == 4) {
            userCache.setQdWaitCollectionPrincipal(userCache.getQdWaitCollectionPrincipal() - transfer.getPrincipal());
            userCache.setQdWaitCollectionInterest(userCache.getQdWaitCollectionInterest() - transfer.getAlreadyInterest());
        }
        userCacheService.save(userCache);
    }


    /**
     * 更新网站统计
     *
     * @param transfer
     */
    private void updateStatisticByTransferReview(Transfer transfer) {
        //全站统计
        Statistic statistic = new Statistic();
        statistic.setLzBorrowTotal(transfer.getPrincipal());
        if (!ObjectUtils.isEmpty(statistic)) {
            try {
                statisticBiz.caculate(statistic);
            } catch (Throwable e) {
                log.error("borrowProvider updateStatisticByTransferReview 异常:", e);
            }
        }
    }

    /**
     * 生成子级债权回款记录，标注老债权回款已经转出
     *
     * @param nowDate
     * @param transfer
     * @param parentBorrow
     * @param childTenderList
     */
    private void addChildTenderCollection(Date nowDate, Transfer transfer, Borrow parentBorrow, List<Tender> childTenderList) {
        //生成子级债权回款记录，标注老债权回款已经转出
        Date repayAt = transfer.getRepayAt();/* 原借款下一期还款日期 */
        Date startAt = DateHelper.subMonths(repayAt, 1);/* 计息开始时间 */
        childTenderList.stream().forEach(childTender -> {
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    childTender.getValidMoney().doubleValue(),
                    transfer.getApr().doubleValue(),
                    transfer.getTimeLimit(),
                    startAt);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(parentBorrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            BorrowCollection borrowCollection;
            int collectionMoney = 0;
            int collectionInterest = 0;
            for (int i = 0; i < repayDetailList.size(); i++) {
                borrowCollection = new BorrowCollection();
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                collectionMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue();
                collectionInterest += new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue();
                borrowCollection.setTenderId(childTender.getId());
                borrowCollection.setStatus(0);
                borrowCollection.setOrder(i);
                borrowCollection.setUserId(childTender.getUserId());
                borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : startAt);
                borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : nowDate);
                borrowCollection.setCollectionAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                borrowCollection.setCollectionMoney(new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue());
                borrowCollection.setInterest(new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue());
                borrowCollection.setPrincipal(new Double(NumberHelper.toDouble(repayDetailMap.get("principal"))).intValue());
                borrowCollection.setCreatedAt(nowDate);
                borrowCollection.setUpdatedAt(nowDate);
                borrowCollection.setCollectionMoneyYes(0);
                borrowCollection.setLateDays(0);
                borrowCollection.setLateInterest(0l);
                borrowCollection.setBorrowId(parentBorrow.getId());
                borrowCollectionService.insert(borrowCollection);
            }

            //更新转出投资记录回款状态
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("status", 0)
                    .build();
            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            borrowCollectionList.stream().forEach(bc -> {
                bc.setTransferFlag(1);
            });
            borrowCollectionService.save(borrowCollectionList);

            // 添加待收
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.CollectionAdd);
            entity.setUserId(childTender.getUserId());
            entity.setToUserId(transfer.getUserId());
            entity.setMoney(collectionMoney);
            entity.setInterest(collectionInterest);
            entity.setRemark("添加待收金额");
            try {
                capitalChangeHelper.capitalChange(entity);
            } catch (Exception e) {
                log.error("债权转让：新债权生成回款记录失败! tenderId:" + childTender.getId());
            }
        });
    }

    /**
     * 新增子级标的
     *
     * @param nowDate
     * @param transfer
     * @param parentTender
     * @param transferBuyLogList
     * @return
     */
    private List<Tender> addChildTender(Date nowDate, Transfer transfer, Tender parentTender, List<TransferBuyLog> transferBuyLogList) {
        //生成债权记录与回款记录
        List<Tender> childTenderList = new ArrayList<>();
        transferBuyLogList.stream().forEach(transferBuyLog -> {
            Tender childTender = new Tender();
            UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());

            childTender.setUserId(transferBuyLog.getUserId());
            childTender.setStatus(1);
            childTender.setBorrowId(transfer.getBorrowId());
            childTender.setSource(transferBuyLog.getSource());
            childTender.setIsAuto(transferBuyLog.getAuto());
            childTender.setAutoOrder(transferBuyLog.getAutoOrder());
            childTender.setMoney(transferBuyLog.getBuyMoney());
            childTender.setValidMoney(transferBuyLog.getPrincipal());
            childTender.setTransferFlag(0);
            childTender.setTUserId(buyUserThirdAccount.getUserId());
            childTender.setState(2);
            childTender.setParentId(parentTender.getId());
            childTender.setTransferBuyId(transferBuyLog.getId());
            childTender.setAlreadyInterest(transferBuyLog.getAlreadyInterest());
            childTender.setThirdTransferOrderId(transferBuyLog.getThirdTransferOrderId());
            childTender.setThirdTransferFlag(transferBuyLog.getThirdTransferFlag());
            childTender.setTransferAuthCode(transferBuyLog.getTransferAuthCode());
            childTender.setCreatedAt(nowDate);
            childTender.setUpdatedAt(nowDate);
            childTenderList.add(childTender);
        });
        tenderService.save(childTenderList);

        //更新老债权为已转让
        parentTender.setTransferFlag(2);
        parentTender.setUpdatedAt(nowDate);
        tenderService.save(parentTender);
        return childTenderList;
    }

    /**
     * 债权转让初审
     *
     * @param voPcFirstVerityTransfer
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> firstVerifyTransfer(VoPcFirstVerityTransfer voPcFirstVerityTransfer) throws Exception {
        String paramStr = voPcFirstVerityTransfer.getParamStr();
        if (!SecurityHelper.checkSign(voPcFirstVerityTransfer.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "债权转让初审 签名验证不通过!"));
        }

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long transferId = NumberHelper.toLong(paramMap.get("transferId"));
        if (doFirstVerifyTransfer(transferId)) {
            return ResponseEntity.ok(VoBaseResp.ok("债权转让初审初审成功!"));
        } else {
            return ResponseEntity.
                    badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "债权转让初审初审失败,transferId：" + transferId));
        }
    }

    /**
     * 去债权转让初审
     *
     * @param transferId
     * @throws Exception
     */
    private boolean doFirstVerifyTransfer(long transferId) throws Exception {
        //更新债权转让状态
        Date nowDate = DateHelper.subSeconds(new Date(), 10);
        Transfer transfer = transferService.findById(transferId);
        transfer.setVerifyAt(nowDate);
        transfer.setState(1);
        Date releaseAt = transfer.getReleaseAt();
        transfer.setReleaseAt(ObjectUtils.isEmpty(releaseAt) ? nowDate : releaseAt);
        transferService.save(transfer);

        // 自动购买债权转让前提:
        // 1.标的年化率为 800 以上
        int apr = transfer.getApr();
        if (apr > 800) {
            transfer.setLock(true);
            transferService.save(transfer);  // 锁住债权转让,禁止手动购买

            //触发自动投标队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
            mqConfig.setTag(MqTagEnum.AUTO_TRANSFER);
            mqConfig.setSendTime(releaseAt);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("transferBizImpl doFirstVerifyTransfer send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
                return true;
            } catch (Throwable e) {
                log.error("transferBizImpl doFirstVerifyTransfer send mq exception", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 购买债权转让
     * 1.判断投资人是否存管开户、并且签约
     * 2.判断债权转让剩余金额是否大于等于购买金额
     * 3.判断账户可用金额是否大于购入金额
     * 4.生成购买债权记录
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> buyTransfer(VoBuyTransfer voBuyTransfer) throws Exception {
        String msg = null;
        long userId = voBuyTransfer.getUserId(); /*购买人id*/
        long transferId = voBuyTransfer.getTransferId(); /*债权转让记录id*/
        long buyMoney = voBuyTransfer.getBuyMoney().longValue(); /*购买债权转让金额*/
        boolean auto = voBuyTransfer.getAuto(); /* 是否是自动购买债权转让 */
        int autoOrder = voBuyTransfer.getAutoOrder(); /* 自动投标order编号 */

        UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(userId);/*购买人存管信息*/
        ThirdAccountHelper.allConditionCheck(buyUserThirdAccount);
        Transfer transfer = transferService.findByIdLock(transferId);/*债权转让记录*/
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        Asset asset = assetService.findByUserIdLock(userId);/* 购买人资产记录 */
        Preconditions.checkNotNull(asset, "购买人资产记录不存在!");

        //验证债权转让
        ImmutableMap<String, Object> verifyTransferMap = verifyTransfer(buyMoney, transfer);
        msg = StringHelper.toString(verifyTransferMap.get(MSG));
        if (!StringUtils.isEmpty(msg)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }
        long leftMoney = NumberHelper.toLong(verifyTransferMap.get(LEFT_MONEY));
        long validMoney = (long) MathHelper.min(leftMoney, buyMoney);/* 可购债权金额  */
        long alreadyInterest = validMoney / transfer.getTransferMoney() * transfer.getAlreadyInterest();/* 应付给债权转让人的当期应计利息 */

        // 验证购买人账户
        ImmutableMap<String, Object> verifyBuyTransferUserMap = verifyBuyTransferUser(buyUserThirdAccount, asset, validMoney);
        msg = StringHelper.toString(verifyBuyTransferUserMap.get(MSG));
        if (!StringUtils.isEmpty(msg)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        //生成购买债权记录
        TransferBuyLog transferBuyLog = saveTransferAndTransferLog(userId, transferId, buyMoney, transfer, validMoney, alreadyInterest, auto, autoOrder);

        //更新购买人账户金额
        updateAssetByBuyUser(transferBuyLog, transfer);

        //判断是否满标，满标触发债权转让复审
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
        mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_TRANSFER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("transferBizImpl buyTransfer send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("transferBizImpl buyTransfer send mq exception", e);
        }

        return ResponseEntity.ok(VoBaseResp.ok("购买成功!"));
    }

    /**
     * 保存债权转让与购买债权转让记录
     *
     * @param userId
     * @param transferId
     * @param buyMoney
     * @param transfer
     * @param validMoney
     * @param alreadyInterest
     */
    private TransferBuyLog saveTransferAndTransferLog(long userId, long transferId, long buyMoney, Transfer transfer, long validMoney, long alreadyInterest, boolean auto, int autoOrder) {
        //新增购买债权记录
        TransferBuyLog transferBuyLog = new TransferBuyLog();
        transferBuyLog.setUserId(userId);
        transferBuyLog.setState(0);
        transferBuyLog.setAuto(auto);
        transferBuyLog.setBuyMoney(buyMoney);
        transferBuyLog.setValidMoney(validMoney);
        transferBuyLog.setPrincipal(validMoney - alreadyInterest);
        transferBuyLog.setCreatedAt(new Date());
        transferBuyLog.setUpdatedAt(new Date());
        transferBuyLog.setDel(false);
        transferBuyLog.setAutoOrder(autoOrder);
        transferBuyLog.setTransferId(transferId);
        transferBuyLog.setAlreadyInterest(NumberHelper.toLong(alreadyInterest));
        transferBuyLog.setSource(0);
        transferBuyLogService.save(transferBuyLog);

        //更新债权抓让信息
        transfer.setTenderCount(NumberHelper.toInt(transfer.getTenderCount()) + 1);
        transfer.setTransferMoneyYes(transfer.getTransferMoneyYes() + validMoney + alreadyInterest);
        transfer.setUpdatedAt(new Date());
        transferService.save(transfer);
        return transferBuyLog;
    }

    /**
     * 更新购买人资产
     *
     * @param transferBuyLog
     * @param transfer
     */
    private void updateAssetByBuyUser(TransferBuyLog transferBuyLog, Transfer transfer) throws Exception {
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(transferBuyLog.getUserId());
        entity.setToUserId(transfer.getUserId());
        entity.setMoney(transferBuyLog.getValidMoney());
        entity.setRemark("购买债权转让冻结资金");
        capitalChangeHelper.capitalChange(entity);
    }

    /**
     * 校验购买人账户
     *
     * @param buyUserThirdAccount
     * @param asset
     * @param validMoney
     * @return
     */
    private ImmutableMap<String, Object> verifyBuyTransferUser(UserThirdAccount buyUserThirdAccount, Asset asset, double validMoney) {
        String msg = "";
        if (validMoney > asset.getUseMoney()) {
            msg = "账户余额不足，请先充值或同步资金!";
        }

        // 查询存管系统资金
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(buyUserThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            msg = "当前网络不稳定,请稍后重试!";
        }

        double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100.0;// 可用余额  账面余额-可用余额=冻结金额
        if (availBal < validMoney) {
            msg = "资金账户未同步，请先在个人中心进行资金同步操作!";
        }
        return ImmutableMap.of(MSG, msg);
    }

    /**
     * 验证债权转让
     *
     * @param buyMoney
     * @param transfer
     * @return
     */
    private ImmutableMap<String, Object> verifyTransfer(double buyMoney, Transfer transfer) {
        String msg = "";
        if (transfer.getState() != 1) {
            msg = "您看到的债权转让消失啦!";
        }

        if (transfer.getTransferMoney() == transfer.getTransferMoneyYes()) {
            msg = "债权转出金额已购满!";
        }

        long leftMoney = transfer.getTransferMoney() - transfer.getTransferMoneyYes();/*债权转让剩余可购买金额*/
        double mayBuyMoney = MathHelper.min(leftMoney, transfer.getLowest());//获取剩余可购买金额
        if (buyMoney < mayBuyMoney) {
            msg = "购买金额小于最小购买金额";
        }
        ImmutableMap<String, Object> immutableMap = ImmutableMap.of(
                MSG, msg,
                LEFT_MONEY, leftMoney
        );

        return immutableMap;
    }


    /**
     * 新版债权转让
     *
     * @param voTransferTenderReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> newTransferTender(VoTransferTenderReq voTransferTenderReq) throws Exception {
        long tenderId = voTransferTenderReq.getTenderId();/* 转让债权id */
        long userId = voTransferTenderReq.getUserId();/* 转让人id */

        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "立即转让: 查询用户投标记录为空!");
        Borrow borrow = borrowService.findByIdLock(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "立即转让: 查询用户投标标的信息为空!");

        // 前期债权转让检测
        ResponseEntity<VoBaseResp> transferConditionCheckResponse = transferConditionCheck(tender, borrow);
        if (!transferConditionCheckResponse.getStatusCode().equals(HttpStatus.OK)) {
            return transferConditionCheckResponse;
        }

        // 计算（未还款）债权本金之和
        // 判断本金必须大于 1000元
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("transferFlag", 0)
                .eq("status", 0)
                .eq("tenderId", tenderId)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "order"));
        Preconditions.checkNotNull(borrowCollectionList, "立即转让: 查询转让用户还款计划为空");
        int waitTimeLimit = borrowCollectionList.size();  // 等待回款期数
        long leftCapital = borrowCollectionList.stream().mapToLong(borrowCollection -> borrowCollection.getPrincipal()).sum(); // 待回款本金
        if (leftCapital < (1000 * 100)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "可转本金必须大于1000元才能转让"));
        }

        //保存债权转让记录
        saveTransfer(tenderId, userId, tender, borrow, waitTimeLimit, leftCapital, borrowCollectionList.get(0));
        return ResponseEntity.ok(VoBaseResp.ok("购买成功!"));
    }

    /**
     * 保存债权转让记录
     *
     * @param tenderId
     * @param userId
     * @param tender
     * @param borrow
     * @param waitTimeLimit
     * @param leftCapital
     * @param firstBorrowCollection
     */
    private void saveTransfer(long tenderId, long userId, Tender tender, Borrow borrow, int waitTimeLimit, long leftCapital, BorrowCollection firstBorrowCollection) {
        //计算当期应计利息
        int interest = firstBorrowCollection.getInterest();/* 当期理论应计利息 */
        Date startAt = DateHelper.beginOfDate(firstBorrowCollection.getStartAt());//理论开始计息时间
        Date collectionAt = DateHelper.beginOfDate(firstBorrowCollection.getCollectionAt());//理论结束还款时间
        Date startAtYes = DateHelper.beginOfDate(firstBorrowCollection.getStartAtYes());//实际开始计息时间
        Date endAt = DateHelper.beginOfDate(new Date());//结束计息时间

        /* 当期应计利息 */
        long alreadyInterest = Math.round(interest *
                Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) /
                DateHelper.diffInDays(collectionAt, startAt, false));

        //新增债权转让记录
        Date nowDate = new Date();
        Transfer transfer = new Transfer();
        transfer.setUpdatedAt(nowDate);
        transfer.setUserId(userId);
        transfer.setTransferMoneyYes(leftCapital + alreadyInterest);
        transfer.setDel(false);
        transfer.setBorrowId(borrow.getId());
        transfer.setPrincipal(leftCapital);
        transfer.setAlreadyInterest(alreadyInterest);
        transfer.setApr(borrow.getApr());
        transfer.setCreatedAt(nowDate);
        transfer.setTimeLimit(waitTimeLimit);
        transfer.setLowest(1000 * 100L);
        transfer.setState(0);
        transfer.setTenderCount(0);
        transfer.setTenderId(tenderId);
        transfer.setTitle(borrow.getName() + "转");
        transfer.setRepayAt(collectionAt);
        transferService.save(transfer);

        //更新投资记录
        tender.setTransferFlag(1);
        tender.setUpdatedAt(nowDate);
        tenderService.updateById(tender);
    }

    /**
     * 转让中
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(VoTransferReq voTransferReq) {
        try {
            Map<String, Object> resultMaps = transferService.transferOfList(voTransferReq);
            List<TransferOf> transferOfs = (List<TransferOf>) resultMaps.get("transferOfList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferOfWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferOfWarpRes.class);
            voViewTransferOfWarpRes.setTransferOfs(transferOfs);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl tranferOfList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferOfWarpRes.class));
        }
    }

    /**
     * 已转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(VoTransferReq voTransferReq) {
        try {
            Map<String, Object> resultMaps = transferService.transferedList(voTransferReq);
            List<Transfered> transfereds = (List<Transfered>) resultMaps.get("transferedList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferedWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferedWarpRes.class);
            voViewTransferOfWarpRes.setTransferedList(transfereds);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl transferedlist query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferedWarpRes.class));
        }
    }

    /**
     * 可转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(VoTransferReq voTransferReq) {
        try {
            Map<String, Object> resultMaps = transferService.transferMayList(voTransferReq);
            List<TransferMay> transferOfs = (List<TransferMay>) resultMaps.get("transferMayList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferMayWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferMayWarpRes.class);
            voViewTransferOfWarpRes.setMayList(transferOfs);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl transferMayList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferMayWarpRes.class));
        }
    }

    /**
     * 已购买
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferBuyWarpRes> tranferBuyList(VoTransferReq voTransferReq) {
        try {
            Map<String, Object> resultMaps = transferService.transferBuyList(voTransferReq);
            List<TransferBuy> transferOfs = (List<TransferBuy>) resultMaps.get("transferBuys");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferBuyWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewTransferBuyWarpRes.class);
            warpRes.setTransferBuys(transferOfs);
            warpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("TransferBizImpl transferMayList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferBuyWarpRes.class));
        }
    }

    /**
     * 债权转让
     *
     * @param voTransferTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> transferTender(VoTransferTenderReq voTransferTenderReq) {
        Date nowDate = new Date();
        Long userId = voTransferTenderReq.getUserId();
        Long tenderId = voTransferTenderReq.getTenderId();

        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "立即转让: 查询用户投标记录为空!");
        Borrow borrow = borrowService.findByIdLock(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "立即转让: 查询用户投标标的信息为空!");

        // 前期债权转让检测
        ResponseEntity<VoBaseResp> transferConditionCheck = transferConditionCheck(tender, borrow);
        if (!transferConditionCheck.getStatusCode().equals(HttpStatus.OK)) {
            return transferConditionCheck;
        }

        // 计算债权本金之和
        // 判断本金必须大于 1000元
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("transferFlag", 0)
                .eq("status", 0)
                .eq("tenderId", tenderId)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "order"));
        Preconditions.checkNotNull(borrowCollectionList, "立即转让: 查询转让用户还款计划为空");
        int waitTimeLimit = borrowCollectionList.size();  // 等待回款期数
        int cantrCapital = borrowCollectionList.stream().mapToInt(borrowCollection -> borrowCollection.getPrincipal()).sum(); // 待汇款本金
        if (cantrCapital < (1000 * 100)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "可转本金必须大于1000元才能转让"));
        }

        saveTranferBorrow(nowDate, userId, tender, borrow, waitTimeLimit, cantrCapital);
        return ResponseEntity.ok(VoBaseResp.ok("操作成功"));
    }

    /**
     * 保存债权,并且更改投标记录为转让中
     *
     * @param nowDate
     * @param userId
     * @param tender
     * @param borrow
     * @param waitTimeLimit
     * @param cantrCapital
     */
    private void saveTranferBorrow(Date nowDate, Long userId, Tender tender, Borrow borrow, int waitTimeLimit, long cantrCapital) {
        // 转让借款
        Borrow tranferBorrow = new Borrow();
        tranferBorrow.setType(3); // 3 转让标
        tranferBorrow.setUse(borrow.getUse());
        tranferBorrow.setIsLock(false);
        tranferBorrow.setRepayFashion(borrow.getRepayFashion());
        tranferBorrow.setTimeLimit(borrow.getRepayFashion() == 1 ? borrow.getTimeLimit() : waitTimeLimit);
        tranferBorrow.setMoney(cantrCapital);
        tranferBorrow.setApr(borrow.getApr());
        tranferBorrow.setLowest(1000 * 100);
        tranferBorrow.setValidDay(1);
        tranferBorrow.setName(borrow.getName());
        tranferBorrow.setDescription(borrow.getDescription());
        tranferBorrow.setIsVouch(borrow.getIsVouch());
        tranferBorrow.setIsMortgage(borrow.getIsMortgage());
        tranferBorrow.setIsConversion(borrow.getIsConversion());
        tranferBorrow.setUserId(userId);
        tranferBorrow.setTenderId(tender.getId());
        tranferBorrow.setCreatedAt(nowDate);
        tranferBorrow.setUpdatedAt(nowDate);
        tranferBorrow.setMost(0);
        tranferBorrow.setMostAuto(0);
        tranferBorrow.setAwardType(0);
        tranferBorrow.setAward(0);
        tranferBorrow.setPassword("");
        tranferBorrow.setMoneyYes(0l);
        tranferBorrow.setTenderCount(0);
        borrowService.insert(tranferBorrow);//插入转让标

        tender.setTransferFlag(1);
        tender.setUpdatedAt(nowDate);
        tenderService.updateById(tender);
    }

    /**
     * 债权装让前期检测
     * 1. 当期债权是否已经发生转让行为
     * 2. 当前待还是否为官方标的
     * 3. 保证只能同时发生一个债权转让
     *
     * @param tender
     * @param borrow
     * @return
     */
    private ResponseEntity<VoBaseResp> transferConditionCheck(Tender tender, Borrow borrow) {
        if (tender.getTransferFlag() != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "操作失败: 你已经出让债权了!"));
        }

        if ((tender.getStatus() != 1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前系统出现异常, 麻烦通知平台客服人员!"));
        }

        if ((borrow.getType() != 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投资非投资官方标的是不可债权转让!"));
        }

        if ((borrow.getStatus() != 3)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前债权不符合转让规则!"));
        }


        Specification<Borrow> borrowSpecification = Specifications
                .<Borrow>and()
                .eq("userId", tender.getUserId())
                .in("status", 0, 1)
                .build();

        long tranferingNum = borrowService.count(borrowSpecification);
        if (tranferingNum > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "您已经有一个进行中的借款标"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
        return ThirdAccountHelper.allConditionCheck(userThirdAccount);
    }

    /**
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    public ResponseEntity<VoGoTenderInfo> goTenderInfo(Long tenderId, Long userId) {
        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "");
        Preconditions.checkArgument(userId.equals(tender.getUserId()), "获取立即转让详情: 非法操作!");

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("tenderId", tenderId)
                .eq("status", 0)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "id"));
        Preconditions.checkNotNull(borrowCollections, "获取立即转让详情: 还款计划查询失败!");
        BorrowCollection borrowCollection = borrowCollections.get(0);
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Preconditions.checkNotNull(borrowCollections, "获取立即转让详情: 获取投资的标的信息失败!");
        String repayFashionStr = "";
        switch (borrow.getRepayFashion()) {
            case BorrowContants.REPAY_FASHION_AYFQ_NUM:
                repayFashionStr = "按月分期";
                break;
            case BorrowContants.REPAY_FASHION_YCBX_NUM:
                repayFashionStr = "一次性还本付息";
                break;
            case BorrowContants.REPAY_FASHION_XXHB_NUM:
                repayFashionStr = "先息后本";
                break;
            default:
        }

        int money = borrowCollections.stream().mapToInt(borrowCollectionItem -> borrowCollectionItem.getPrincipal()).sum(); // 待汇款本金
        // 0.4% + 0.08% * (剩余期限-1)  （费率最高上限为1.28%）
        double rate = 0.004 + 0.0008 * (borrowCollections.size() - 1);
        rate = Math.min(rate, 0.0128);
        Double fee = money * rate;  // 费用
        int day = DateHelper.diffInDays(borrowCollection.getCollectionAt(), new Date(), false);
        day = day < 0 ? 0 : day;
        VoGoTenderInfo voGoTenderInfo = VoGoTenderInfo.ok("查询成功!", VoGoTenderInfo.class);
        voGoTenderInfo.setTenderId(tender.getId());
        voGoTenderInfo.setApr(StringHelper.formatDouble(borrow.getApr(), 100.0, false));
        voGoTenderInfo.setBorrowName(borrow.getName());
        voGoTenderInfo.setNextRepaymentDate(DateHelper.dateToString(borrowCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
        voGoTenderInfo.setSurplusDate(String.valueOf(day));
        voGoTenderInfo.setRepayFashionStr(repayFashionStr);
        voGoTenderInfo.setTimeLimit(String.valueOf(borrowCollections.size()) + "个月");
        voGoTenderInfo.setMoney(StringHelper.formatDouble(money, 100.0, true));
        voGoTenderInfo.setFee(StringHelper.formatDouble(fee, 100.0, true));
        return ResponseEntity.ok(voGoTenderInfo);
    }

    @Override
    public List<VoViewBorrowList> findTransferList(VoBorrowListReq voBorrowListReq) {
        Specification<Transfer> ts = Specifications
                .<Transfer>and()
                .in("state", ImmutableList.of(1, 2))
                .build();
        Pageable pageable = new PageRequest(voBorrowListReq.getPageIndex(), voBorrowListReq.getPageSize(), new Sort(Sort.Direction.ASC, "state"));
        List<Transfer> transferList = transferService.findList(ts, pageable);
        if (CollectionUtils.isEmpty(transferList)) {
            return Lists.newArrayList();
        }

        Set<Long> borrowIds = transferList
                .stream()
                .map(transfer -> transfer.getBorrowId()).collect(Collectors.toSet());

        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", borrowIds)
                .build();

        List<Borrow> borrowList = borrowService.findList(bs);
        if (CollectionUtils.isEmpty(borrowList)) {
            return Lists.newArrayList();
        }

        Map<Long, Borrow> borrowRef = borrowList
                .stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));
        Set<Long> userIds = transferList
                .stream()
                .map(transfer -> transfer.getUserId()).collect(Collectors.toSet());
        Specification<Users> us = Specifications
                .<Users>and()
                .in("id", userIds)
                .build();

        List<Users> userLists = userService.findList(us);
        Map<Long, Users> userRef = userLists
                .stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));


        List<VoViewBorrowList> voViewBorrowLists = new ArrayList<>(transferList.size());

        VoViewBorrowList voViewBorrowList = null;
        Borrow borrow = null;
        for (Transfer item : transferList) {
            voViewBorrowList = new VoViewBorrowList();
            borrow = borrowRef.get(item.getBorrowId());
            voViewBorrowList.setId(item.getId());  // ID
            voViewBorrowList.setMoney(StringHelper.formatMon(item.getTransferMoney() / 100d) + MoneyConstans.RMB);
            voViewBorrowList.setIsContinued(borrow.getIsContinued());
            voViewBorrowList.setLockStatus(borrow.getIsLock());
            voViewBorrowList.setIsImpawn(borrow.getIsImpawn());
            voViewBorrowList.setApr(StringHelper.formatMon(borrow.getApr() / 100d) + MoneyConstans.PERCENT);  //转换率
            voViewBorrowList.setName(item.getTitle());
            voViewBorrowList.setMoneyYes(StringHelper.formatMon(item.getPrincipal() / 100d) + MoneyConstans.RMB);
            voViewBorrowList.setIsNovice(borrow.getIsNovice());
            voViewBorrowList.setIsMortgage(borrow.getIsMortgage());
            voViewBorrowList.setIsPassWord(false);

            if (borrow.getType() == com.gofobao.framework.borrow.contants.BorrowContants.REPAY_FASHION_ONCE) {
                voViewBorrowList.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
            } else {
                voViewBorrowList.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
            }

            //待发布时间
            voViewBorrowList.setSurplusSecond(0L);
            //进度
            voViewBorrowList.setSpend(0d);

            if (item.getState() == 1) {  //债权转让进行中
                double spend = Double.parseDouble(StringHelper.formatMon(item.getTransferMoneyYes().doubleValue() / item.getTransferMoney()));
                if (spend == 1) {
                    voViewBorrowList.setStatus(6);
                } else {
                    voViewBorrowList.setStatus(3);
                }
                voViewBorrowList.setSpend(spend);
            } else { // 回款中
                if (ObjectUtils.isEmpty(borrow.getCloseAt())) {
                    voViewBorrowList.setStatus(2);
                    voViewBorrowList.setSpend(1D);
                } else {
                    voViewBorrowList.setStatus(4);
                    voViewBorrowList.setSpend(1D);
                }
            }

            Users user = userRef.get(item.getUserId());
            voViewBorrowList.setUserName(!StringUtils.isEmpty(user.getUsername()) ? user.getUsername() : user.getPhone());
            voViewBorrowList.setType(5);
            voViewBorrowList.setIsFlow(true);
            voViewBorrowList.setReleaseAt(DateHelper.dateToString(item.getReleaseAt()));
            voViewBorrowList.setRepayFashion(borrow.getRepayFashion());
            voViewBorrowList.setIsVouch(borrow.getIsVouch());
            voViewBorrowList.setTenderCount(item.getTenderCount());
            voViewBorrowList.setAvatar(imageDomain + "/data/images/avatar/" + item.getUserId() + "_avatar_middle.jpg");
            voViewBorrowLists.add(voViewBorrowList);
        }
        return voViewBorrowLists;
    }
}
