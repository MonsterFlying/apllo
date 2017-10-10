package com.gofobao.framework.starfire.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.starfire.common.response.CodeTypeConstant;
import com.gofobao.framework.starfire.common.response.ResultCodeEnum;
import com.gofobao.framework.starfire.common.response.ResultCodeMsgEnum;
import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowCollectionConstant;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowConstant;
import com.gofobao.framework.starfire.tender.biz.StarFireTenderBiz;
import com.gofobao.framework.starfire.tender.vo.request.BorrowCollectionRecords;
import com.gofobao.framework.starfire.tender.vo.request.UserTenderQuery;
import com.gofobao.framework.starfire.tender.vo.response.UserBorrowCollectionRecordsRes;
import com.gofobao.framework.starfire.tender.vo.response.UserTenderRes;
import com.gofobao.framework.starfire.util.AES;
import com.gofobao.framework.starfire.util.SignUtil;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by master on 2017/9/28.
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class StarFireTenderBizImpl implements StarFireTenderBiz {

    @Autowired
    private TenderService tenderService;

    @Value("${starfire.key}")
    private static String key;

    @Value("${starfire.initVector}")
    private static String initVector;

    @Autowired
    private BaseRequest baseRequest;

    private static Gson GSON = new Gson();

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowCollectionService borrowCollectionService;

    @Autowired
    private UserCacheService userCacheService;

    /**
     * 用户投资记录查询接口
     *
     * @param userTenderQuery
     * @return
     */
    @Override
    public UserTenderRes userTenderList(UserTenderQuery userTenderQuery) {
        log.info("==============星火用户投资记录===============");
        log.info("打印星火请求参数:" + GSON.toJson(userTenderQuery));
        //封装验签参数
        baseRequest.setT_code(userTenderQuery.getT_code());
        baseRequest.setC_code(userTenderQuery.getC_code());
        baseRequest.setSerial_num(userTenderQuery.getSerial_num());
        baseRequest.setSign(userTenderQuery.getSign());
        //封装返回参数
        UserTenderRes userTenderRes = new UserTenderRes();
        userTenderRes.setSerial_num(userTenderQuery.getSerial_num());
        try {
            //验签
            if (!SignUtil.checkSign(baseRequest, key, initVector)) {
                String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
                userTenderRes.setResult(code);
                userTenderRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                return userTenderRes;
            }
            //解密参数
            String userId = AES.decrypt(key, initVector, userTenderQuery.getPlatform_uid());
            String endAt = userTenderQuery.getEnd_time();
            String startAt = userTenderQuery.getStart_time();
            List<String> userList = Lists.newArrayList(userId.split(";"));
            //查询用户投资
            Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                    .in("userId", userList.toArray())
                    .eq("status", TenderConstans.SUCCESS)
                    .between(!StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(endAt),
                            "createdAt",
                            new Range<>(endAt, startAt))
                    .build();

            List<Tender> tenders = tenderService.findList(tenderSpecification,
                    new Sort(Sort.Direction.DESC,
                            "createdAt"));

            Set<Long> borrowIds = tenders.stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toSet());

            List<Borrow> borrows = borrowService.findByBorrowIds(new ArrayList<>(borrowIds));
            Map<Long, Borrow> borrowMap = borrows.stream()
                    .collect(Collectors.toMap(Borrow::getId,
                            Function.identity()));
            //用户投资记录为空直接返回
            if (CollectionUtils.isEmpty(tenders)) {
                return userTenderRes;
            }
            //装配数据
            Map<Long, List<Tender>> userTenderMaps = tenders.stream()
                    .collect(Collectors.groupingBy(Tender::getUserId));
            List<UserTenderRes.UserRecords> records = userTenderRes.getRecords();
            for (Long tenderUserId : userTenderMaps.keySet()) {
                Users users = userService.findById(tenderUserId);
                UserTenderRes.UserRecords userRecords = userTenderRes.new UserRecords();
                userRecords.setMobile(AES.encrypt(key, initVector, users.getPhone()));
                List<Tender> tendersList = userTenderMaps.get(tenderUserId);
                userRecords.setBidtotalCount(String.valueOf(tendersList.size()));
                userRecords.setPlatform_uid(users.getId().toString());
                userRecords.setMobile(users.getPhone());

                List<UserTenderRes.UserbidRecords> userbidRecordsList = Lists.newArrayList();
                //封装用户投资记录
                tendersList.forEach(p -> {
                    UserTenderRes.UserbidRecords userbidRecords = userTenderRes.new UserbidRecords();
                    Borrow borrow = borrowMap.get(p.getBorrowId());
                    userbidRecords.setBid_id(borrow.getId().toString());
                    userbidRecords.setProductBidId(p.getId().toString());
                    userbidRecords.setRate(StringHelper.formatMon(borrow.getApr() / 100D));
                    userbidRecords.setRaiseRate("0.00");
                    //查询回款列表
                    Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                            .eq("tenderId", p.getId())
                            .build();
                    List<BorrowCollection> borrowCollections = borrowCollectionService.findList(specification);
                    String tenderStuats = getTenderStuats(borrowCollections, p, borrow);
                    userbidRecords.setBidResult(tenderStuats);
                    BorrowCollection borrowCollection = borrowCollections.get(0);
                    //到期时间
                    if (tenderStuats.equals(StarFireBorrowConstant.YIZHUANGRANG)) {
                        userbidRecords.setExpireDate(DateHelper.dateToString(
                                p.getUpdatedAt(),
                                DateHelper.DATE_FORMAT_YMD));
                    } else {
                        userbidRecords.setExpireDate(DateHelper.dateToString(
                                borrowCollection.getCollectionAt(),
                                DateHelper.DATE_FORMAT_YMD));
                    }
                    //计息时间
                    if (!tenderStuats.equals(StarFireBorrowConstant.LIUBIAO)
                            && !tenderStuats.equals(StarFireBorrowConstant.WEIMIANBIAO)
                            && !tenderStuats.equals(StarFireBorrowConstant.SHENHEZHONG)) {
                        userbidRecords.setInterestDate(DateHelper.dateToString(borrow.getRecheckAt(),
                                DateHelper.DATE_FORMAT_YMD));
                    }
                    //投资金额
                    userbidRecords.setInvestAmount(StringHelper.formatDouble(p.getMoney() / 100D, false));
                    //是否可转让
                    userbidRecords.setCanAssign(borrow.getStatus() == BorrowContants.JING_ZHI ? "false" : "true");
                    //已获取收益
                    Long interestYes = borrowCollections.stream()
                            .filter(w -> w.getCollectionMoneyYes() > 0)
                            .mapToLong(s -> s.getInterest())
                            .sum();
                    userbidRecords.setProfitAmount(StringHelper.formatDouble(interestYes / 100D, false));
                    UserCache userCache = userCacheService.findById(p.getUserId());
                    //是否是首投
                    if (userCache.getTenderTuijian().intValue() == p.getId()
                            || userCache.getTenderQudao().intValue() == p.getId()) {
                        userbidRecords.setIsFirstInvest("true");
                    }
                    userbidRecordsList.add(userbidRecords);
                });
                records.add(userRecords);
            }
            userTenderRes.setRecords(records);
            return userTenderRes;
        } catch (Exception e) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            userTenderRes.setResult(code);
            userTenderRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return userTenderRes;
        }
    }

    /**
     * 标的回款信息查询接
     *
     * @param borrowCollectionRecords
     * @return
     */
    @Override
    public UserBorrowCollectionRecordsRes borrowCollections(BorrowCollectionRecords borrowCollectionRecords) {
        log.info("===============标的回款信息查询接==================");
        log.info("打印星火请求参数:" + GSON.toJson(borrowCollectionRecords));
        //封装验签参数
        baseRequest.setSign(borrowCollectionRecords.getSign());
        baseRequest.setSerial_num(borrowCollectionRecords.getSerial_num());
        baseRequest.setC_code(borrowCollectionRecords.getC_code());
        baseRequest.setT_code(borrowCollectionRecords.getT_code());
        String platformUid = borrowCollectionRecords.getPlatform_uid();
        //封装返回参数
        UserBorrowCollectionRecordsRes recordsRes = new UserBorrowCollectionRecordsRes();
        if (!SignUtil.checkSign(baseRequest, key, initVector)) {
            log.info("标的回款信息查询接验签失败");
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            recordsRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return recordsRes;
        }
        try {
            List<String> userIds;
            if (StringUtils.isEmpty(platformUid)) {
                Specification<Users> usersSpecification = Specifications.<Users>and()
                        .ne("starFireUserId", null)
                        .build();
                List<Users> usersList = userService.findList(usersSpecification);
                List<Long> userIdArray = usersList.stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());
                userIds = Lists.transform(userIdArray, Functions.toStringFunction());
            } else {
                userIds = Lists.newArrayList(platformUid.split(";"));
            }
            Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                    .in("userId", userIds.toArray())
                    .eq("status", TenderConstans.SUCCESS)
                    .build();

            List<Tender> tenders = tenderService.findList(tenderSpecification,
                    new Sort(Sort.Direction.DESC,
                            "createdAt"));
            //根据用户分组tender
            Map<Long, List<Tender>> usersTenderMaps = tenders.stream()
                    .collect(Collectors.groupingBy(Tender::getUserId));
            //去重borrowId
            List<Long> borrows = tenders.stream()
                    .map(p -> p.getBorrowId()).distinct()
                    .collect(Collectors.toList());
            //查询所有的标的集合
            List<Borrow> borrowList = borrowService.findByBorrowIds(borrows);
            Map<Long, Borrow> borrowMap = borrowList.stream()
                    .collect(Collectors.toMap(Borrow::getId,
                            Function.identity()));
            List<UserBorrowCollectionRecordsRes.Records> recordsList = Lists.newArrayList();

            for (Long userId : usersTenderMaps.keySet()) {
                List<Tender> tenderList = usersTenderMaps.get(userId);
                UserBorrowCollectionRecordsRes.Records records = recordsRes.new Records();
                Integer tenderSize = tenderList.size();
                records.setBidCount(tenderSize);
                records.setPlatform_uid(userId.toString());
                List<UserBorrowCollectionRecordsRes.BidRecords> bidRecords = new ArrayList<>(tenderSize);
                tenderList.forEach(p -> {
                    UserBorrowCollectionRecordsRes.BidRecords bidRecord = recordsRes.new BidRecords();
                    bidRecord.setBid_id(p.getBorrowId().toString());
                    bidRecord.setProductBidId(p.getId().toString());
                    Specification<BorrowCollection> borrowCollectionSpecification = Specifications.<BorrowCollection>and()
                            .eq("tenderId", p.getId())
                            .build();
                    List<BorrowCollection> borrowCollections = borrowCollectionService.findList(borrowCollectionSpecification);
                    Integer borrowCollectionSize = borrowCollections.size();
                    bidRecord.setBidRepayCount(borrowCollectionSize);
                    List<UserBorrowCollectionRecordsRes.BidRepayRecords> repayRecordsList = new ArrayList<>(borrowCollectionSize);
                    //已回款集合
                    List<BorrowCollection> borrowCollectionYes = borrowCollections.stream()
                            .filter(borrowCollection -> borrowCollection.getStatus() == BorrowCollectionContants.STATUS_YES)
                            .collect(Collectors.toList());
                    //累计还款本金
                    Long accruedRepayCapital = borrowCollectionYes.stream()
                            .mapToLong(temp -> temp.getPrincipal())
                            .sum();
                    //累计还款利息
                    Long accruedRepayInterest = borrowCollectionYes.stream()
                            .mapToLong(temp -> temp.getInterest() + temp.getLateInterest())
                            .sum();
                    //未回款集合
                    List<BorrowCollection> borrowCollectionNo = borrowCollections.stream()
                            .filter(borrowCollection -> borrowCollection.getStatus() == BorrowCollectionContants.STATUS_NO)
                            .collect(Collectors.toList());
                    //剩余还款本金
                    Long leftRepayCapital = borrowCollectionNo.stream()
                            .mapToLong(temp -> temp.getPrincipal())
                            .sum();
                    //剩余还款利息
                    Long leftRepayInterest = borrowCollectionNo.stream()
                            .mapToLong(temp -> temp.getLateInterest() + temp.getInterest())
                            .sum();
                    borrowCollections.forEach(w -> {
                        UserBorrowCollectionRecordsRes.BidRepayRecords bidRepayRecords = recordsRes.new BidRepayRecords();
                        bidRepayRecords.setRepayPeriods(w.getOrder() + 1);
                        bidRepayRecords.setCurrentRepayPeriod(w.getOrder() + 1);
                        bidRepayRecords.setRepayDate(DateHelper.dateToString(w.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
                        bidRepayRecords.setActualRepayTime(!StringUtils.isEmpty(w.getCollectionAtYes())
                                ? DateHelper.dateToString(w.getCollectionAt(), DateHelper.DATE_FORMAT_YMD)
                                : "");
                        bidRepayRecords.setCurrentRepayCapital(StringHelper.formatDouble(w.getPrincipal(), false));
                        bidRepayRecords.setCurrentRepayInterest(StringHelper.formatDouble(w.getInterest(), false));
                        bidRepayRecords.setAccruedRepayCapital(StringHelper.formatDouble(accruedRepayCapital / 100d, false));
                        bidRepayRecords.setAccruedRepayInterest(StringHelper.formatDouble(accruedRepayInterest / 100D, false));
                        bidRepayRecords.setLeftRepayCapital(StringHelper.formatDouble(leftRepayCapital / 100D, false));
                        bidRepayRecords.setLeftRepayInterest(StringHelper.formatDouble(leftRepayInterest / 100D, false));
                        bidRepayRecords.setRepayResult(w.getStatus());
                        bidRepayRecords.setRepayType(getBorrowCollectionStatus(w));
                        repayRecordsList.add(bidRepayRecords);
                    });
                });
                recordsList.add(records);
            }
            recordsRes.setRecords(recordsList);
            return recordsRes;
        } catch (Exception e) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            recordsRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            recordsRes.setResult(code);
            return recordsRes;
        }
    }

    /**
     * 获取投资状态
     *
     * @param borrowCollections
     * @param tender
     * @param borrow
     * @return
     */
    private String getTenderStuats(List<BorrowCollection> borrowCollections, Tender tender, Borrow borrow) {
        Integer borrowStatus = borrow.getStatus();  //标的状态
        Integer tenderState = tender.getState();
        Date nowDate = new Date();
        if (tenderState == TenderConstans.BIDDING) {
            if (borrowStatus == BorrowContants.CANCEL) {
                return StarFireBorrowConstant.LIUBIAO;
            } else if (borrow.getMoneyYes() < borrow.getMoney()) {
                return StarFireBorrowConstant.WEIMIANBIAO;
            } else {
                return StarFireBorrowConstant.SHENHEZHONG;
            }
            //是否转让
        } else if (tender.getTransferFlag() == TenderConstans.TRANSFER_YES
                || tender.getTransferFlag() == TenderConstans.TRANSFER_PART_YES) {
            return StarFireBorrowConstant.YIZHUANGRANG;
        } else if (tenderState == TenderConstans.BACK_MONEY) {
            boolean falg = false;
            for (BorrowCollection collection : borrowCollections) {
                if (collection.getStatus() == BorrowCollectionContants.STATUS_YES && collection.getLateDays() > 0) {
                    falg = true;
                    break;
                } else if (collection.getStatus() == BorrowCollectionContants.STATUS_NO) {
                    if (nowDate.getTime() < collection.getCollectionAt().getTime()) {
                        falg = true;
                        break;
                    }
                }
            }
            //还款中 逾期
            return falg ? StarFireBorrowConstant.YUQI : StarFireBorrowConstant.HUANKUANZHONG;
        } else {
            return StarFireBorrowConstant.YIJIEQING;
        }
    }

    /**
     * 获取回款状态
     *
     * @param borrowCollection
     * @return
     */
    private String getBorrowCollectionStatus(BorrowCollection borrowCollection) {

        //时间回款时间
        Date collectionAtYes = borrowCollection.getCollectionAtYes();
        //应还截至时间
        Date collectionAt = DateHelper.subHours(DateHelper.nextDate(borrowCollection.getCollectionAt()), 3);
        //还款日开始时间
        Date beginOfDate = DateHelper.beginOfDate(collectionAt);
        //已还款
        if (borrowCollection.getStatus() == BorrowCollectionContants.STATUS_YES) {

            if (collectionAtYes.getTime() > collectionAt.getTime()) {
                //实际还款时间大于应还截至时间  逾期还款
                return StarFireBorrowCollectionConstant.YUQIHUANKUAN;
            } else if (collectionAtYes.getTime() < beginOfDate.getTime()) {//实际还款日小于还款开始时间
                //提前还款
                return StarFireBorrowCollectionConstant.TIQIANHUANKUAN;
            } else {
                //正常还款
                return StarFireBorrowCollectionConstant.ZHENGCHANGHUANKUAN;
            }
        } else {
            Date nowDate = new Date();
            if (collectionAt.getTime() > nowDate.getTime()) {  //未到还款截至时间
                return "";
            } else  {
                return StarFireBorrowCollectionConstant.YUQI;
            }
        }
    }

}
