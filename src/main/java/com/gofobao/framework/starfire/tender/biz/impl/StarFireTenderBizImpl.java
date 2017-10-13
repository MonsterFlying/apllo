package com.gofobao.framework.starfire.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.contants.RepaymentContants;
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
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.gofobao.framework.starfire.common.response.CodeTypeConstant;
import com.gofobao.framework.starfire.common.response.ResultCodeEnum;
import com.gofobao.framework.starfire.common.response.ResultCodeMsgEnum;
import com.gofobao.framework.starfire.tender.biz.StarFireTenderBiz;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowCollectionConstant;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowConstant;
import com.gofobao.framework.starfire.tender.constants.StarFireBorrowRepaymentConstant;
import com.gofobao.framework.starfire.tender.vo.request.BorrowCollectionRecords;
import com.gofobao.framework.starfire.tender.vo.request.BorrowRepaymentQuery;
import com.gofobao.framework.starfire.tender.vo.request.UserTenderQuery;
import com.gofobao.framework.starfire.tender.vo.response.BidRepaymentInfoRes;
import com.gofobao.framework.starfire.tender.vo.response.UserBorrowCollectionRecordsRes;
import com.gofobao.framework.starfire.tender.vo.response.UserTenderRes;
import com.gofobao.framework.starfire.util.AES;
import com.gofobao.framework.starfire.util.SignUtil;
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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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
    private String key;

    @Value("${starfire.initVector}")
    private String initVector;

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

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    @PersistenceContext
    private EntityManager entityManager;


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
            String endAt = userTenderQuery.getEnd_time();
            String startAt = userTenderQuery.getStart_time();
            String userIdStr = userTenderQuery.getPlatform_uid();
            List<String> userIdsList = null;
            if (!StringUtils.isEmpty(userIdStr)) {
                userIdsList = Lists.newArrayList(AES.decrypt(key, initVector, userIdStr).split(","));
            } else {
                Specification<Users> usersSpecification = Specifications.<Users>and()
                        .ne("starFireUserId", null)
                        .ne("starFireRegisterToken",null)
                        .build();
                List<Users> usersList = userService.findList(usersSpecification);
                List<Long> userIds = usersList.stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());
                userIdsList = Lists.transform(userIds, Functions.toStringFunction());
            }

            if(CollectionUtils.isEmpty(userIdsList)){
                userTenderRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
                return userTenderRes;
            }
            //查询用户投资
            Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                    .in("userId", userIdsList.toArray())
                    .eq("status", TenderConstans.SUCCESS)
                    .between(!StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(endAt),
                            "createdAt",
                            new Range<>(DateHelper.stringToDate(startAt), DateHelper.stringToDate(endAt)))
                    .build();
            List<Tender> tenders = tenderService.findList(tenderSpecification,
                    new Sort(Sort.Direction.DESC,
                            "createdAt"));

            //用户投资记录为空直接返回
            if (CollectionUtils.isEmpty(tenders)) {
                userTenderRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
                return userTenderRes;
            }

            Set<Long> borrowIds = tenders.stream()
                    .map(p -> p.getBorrowId())
                    .collect(Collectors.toSet());
            List<Borrow> borrows = borrowService.findByBorrowIds(new ArrayList<>(borrowIds));
            Map<Long, Borrow> borrowMap = borrows.stream()
                    .collect(Collectors.toMap(Borrow::getId,
                            Function.identity()));

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
                    //到期时间
                    if (tenderStuats.equals(StarFireBorrowConstant.YIZHUANGRANG)
                            || tenderStuats.equals(StarFireBorrowConstant.TIQIANJIEQING)) {
                        userbidRecords.setExpireDate(DateHelper.dateToString(
                                p.getUpdatedAt(),
                                DateHelper.DATE_FORMAT_YMD));
                    } else {
                        BorrowCollection lastBorrowCollection = borrowCollections.stream()
                                .max(comparing(temp -> temp.getCollectionAt()))
                                .get();
                        userbidRecords.setExpireDate(DateHelper.dateToString(
                                lastBorrowCollection.getCollectionAt(),
                                DateHelper.DATE_FORMAT_YMD));
                    }
                    //计息时间
                    userbidRecords.setInterestDate(!ObjectUtils.isEmpty(borrow.getRecheckAt())
                            ? DateHelper.dateToString(borrow.getRecheckAt(), DateHelper.DATE_FORMAT_YMD)
                            : "");
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
                    userbidRecords.setInvestTime(DateHelper.dateToString(p.getCreatedAt()));
                    userbidRecordsList.add(userbidRecords);
                });
                userRecords.setUserbidrecords(userbidRecordsList);
                records.add(userRecords);
            }
            userTenderRes.setTotalCount(userTenderMaps.size());
            userTenderRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            userTenderRes.setRecords(records);
            return userTenderRes;
        } catch (Exception e) {
            log.error("查询星火用户投资记录失败,打印错误信息：", e);
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
        recordsRes.setSerial_num(borrowCollectionRecords.getSerial_num());
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
                        .ne("starFireRegisterToken",null)
                        .build();
                List<Users> usersList = userService.findList(usersSpecification);
                List<Long> userIdArray = usersList.stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());
                userIds = Lists.transform(userIdArray, Functions.toStringFunction());
            } else {
                userIds = Lists.newArrayList(AES.decrypt(key, initVector, platformUid).split(","));
            }

            if(CollectionUtils.isEmpty(userIds)){
                recordsRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
                return recordsRes;
            }
            Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                    .in("userId", userIds.toArray())
                    .eq("status", TenderConstans.SUCCESS)
                    .build();
            List<Tender> tenders = tenderService.findList(tenderSpecification,
                    new Sort(Sort.Direction.DESC,
                            "createdAt"));
            //用户投资记录为空直接返回
            if (CollectionUtils.isEmpty(tenders)) {
                recordsRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
                return recordsRes;
            }
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
                for (Tender tender : tenderList) {
                    UserBorrowCollectionRecordsRes.BidRecords bidRecord = recordsRes.new BidRecords();
                    bidRecord.setBid_id(tender.getBorrowId().toString());
                    bidRecord.setProductBidId(tender.getId().toString());
                    Specification<BorrowCollection> borrowCollectionSpecification = Specifications.<BorrowCollection>and()
                            .eq("tenderId", tender.getId())
                            .build();
                    List<BorrowCollection> borrowCollections = borrowCollectionService.findList(borrowCollectionSpecification,
                            new Sort(Sort.Direction.ASC, "order"));
                    Integer borrowCollectionSize = borrowCollections.size();
                    bidRecord.setBidRepayCount(borrowCollectionSize);
                    List<UserBorrowCollectionRecordsRes.RepayRecords> repayRecordsList = new ArrayList<>(borrowCollectionSize);
                    //总剩余还款本金
                    Long sumLeftRepayCapital = borrowCollections.stream()
                            .mapToLong(borrowCollection -> borrowCollection.getPrincipal())
                            .sum();
                    //总剩余还款利息
                    Long sumLeftRepayInterest = borrowCollections.stream()
                            .mapToLong(borrowCollection -> borrowCollection.getInterest())
                            .sum();
                    //累计还款本金
                    Long accruedRepayCapital = 0L;
                    //累计还款利息
                    Long accruedRepayInterest = 0L;
                    Integer orderCount = borrowCollections.size();
                    for (BorrowCollection borrowCollection : borrowCollections) {
                        UserBorrowCollectionRecordsRes.RepayRecords bidRepayRecords = recordsRes.new RepayRecords();
                        bidRepayRecords.setRepayPeriods(orderCount);
                        bidRepayRecords.setCurrentRepayPeriod(borrowCollection.getOrder() + 1);
                        bidRepayRecords.setRepayDate(DateHelper.dateToString(borrowCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
                        bidRepayRecords.setActualRepayTime(!StringUtils.isEmpty(borrowCollection.getCollectionAtYes())
                                ? DateHelper.dateToString(borrowCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD)
                                : "");
                        Long principal = borrowCollection.getPrincipal();
                        Long interest = borrowCollection.getInterest();
                        bidRepayRecords.setCurrentRepayCapital(StringHelper.formatDouble(principal / 100D, false));
                        bidRepayRecords.setCurrentRepayInterest(StringHelper.formatDouble(interest / 100D, false));
                        //已还
                        if (borrowCollection.getStatus().intValue() == BorrowCollectionContants.STATUS_YES.intValue()) {
                            sumLeftRepayCapital -= principal;    //累计剩余本金
                            sumLeftRepayInterest -= interest; //累计剩余利息
                            accruedRepayCapital += principal; //累计还款本金
                            accruedRepayInterest += interest; //累计还款利息
                        }
                        bidRepayRecords.setAccruedRepayCapital(StringHelper.formatDouble(accruedRepayCapital / 100D, false));
                        bidRepayRecords.setAccruedRepayInterest(StringHelper.formatDouble(accruedRepayInterest / 100D, false));
                        bidRepayRecords.setLeftRepayCapital(StringHelper.formatDouble(sumLeftRepayCapital / 100D, false));
                        bidRepayRecords.setLeftRepayInterest(StringHelper.formatDouble(sumLeftRepayInterest / 100D, false));
                        bidRepayRecords.setRepayResult(borrowCollection.getStatus());
                        bidRepayRecords.setRepayType(getBorrowCollectionStatus(borrowCollection));
                        repayRecordsList.add(bidRepayRecords);
                    }
                    ;
                    bidRecord.setBidRepayRecords(repayRecordsList);
                    bidRecords.add(bidRecord);
                }
                ;
                records.setBidRecords(bidRecords);
                recordsList.add(records);
            }
            recordsRes.setTotalCount(usersTenderMaps.size());
            recordsRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            recordsRes.setRecords(recordsList);
            return recordsRes;
        } catch (Exception e) {
            log.error("查询标的回款信息查询接失败,打印错误信息：", e);
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            recordsRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            recordsRes.setResult(code);
            return recordsRes;
        }
    }


    /**
     * 标的回款信息查询接口
     *
     * @param borrowRepaymentQuery
     * @return
     */
    @Override
    public BidRepaymentInfoRes repaymentList(BorrowRepaymentQuery borrowRepaymentQuery) {
        log.info("==============标的回款信息查询接口===============");
        log.info("打印星火请求参数:" + GSON.toJson(borrowRepaymentQuery));
        //封装验签参数
        baseRequest.setT_code(borrowRepaymentQuery.getT_code());
        baseRequest.setC_code(borrowRepaymentQuery.getC_code());
        baseRequest.setSerial_num(borrowRepaymentQuery.getSerial_num());
        baseRequest.setSign(borrowRepaymentQuery.getSign());
        //封装放回参数
        BidRepaymentInfoRes bidRepaymentInfoRes = new BidRepaymentInfoRes();
        if (!SignUtil.checkSign(baseRequest, key, initVector)) {
            log.info("标的回款信息查询接口接验签失败");
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            bidRepaymentInfoRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return bidRepaymentInfoRes;
        }
        String bidStr = borrowRepaymentQuery.getBid_id();
        List<String> borrowIds = null;
        if (StringUtils.isEmpty(bidStr)) {
            Query query = entityManager.createQuery(
                    "SELECT b FROM  Borrow b " +
                        "WHERE " +
                            "b.recheckAt>'2017-09-01'" +
                        " AND " +
                            "isWindmill=1");
            List<Borrow> borrows = query.getResultList();
            borrowIds = borrows.stream()
                    .map(p -> p.getId().toString())
                    .collect(Collectors.toList());
        } else {
            borrowIds = Lists.newArrayList(AES.decrypt(key,initVector,bidStr).split(","));
        }
        Specification<BorrowRepayment> borrowRepaymentSpecification = Specifications.<BorrowRepayment>and()
                .in("borrowId", borrowIds.toArray())
                .build();
        List<BorrowRepayment> borrowRepayments = borrowRepaymentService.findList(borrowRepaymentSpecification,
                new Sort(Sort.Direction.DESC,
                        "id"));

        if (CollectionUtils.isEmpty(borrowRepayments)) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS);
            bidRepaymentInfoRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return bidRepaymentInfoRes;
        }
        try {
            Map<Long, List<BorrowRepayment>> borrowRepaymentMaps = borrowRepayments.stream()
                    .collect(Collectors.groupingBy(BorrowRepayment::getBorrowId));
            List<BidRepaymentInfoRes.Records> records = new ArrayList<>();
            for (Long borrowId : borrowRepaymentMaps.keySet()) {
                BidRepaymentInfoRes.Records record = bidRepaymentInfoRes.new Records();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentMaps.get(borrowId);
                record.setBid_id(borrowId.toString());
                Integer orderCount = borrowRepaymentList.size();
                record.setRepayCounts(orderCount);

                Long sumLeftRepayInterest = borrowRepaymentList.stream()
                        .mapToLong(p -> p.getInterest())
                        .sum();
                Long sumLeftRepayCapital = borrowRepaymentList.stream()
                        .mapToLong(p -> p.getPrincipal())
                        .sum();
                Long accruedRepayCapital = 0L;
                Long accruedRepayInterest = 0L;

                List<BidRepaymentInfoRes.RepayRecords> repayRecords = new ArrayList<>(orderCount);
                borrowRepaymentList.sort(Comparator.comparing(s->s.getRepayAt()));
                for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                    BidRepaymentInfoRes.RepayRecords repayRecord = bidRepaymentInfoRes.new RepayRecords();
                    repayRecord.setRepayPeriods(orderCount);
                    repayRecord.setCurrentRepayPeriod(borrowRepayment.getOrder() + 1);
                    repayRecord.setRepayDate(DateHelper.dateToString(borrowRepayment.getRepayAt(), DateHelper.DATE_FORMAT_YMD));
                    if (!StringUtils.isEmpty(borrowRepayment.getRepayAtYes())) {
                        repayRecord.setActualRepayTime(DateHelper.dateToString(borrowRepayment.getRepayAtYes(), DateHelper.DATE_FORMAT_YMD));
                    } else if (!StringUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
                        repayRecord.setActualRepayTime(DateHelper.dateToString(borrowRepayment.getAdvanceAtYes(), DateHelper.DATE_FORMAT_YMD));
                    }
                    Long interest = borrowRepayment.getInterest();
                    Long principal = borrowRepayment.getPrincipal();
                    if (borrowRepayment.getStatus().intValue() == RepaymentContants.STATUS_YES) {
                        sumLeftRepayInterest -= interest;
                        sumLeftRepayCapital -= principal;
                        accruedRepayInterest += interest;
                        accruedRepayCapital += principal;
                    }
                    repayRecord.setLeftRepayCapital(StringHelper.formatDouble(sumLeftRepayCapital, 100, false));
                    repayRecord.setLeftRepayInterest(StringHelper.formatDouble(sumLeftRepayInterest, 100, false));
                    repayRecord.setAccruedRepayInterest(StringHelper.formatDouble(accruedRepayInterest, 100, false));
                    repayRecord.setAccruedRepayCapital(StringHelper.formatDouble(accruedRepayCapital, 100, false));
                    repayRecord.setCurrentRepayInterest(StringHelper.formatDouble(interest, 100, false));
                    repayRecord.setCurrentRepayCapital(StringHelper.formatDouble(principal, 100, false));
                    repayRecord.setRepayResult(borrowRepayment.getStatus());
                    repayRecord.setRepayType(getRepaymentStuats(borrowRepayment));
                    repayRecords.add(repayRecord);
                    record.setRepayRecords(repayRecords);
                };
                records.add(record);
            }
            bidRepaymentInfoRes.setRecords(records);
            bidRepaymentInfoRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            bidRepaymentInfoRes.setTotalCount(borrowRepaymentMaps.size());
            return bidRepaymentInfoRes;
        } catch (Exception e) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS);
            bidRepaymentInfoRes.setResult(ResultCodeMsgEnum.getResultMsg(code));
            return bidRepaymentInfoRes;
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
        if (tenderState.intValue() == TenderConstans.BIDDING.intValue()) {
            if (borrowStatus.intValue() == BorrowContants.CANCEL.intValue()) {
                return StarFireBorrowConstant.LIUBIAO;
            } else if (borrow.getMoneyYes() < borrow.getMoney()) {
                return StarFireBorrowConstant.WEIMIANBIAO;
            } else {
                return StarFireBorrowConstant.SHENHEZHONG;
            }
            //是否转让
        } else if (tender.getTransferFlag().intValue() == TenderConstans.TRANSFER_YES.intValue()
                || tender.getTransferFlag().intValue() == TenderConstans.TRANSFER_PART_YES.intValue()) {
            return StarFireBorrowConstant.YIZHUANGRANG;
        } else if (tenderState.intValue() == TenderConstans.BACK_MONEY.intValue()) {
            BorrowCollection lastBorrowCollection = borrowCollections.stream()
                    .max(comparing(temp -> temp.getCollectionAt()))
                    .get();
            //如果当前时间大于应还时间
            if (nowDate.getTime() > lastBorrowCollection.getCollectionAt().getTime())
                return StarFireBorrowConstant.YUQI;
            else
                return StarFireBorrowConstant.HUANKUANZHONG;
        } else {
            BorrowCollection lastBorrowCollection = borrowCollections.stream()
                    .max(comparing(temp -> temp.getCollectionAt()))
                    .get();
            //逾期天数
            if (lastBorrowCollection.getLateDays().intValue() > 0) {
                return StarFireBorrowConstant.YUQIHUANKUAN;
                //实际还款日为 应还日的前一天
            } else if (lastBorrowCollection.getCollectionAtYes().getTime()
                    < DateHelper.beginOfDate(lastBorrowCollection.getCollectionAt()).getTime()) {
                return StarFireBorrowConstant.TIQIANJIEQING;
            } else {
                return StarFireBorrowConstant.YIJIEQING;
            }
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
        if (borrowCollection.getStatus().intValue() == BorrowCollectionContants.STATUS_YES.intValue()) {
            //逾期天数
            if (borrowCollection.getLateDays() > 0) {
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
            } else {
                return StarFireBorrowCollectionConstant.YUQI;
            }
        }
    }

    /**
     * 还款状态
     *
     * @param borrowRepayment
     * @return
     */
    private String getRepaymentStuats(BorrowRepayment borrowRepayment) {
        if (borrowRepayment.getStatus().intValue() == RepaymentContants.STATUS_YES.intValue()) {
            Date beginRepayAt = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
            if (borrowRepayment.getLateDays() > 0) {
                return StarFireBorrowRepaymentConstant.YUQIHUANKUAN;
            } else if (borrowRepayment.getRepayAtYes().getTime() < beginRepayAt.getTime()) {
                return StarFireBorrowRepaymentConstant.TIQIANHUANKUAN;
            } else {
                return StarFireBorrowRepaymentConstant.ZHENGCHANGHUANKUAN;
            }
        } else {
            Date endRepayAt = DateHelper.subHours(DateHelper.endOfDate(borrowRepayment.getRepayAt()), 3);
            if (new Date().getTime() > endRepayAt.getTime()) {
                return StarFireBorrowRepaymentConstant.YUQI;
            }
        }
        return "";
    }
}
