package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.IntTypeContant;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCreateThirdBorrowReq;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Persistence;
import java.util.*;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    Gson GSON = new GsonBuilder().create();

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;

    /**
     * 初审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doFirstVerify(Map<String, String> msg) throws Exception {
        boolean bool = false;
        do {
            Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
                return false;
            }

            Integer borrowType = borrow.getType();
            if (borrowType == 2) { //秒标
                bool = miaoBorrow(borrow);
            } else if (!ObjectUtils.isEmpty(borrow.getLendId())) { //转让标
                bool = lendBorrow(borrow);
            } else { //车贷、渠道、净值、转让 标
                bool = baseBorrow(borrow);
            }

            if (!bool || borrow.isTransfer()) { //初审操作失败  或者 转让标不需要与即信通信
                break;
            }

            bool = createThridBorrow(borrow);
        } while (false);
        return bool;
    }

    private boolean createThridBorrow(Borrow borrow) {
        //===================即信登记标的===========================
        String name = borrow.getName();
        Long userId = borrow.getUserId();

        VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
        voCreateThirdBorrowReq.setUserId(userId);
        voCreateThirdBorrowReq.setRate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        voCreateThirdBorrowReq.setTxAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
        voCreateThirdBorrowReq.setAcqRes(String.valueOf(userId));
        voCreateThirdBorrowReq.setIntType(IntTypeContant.SINGLE_USE);
        voCreateThirdBorrowReq.setDuration(String.valueOf(borrow.getTimeLimit()));
        voCreateThirdBorrowReq.setProductDesc(StringUtils.isEmpty(name) ? "净值借款" : name);
        voCreateThirdBorrowReq.setProductId(String.valueOf(borrow.getId()));
        voCreateThirdBorrowReq.setRaiseDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        voCreateThirdBorrowReq.setRaiseEndDate(DateHelper.dateToString(DateHelper.addDays(new Date(), 1), DateHelper.DATE_FORMAT_YMD_NUM));

        ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
        if (!ObjectUtils.isEmpty(resp)) {
            log.error("===========================即信标的登记==============================");
            log.error("即信标的登记失败：", resp.getBody().getState().getMsg());
            log.error("=====================================================================");
            return false;
        }
        return true;
    }

    /**
     * 车贷标、净值标、渠道标、转让标初审
     *
     * @return
     */
    private boolean baseBorrow(Borrow borrow) {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer borrowType = borrow.getType();
            if ((!ObjectUtils.isEmpty(borrow.getPassword())) || (borrowType != 0 || borrowType != 1 || borrowType != 4) && borrow.getApr() < 800) {
                break;
            }

            //更新借款状态
            borrow.setIsLock(true);
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Date releaseDate = borrow.getReleaseAt();

            //====================================
            //延时投标
            //====================================
            if (borrow.getIsNovice()) {//判断是否是新手标
                Date tempDate = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);
                releaseDate = DateHelper.max(tempDate, releaseDate);
            }

            //触发自动投标队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Exception e) {
                log.error("borrowProvider autoTender send mq exception", e);
            }
            bool = true;
        } while (false);
        return bool;
    }

    /**
     * 摘草 生成借款 初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean lendBorrow(Borrow borrow) throws Exception {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Long lendId = borrow.getLendId();
            if (!ObjectUtils.isEmpty(lendId)) {
                Lend lend = lendService.findById(lendId);
                VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
                voCreateTenderReq.setUserId(lend.getUserId());
                voCreateTenderReq.setBorrowId(borrow.getId());
                voCreateTenderReq.setTenderMoney(borrow.getMoney());
                Map<String, Object> rsMap = tenderBiz.createTender(voCreateTenderReq);

                Object msg = rsMap.get("msg");
                if (ObjectUtils.isEmpty(msg)) {
                    log.error(StringHelper.toString(msg));
                }
            }
            bool = true;
        } while (false);
        return bool;
    }

    /**
     * 秒标初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean miaoBorrow(Borrow borrow) throws Exception {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer payMoney = 0;
            Integer borrowType = borrow.getType();
            if (borrowType != 2) {
                break;
            }
            Double principal = NumberHelper.toDouble(StringHelper.toString(borrow.getMoney()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal,
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            payMoney = (int) MathHelper.myRound((Double) rsMap.get("repayTotal") - principal, 2);

            if (borrow.getAwardType() == 1) {
                payMoney += borrow.getAward();
            } else if (borrow.getAwardType() == 2) {  //
                payMoney += (int) MathHelper.myRound(borrow.getMoney() * borrow.getAward() / 100 / 100, 2);
            }

            //更新资产记录
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Frozen);
            entity.setUserId(borrow.getUserId());
            entity.setMoney(payMoney);
            entity.setRemark("冻结秒标应付资金");
            capitalChangeHelper.capitalChange(entity);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);
            bool = true;
        } while (false);
        return bool;
    }

    /**
     * 复审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doAgainVerify(Map<String, String> msg) throws Exception {
        boolean bool = false;
        do {
            Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get("borrowId")));
            Date nowDate = new Date();

            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 1) || (borrow.getMoney() == borrow.getMoneyYes())) {
                break;
            }

            Long tenderId = borrow.getTenderId();
            Integer repayMoney = 0;
            Integer repayInterest = 0;
            Integer borrowType = borrow.getType();
            List<BorrowCollection> transferedBorrowCollections = null;
            if (borrow.isTransfer()) {
                //============================更新转让标识=============================
                BorrowCollection borrowCollection = new BorrowCollection();
                borrowCollection.setTransferFlag(1);
                Specification<BorrowCollection> bcs = Specifications.<BorrowCollection>and()
                        .eq("tenderId", tenderId)
                        .eq("status", 0)
                        .build();
                borrowCollectionService.updateBySpecification(borrowCollection, bcs);

                Tender tender = new Tender();
                tender.setId(tenderId);
                tender.setTransferFlag(2);
                tenderService.updateById(tender);
                //======================================================================
                //扣除转让待收
                bcs = Specifications.<BorrowCollection>and()
                        .eq("status", 0)
                        .eq("transferFlag", 1)
                        .build();

                transferedBorrowCollections = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "`order`"));

                Integer collectionMoney = 0;
                Integer collectionInterest = 0;
                for (BorrowCollection temp : transferedBorrowCollections) {
                    collectionMoney += temp.getCollectionMoney();
                    collectionInterest += temp.getInterest();
                }

                //更新资产记录
                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.CollectionLower);
                entity.setUserId(borrow.getUserId());
                entity.setMoney(collectionMoney);
                entity.setInterest(collectionInterest);
                entity.setRemark("债权转让成功，扣除待收资金");
                capitalChangeHelper.capitalChange(entity);
            } else {
                BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(StringHelper.toString(borrow.getMoney())),
                        NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
                Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");

                BorrowRepayment borrowRepayment = new BorrowRepayment();
                for (int i = 0; i < repayDetailList.size(); i++) {
                    Map<String, Object> repayDetailMap = repayDetailList.get(i);
                    repayMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue();
                    repayInterest += new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue();
                    borrowRepayment.setBorrowId(borrow.getId());
                    borrowRepayment.setStatus(0);
                    borrowRepayment.setOrder(i);
                    borrowRepayment.setRepayAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                    borrowRepayment.setRepayMoney(new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue());
                    borrowRepayment.setPrincipal(new Double(NumberHelper.toDouble(repayDetailMap.get("principal"))).intValue());
                    borrowRepayment.setInterest(new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue());
                    borrowRepayment.setRepayMoneyYes(0);
                    borrowRepayment.setCreatedAt(nowDate);
                    borrowRepayment.setUpdatedAt(nowDate);
                    borrowRepayment.setAdvanceMoneyYes(0);
                    borrowRepayment.setLateDays(0);
                    borrowRepayment.setLateInterest(0);
                    borrowRepaymentService.insert(borrowRepayment);
                }
            }

            //投标用户id集合
            Set<Integer> tenderUserIds = new HashSet<>();

            //查询当前借款的所有 状态为1的 tender记录
            Specification<Tender> ts = Specifications.<Tender>and()
                    .eq("borrowId", borrowId)
                    .eq("status", 1)
                    .build();
            List<Tender> tenderList = tenderService.findList(ts);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            Date borrowDate = null;
            BorrowCollection transferedBorrowCollection = transferedBorrowCollections.get(0); //最近一期转让债券
            BorrowCollection borrowCollection = null;
            for (Tender tender : tenderList) {
                borrowDate = (borrow.getType() == 0) && (!ObjectUtils.isEmpty(borrow.getTenderId())) && (borrow.getTenderId() > 0) ? transferedBorrowCollection.getStartAt() : nowDate;

                BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                        NumberHelper.toDouble(StringHelper.toString(tender.getValidMoney())),
                        NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrowDate);
                Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");

                borrowCollection = new BorrowCollection();
                Integer collectionMoney = 0;
                Integer collectionInterest = 0;
                for (int i = 0; i < repayDetailList.size(); i++) {
                    Map<String, Object> repayDetailMap = repayDetailList.get(i);
                    collectionMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue();
                    collectionInterest += new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue();
                    borrowCollection.setTenderId(tender.getId());
                    borrowCollection.setStatus(0);
                    borrowCollection.setOrder(i);
                    borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))) : borrowDate);
                    borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))) : nowDate);
                    borrowCollection.setCollectionAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                    borrowCollection.setCollectionMoney(new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue());
                    borrowCollection.setPrincipal(new Double(NumberHelper.toDouble(repayDetailMap.get("principal"))).intValue());
                    borrowCollection.setInterest(new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue());
                    borrowCollection.setCreatedAt(nowDate);
                    borrowCollection.setUpdatedAt(nowDate);
                    borrowCollection.setCollectionMoneyYes(0);
                    borrowCollection.setLateDays(0);
                    borrowCollection.setLateInterest(0);
                    borrowCollectionService.insert(borrowCollection);
                }

                //扣除冻结
                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.Tender);
                entity.setUserId(tender.getUserId());
                entity.setToUserId(borrow.getUserId());
                entity.setMoney(tender.getValidMoney());
                entity.setRemark("成功投资[" + BorrowHelper.getBorrowLink(borrowId, borrow.getName()) + "]");
                capitalChangeHelper.capitalChange(entity);

                //添加待收
                entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.CollectionAdd);
                entity.setUserId(tender.getUserId());
                entity.setToUserId(borrow.getUserId());
                entity.setMoney(collectionMoney);
                entity.setInterest(collectionInterest);
                entity.setRemark("添加待收金额");
                capitalChangeHelper.capitalChange(entity);

                //添加奖励
                if (borrow.getAwardType() > 0) {
                    int money = (int) MathHelper.myRound((tender.getValidMoney() / borrow.getMoney()) * borrow.getAward(), 2);
                    if (borrow.getAwardType() == 2) {
                        money = (int) MathHelper.myRound(tender.getValidMoney() * borrow.getAward() / 100, 2);
                    }

                    entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.Award);
                    entity.setUserId(tender.getUserId());
                    entity.setToUserId(borrow.getUserId());
                    entity.setMoney(money);
                    entity.setRemark("借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的奖励");
                    capitalChangeHelper.capitalChange(entity);
                }

                if (!tenderUserIds.contains(tender.getUserId())) {
                    Notices notices = new Notices();
                    notices.setFromUserId(1L);
                    notices.setUserId(tender.getUserId());
                    notices.setRead(false);
                    notices.setName("投资的借款满标审核通过");
                    notices.setContent("您所投资的借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]在 " + DateHelper.dateToString(nowDate) + " 已满标审核通过");
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
                        log.info(String.format("borrowProvider doAgainVerify send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Exception e) {
                        log.error("borrowProvider doAgainVerify send mq exception", e);
                    }
                }

                //触发投标成功事件
                //=============================================================
                //投资车贷标成功添加 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
                //更新 投过相应标种 标识
                //=============================================================
                /**
                 * @// TODO: 2017/6/2 投标成功事件
                 */

            }

            //借款入账
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Borrow);
            entity.setUserId(borrow.getUserId());
            entity.setMoney(borrow.getMoney());
            entity.setRemark("通过[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]借到的款");

            if (borrowType == 2) {
                entity.setAsset("add@noUseMoney");
            }
            capitalChangeHelper.capitalChange(entity);

            //扣除奖励
            Integer awardType = borrow.getAwardType();
            if (!ObjectUtils.isEmpty(awardType)) {
                entity = new CapitalChangeEntity();

                if (borrow.getType() == 2) {
                    entity.setAsset("sub@noUseMoney");
                }
                int tempMoney = borrow.getAward();
                if (borrow.getAwardType() == 2) {
                    tempMoney = borrow.getMoney() * borrow.getAward();
                }
                entity.setType(CapitalChangeEnum.Fee);
                entity.setUserId(borrow.getUserId());
                entity.setMoney(tempMoney);
                entity.setRemark("扣除借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的奖励");
                capitalChangeHelper.capitalChange(entity);
            }

            if ((borrow.getType() == 0) && (!ObjectUtils.isEmpty(borrow.getTenderId())) && (borrow.getTenderId() > 0)) { //转让管理费
                double transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128);

                //转让管理费
                entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.Fee);
                entity.setUserId(borrow.getUserId());
                entity.setMoney((int) (borrow.getMoney() * transferFeeRate));
                entity.setRemark("扣除借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的转让管理费");
                capitalChangeHelper.capitalChange(entity);

            } else {
                //添加待还
                entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.PaymentAdd);
                entity.setUserId(borrow.getUserId());
                entity.setMoney(repayMoney);
                entity.setInterest(repayInterest);
                entity.setRemark("添加待还金额");
                capitalChangeHelper.capitalChange(entity);
            }

            //净值账户管理费
            if (borrowType == 1) {
                double manageFeeRate = 0.0012;
                double fee = 0;
                if (borrow.getRepayFashion() == 1) {
                    fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 2);
                } else {
                    fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 2);
                }

                entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.Manager);
                entity.setUserId(borrow.getUserId());
                entity.setMoney((int) fee);
                entity.setRemark("扣除借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的管理费");
                capitalChangeHelper.capitalChange(entity);
            }

            Borrow tempBorrow = new Borrow();
            tempBorrow.setId(borrow.getId());
            tempBorrow.setStatus(3);
            tempBorrow.setSuccessAt(nowDate);
            borrowService.updateById(tempBorrow);

            /**
             * @// TODO: 2017/6/2 复审事件
             */
            /**
             * updateUserCacheByBorrowReview(borrowId);
             //更新网站统计
             updateStatisticByBorrowReview(borrowId);
             //借款成功发送通知短信
             smsNoticeByBorrowReview(borrowId);
             //发送借款协议
             try {
             sendBorrowProtocol(borrowId);
             } catch (Exception e) {
             logger.debug("借款协议发送失败!");
             }
             //还款事件得放最后面
             autoRepayForMiao(borrow);
             */
            bool = true;
        } while (false);
        return bool;
    }
}
