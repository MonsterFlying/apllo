package com.gofobao.framework.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.model.debt_details_query.DebtDetail;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListWarpRes;
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
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/5/26.
 */
@Service
@Slf4j
public class BorrowBizImpl implements BorrowBiz {

    static final Gson GSON = new Gson();

    @Autowired
    private UserService userService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;


    @Override
    public ResponseEntity<VoViewBorrowListWarpRes> findAll(VoBorrowListReq voBorrowListReq) {
        try {
            List<VoViewBorrowList> borrowLists = borrowService.findAll(voBorrowListReq);
            VoViewBorrowListWarpRes listWarpRes = VoBaseResp.ok("查询成功", VoViewBorrowListWarpRes.class);
            listWarpRes.setVoViewBorrowLists(borrowLists);
            return ResponseEntity.ok(listWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"查询失败", VoViewBorrowListWarpRes.class));
        }
    }

    /**
     * 新增净值借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow) {
        Long userId = voAddNetWorthBorrow.getUserId();
        String releaseAtStr = voAddNetWorthBorrow.getReleaseAt();
        Integer money = voAddNetWorthBorrow.getMoney();
        boolean closeAuto = voAddNetWorthBorrow.isCloseAuto();

        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(asset)) {
            log.info("新增净值借款：用户asset未被查询得到。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users.getCardId())) {
            log.info("新增净值借款：当前用户未实名。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未实名认证!"));
        }

        Date releaseAt = DateHelper.stringToDate(releaseAtStr, DateHelper.DATE_FORMAT_YMDHMS);
        if (releaseAt.getTime() > DateHelper.addDays(new Date(), 1).getTime()) {
            log.info("新增净值借款：发布时间必须在24小时内。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "发布时间必须在24小时内!"));
        }

        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)) {
            log.info("新增净值借款：用户usercache未被查询得到。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        double totalMoney = (asset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8 - asset.getPayment();
        if (totalMoney < money) {
            log.info("新增净值借款：借款金额大于净值额度。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款金额大于净值额度!"));
        }

        long count = borrowService.countByUserIdAndStatusIn(userId, Arrays.asList(0, 1));
        if (count > 0) {
            log.info("新增净值借款：您已经有一个进行中的借款标。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "您已经有一个进行中的借款标!"));
        }

        if (closeAuto) { //关闭用户自动投标
            AutoTender saveAutoTender = new AutoTender();
            saveAutoTender.setStatus(false);
            saveAutoTender.setUpdatedAt(new Date());

            AutoTender condAutoTender = new AutoTender();
            condAutoTender.setUserId(userId);
            Example<AutoTender> autoTenderExample = Example.of(condAutoTender);

            if (!autoTenderService.updateByExample(saveAutoTender, autoTenderExample)) {
                log.info("新增净值借款：自动投标关闭失败。");
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标关闭失败!"));
            }
        }

        Long borrowId = null;
        try {
            borrowId = insertBorrow(voAddNetWorthBorrow, userId);  // 插入标
        } catch (Exception e) {
            log.error("新增净值借款异常：", e);
        }

        if (borrowId <= 0) {
            log.info("新增净值借款：净值标插入失败。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "净值标插入失败!"));
        }

        //初审
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
        mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        boolean mqState = false;
        try {
            log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
            mqState = mqHelper.convertAndSend(mqConfig);
        } catch (Exception e) {
            log.error("borrowBizImpl firstVerify send mq exception", e);
        }

        if (!mqState) {
            return ResponseEntity.ok(VoBaseResp.ok("发布净值借款失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("发布净值借款成功!"));
    }

    private long insertBorrow(VoAddNetWorthBorrow voAddNetWorthBorrow, Long userId) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        Borrow borrow = new Borrow();
        borrow.setType(BorrowContants.JING_ZHI); // 净值标
        borrow.setUserId(userId);
        borrow.setTUserId(userThirdAccount.getId());
        borrow.setUse(0);
        borrow.setStatus(0);
        borrow.setIsLock(false);
        borrow.setIsImpawn(false);
        borrow.setIsContinued(false);
        borrow.setIsConversion(false);
        borrow.setIsNovice(false);
        borrow.setIsVouch(false);
        borrow.setIsMortgage(false);
        borrow.setName(voAddNetWorthBorrow.getName());
        borrow.setMoney(voAddNetWorthBorrow.getMoney());
        borrow.setRepayFashion(1);
        borrow.setTimeLimit(voAddNetWorthBorrow.getTimeLimit());
        borrow.setApr(voAddNetWorthBorrow.getApr());
        borrow.setLowest(50 * 100);
        borrow.setMost(0);
        borrow.setMostAuto(0);
        borrow.setValidDay(voAddNetWorthBorrow.getValidDay());
        borrow.setAward(0);
        borrow.setAwardType(0);
        String releaseAt = voAddNetWorthBorrow.getReleaseAt();
        if (!ObjectUtils.isEmpty(releaseAt)) {
            borrow.setReleaseAt(DateHelper.stringToDate(releaseAt, "yyyy-MM-dd HH:mm:ss"));
        }
        borrow.setDescription("");
        borrow.setPassword("");
        borrow.setMoneyYes(0);
        borrow.setTenderCount(0);
        borrow.setCreatedAt(new Date());
        borrow.setUpdatedAt(new Date());
        boolean rs = borrowService.insert(borrow);
        if (rs) {
            return borrow.getId();
        } else {
            return 0;
        }
    }

    /**
     * 取消借款
     *
     * @param voCancelBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow) {
        Long borrowId = voCancelBorrow.getBorrowId();
        Long userId = voCancelBorrow.getUserId();
        Date nowDate = new Date();

        Borrow borrow = borrowService.findByIdLock(borrowId);
        if (ObjectUtils.isEmpty(borrow) || ObjectUtils.isEmpty(userId)
                || (borrow.getStatus() != 0 && borrow.getStatus() != 1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款状态已发生更改!"));
        }

        boolean bool = false;//债权转让默认不过期
        if (!ObjectUtils.isEmpty(borrow.getReleaseAt())) {
            bool = DateHelper.diffInDays(new Date(), borrow.getReleaseAt(), false) >= borrow.getValidDay();//比较借款时间是否过期
        }

        if (((borrow.getStatus() == 1) && (bool)) || (StringHelper.toString(borrow.getUserId()).equals(StringHelper.toString(voCancelBorrow.getUserId())))) {//只有借款标过期或者本人才能取消借款

        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "只有借款标过期或者本人才能取消借款!"));
        }

        //================================调用即信取消标的====================================
        VoQueryThirdBorrowList voQueryThirdBorrowList = new VoQueryThirdBorrowList();
        voQueryThirdBorrowList.setBorrowId(borrowId);
        voQueryThirdBorrowList.setUserId(userId);
        voQueryThirdBorrowList.setPageNum("1");
        voQueryThirdBorrowList.setPageSize("10");
        DebtDetailsQueryResp resp = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);
        int totalItems = NumberHelper.toInt(resp.getTotalItems()) + 1;
        if (totalItems > 0) {//在即信查询到对应的标的
            List<DebtDetail> debtDetailList = GSON.fromJson(resp.getSubPacks(), new TypeToken<List<DebtDetail>>() {
            }.getType());
            Preconditions.checkNotNull(debtDetailList, "即信标的不存在!");

            VoCancelThirdBorrow voCancelThirdBorrow = new VoCancelThirdBorrow();
            voCancelThirdBorrow.setUserId(userId);
            voCancelThirdBorrow.setBorrowId(borrowId);
            voCancelThirdBorrow.setRaiseDate(debtDetailList.get(0).getRaiseDate());
            ResponseEntity responseEntity = borrowThirdBiz.cancelThirdBorrow(voCancelThirdBorrow);
            if (!ObjectUtils.isEmpty(responseEntity)) {
                return responseEntity;
            }
        }
        //======================================================================================

        Specification<Tender> borrowSpecification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(borrowSpecification);
        Set<Long> tenderUserIds = new HashSet<>();//投标用户id集合
        if (!CollectionUtils.isEmpty(tenderList)) {
            Iterator<Tender> itTender = tenderList.iterator();
            Tender tender = null;
            Notices notices = null;
            while (itTender.hasNext()) {
                notices = new Notices();
                tender = itTender.next();

                //更新资产记录
                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.Unfrozen);
                entity.setUserId(tender.getUserId());
                entity.setMoney(tender.getValidMoney());
                entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 招标失败解除冻结资金。");
                try {
                    capitalChangeHelper.capitalChange(entity);
                } catch (Exception e) {
                    log.error("borrowBizImpl cancelBorrow error", e);
                }

                //更新投标记录状态
                tender.setId(tender.getId());
                tender.setStatus(2); // 取消状态
                tender.setUpdatedAt(nowDate);
                tenderService.updateById(tender);

                if (!tenderUserIds.contains(tender.getUserId())) {
                    tenderUserIds.add(tender.getUserId());
                    notices.setFromUserId(1L);
                    notices.setUserId(tender.getUserId());
                    notices.setRead(false);
                    notices.setName("投资的借款失败");
                    notices.setContent("你所投资的借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]在" + DateHelper.nextDate(nowDate) + "已取消");
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
                        log.info(String.format("borrowBizImpl cancelBorrow send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Exception e) {
                        log.error("borrowBizImpl cancelBorrow send mq exception", e);
                    }
                }
            }
        }

        Long tenderId = borrow.getTenderId();
        if ((borrow.getType() == 0) && (!ObjectUtils.isEmpty(tenderId)) && (tenderId > 0)) {//判断是否是转让标，并将借款状态置为0
            Tender tender = tenderService.findById(tenderId);
            tender.setTransferFlag(0);
            tender.setUpdatedAt(nowDate);
            tenderService.updateById(tender);
        }

        Integer payMoney = 0;
        if (borrow.getType() == 2) {//如果是秒标则解除冻结
            Double principal = NumberHelper.toDouble(StringHelper.toString(borrow.getMoney()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal,
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> debx = borrowCalculatorHelper.dengEBenXi();
            payMoney = (int) MathHelper.myRound((Double) debx.get("repayTotal") - principal, 2);

            if (borrow.getAwardType() == 1) {
                payMoney += borrow.getAward();
            } else if (borrow.getAwardType() == 2) {
                payMoney += (int) MathHelper.myRound(borrow.getMoney() * borrow.getAward(), 2);
            }

            //更新资产记录
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Unfrozen);
            entity.setUserId(borrow.getUserId());
            entity.setMoney(payMoney);
            entity.setRemark("取消秒标借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]解除冻结资金");
            try {
                capitalChangeHelper.capitalChange(entity);
            } catch (Exception e) {
                log.error("borrowBizImpl cancelBorrow error", e);
            }
        }

        //更新借款
        borrow.setStatus(5);
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);

        return ResponseEntity.ok(VoBaseResp.ok("取消借款成功!"));
    }
}
