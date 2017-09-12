package com.gofobao.framework.repayment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.repayment.vo.request.VoOrderListReq;
import com.gofobao.framework.repayment.vo.response.RepayCollectionLog;
import com.gofobao.framework.repayment.vo.response.RepaymentOrderDetail;
import com.gofobao.framework.repayment.vo.response.pc.VoCollection;
import com.gofobao.framework.repayment.vo.response.pc.VoOrdersList;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/1.
 */

@SuppressWarnings("all")
@Component
public class BorrowRepaymentServiceImpl implements BorrowRepaymentService {

    @Autowired
    private BorrowRepaymentRepository borrowRepaymentRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransferService transferService;

    @Autowired
    private ThirdBatchLogService thirdBatchLogService;


    /**
     * 还款计划列表
     *
     * @param voCollectionOrderReq
     * @return VoViewCollectionOrderListRes
     */
    @Override
    public List<BorrowRepayment> repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.beginOfDate(DateHelper.stringToDate(voCollectionOrderReq.getTime(), DateHelper.DATE_FORMAT_YMD));
        Specification<BorrowRepayment> specification = Specifications.<BorrowRepayment>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("repayAt", new Range<>(date, DateHelper.endOfDate(date)))
                .build();
        return borrowRepaymentRepository.findAll(specification);

    }

    /**
     * pc:还款计划
     *
     * @param orderListReq
     * @return
     */
    @Override
    public Map<String, Object> pcOrderList(VoOrderListReq orderListReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();
        //总记录数
        String totalSql = "select count(b.id) from BorrowRepayment AS b where b.userId=:userId and b.status=0  GROUP BY date_format(b.repayAt,'%Y-%m-%d') ";
        Query nativeQuery = entityManager.createQuery(totalSql, Long.class);
        nativeQuery.setParameter("userId", orderListReq.getUserId());
        List<Long> totalCount = nativeQuery.getResultList();
        resultMaps.put("totalCount", totalCount.size());

        //分页
        String sql = "select date_format(b.repayAt,'%Y-%m-%d'), SUM (b.repayMoney),SUM(b.principal),SUM(b.interest),COUNT(b.id) FROM BorrowRepayment b where b.userId=:userId and b.status=0 GROUP BY date_format(b.repayAt,'%Y-%m-%d') ORDER BY  b.repayAt ASC";
        Query query = entityManager.createQuery(sql);
        query.setFirstResult(orderListReq.getPageIndex() * orderListReq.getPageSize());
        query.setMaxResults(orderListReq.getPageSize());
        query.setParameter("userId", orderListReq.getUserId());
        List resultList = query.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            resultMaps.put("orderList", new ArrayList<>());
            return resultMaps;
        }
        List<VoOrdersList> ordersLists = Lists.newArrayList();
        //装配结果集
        resultList.stream().forEach(p -> {
            VoOrdersList item = new VoOrdersList();
            Object[] objects = (Object[]) p;
            item.setTime((String) objects[0]);
            item.setCollectionMoney(StringHelper.formatMon((Long) objects[1] / 100D));
            item.setPrincipal(StringHelper.formatMon((Long) objects[2] / 100D));
            item.setInterest(StringHelper.formatMon((Long) objects[3] / 100D));
            item.setOrderCount((Long) objects[4]);
            ordersLists.add(item);
        });
        resultMaps.put("orderList", ordersLists);
        return resultMaps;
    }

    /**
     * pc:还款计划导出到excel
     *
     * @param orderListReq
     * @return
     */
    @Override
    public List<VoOrdersList> toExcel(VoOrderListReq orderListReq) {

        String sql = "select date_format(b.repayAt,'%Y-%m-%d'), SUM (b.repayMoney),SUM(b.principal),SUM(b.interest),COUNT(b.id) FROM BorrowRepayment b where b.userId=:userId and b.status=0 GROUP BY date_format(b.repayAt,'%Y-%m-%d') ORDER BY  b.repayAt ASC";
        Query query = entityManager.createQuery(sql);

        query.setParameter("userId", orderListReq.getUserId());
        List resultList = query.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.EMPTY_LIST;
        }
        List<VoOrdersList> ordersLists = Lists.newArrayList();
        //装配结果集
        resultList.stream().forEach(p -> {
            VoOrdersList item = new VoOrdersList();
            Object[] objects = (Object[]) p;
            item.setTime((String) objects[0]);
            item.setCollectionMoney(StringHelper.formatMon((Long) objects[1] / 100D));
            item.setPrincipal(StringHelper.formatMon((Long) objects[2] / 100D));
            item.setInterest(StringHelper.formatMon((Long) objects[3] / 100D));
            item.setOrderCount((Long) objects[4]);
            ordersLists.add(item);
        });
        return ordersLists;
    }


    /**
     * pc：还款详情
     *
     * @param orderReq
     * @return
     */
    @Override
    public Map<String, Object> collectionList(VoCollectionListReq orderReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();

        String time = orderReq.getTime();
        Date beginAt = DateHelper.beginOfDate(DateHelper.stringToDate(time, DateHelper.DATE_FORMAT_YMD));
        Date endAt = DateHelper.endOfDate(DateHelper.stringToDate(time, DateHelper.DATE_FORMAT_YMD));

        Specification specification = Specifications.<BorrowRepayment>and()
                .eq("userId", orderReq.getUserId())
                .between("repayAt", new Range<>(beginAt, endAt))
                .eq("status", RepaymentContants.STATUS_NO)
                .build();
        Page<BorrowRepayment> borrowRepaymentPage = borrowRepaymentRepository.findAll(specification, new PageRequest(orderReq.getPageIndex(), orderReq.getPageSize(), new Sort(Sort.Direction.ASC, "repayAt")));
        Long totalCount = borrowRepaymentPage.getTotalElements();
        resultMaps.put("totalCount", totalCount);

        List<BorrowRepayment> repaymentList = borrowRepaymentPage.getContent();
        if (CollectionUtils.isEmpty(repaymentList)) {
            resultMaps.put("repaymentList", new ArrayList<>());
            return resultMaps;
        }
        //标集合
        Set<Long> borrowIds = repaymentList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        //装配结果集
        List<VoCollection> collections = Lists.newArrayList();
        repaymentList.stream().forEach(p -> {
            Specification<ThirdBatchLog> thirdBatchLogSpecification = Specifications.<ThirdBatchLog>and()
                    .eq("type", ThirdBatchLogContants.BATCH_REPAY)
                    .eq("sourceId", p.getId())
                    .in("state",Lists.newArrayList(0,1).toArray())
                    .build();
            List<ThirdBatchLog> thirdBatchLogs = thirdBatchLogService.findList(thirdBatchLogSpecification);

            VoCollection collection = new VoCollection();
            collection.setOrder(p.getOrder() + 1);
            Borrow borrow = borrowMap.get(p.getBorrowId());
            collection.setTimeLimit(borrow.getTimeLimit());
            collection.setLend(!StringUtils.isEmpty(borrow.getLendId()) ? true : false);
            collection.setRepayAt(!StringUtils.isEmpty(borrow.getLendId()) ? DateHelper.dateToString(p.getRepayAt(), DateHelper.DATE_FORMAT_YMD) : DateHelper.dateToString(p.getRepayAt()));
            collection.setRepaymentId(p.getId());
            collection.setStatus(p.getStatus()==0?!CollectionUtils.isEmpty(thirdBatchLogs)?2:p.getStatus():p.getStatus());
            collection.setBorrowName(borrow.getName());
            collection.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            collection.setInterest(StringHelper.formatMon(p.getInterest() / 100D));
            collections.add(collection);
        });
        resultMaps.put("repaymentList", collections);
        return resultMaps;
    }

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return VoViewOrderDetailResp
     */
    @Override
    public RepaymentOrderDetail detail(VoInfoReq voInfoReq) {
        RepaymentOrderDetail detailRes = new RepaymentOrderDetail();
        Specification<BorrowRepayment> specification = Specifications.<BorrowRepayment>and()
                .eq("userId", voInfoReq.getUserId())
                .eq("id", voInfoReq.getRepaymentId())
                .build();
        BorrowRepayment borrowRepayment = borrowRepaymentRepository.findOne(specification);
        if (ObjectUtils.isEmpty(borrowRepayment)) {
            return detailRes;
        }
        Long borrowId = borrowRepayment.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        Long interest = 0L;
        Long principal = 0L;
        if (borrowRepayment.getStatus() == RepaymentContants.STATUS_NO) {
            detailRes.setRepayAt(DateHelper.dateToString(borrowRepayment.getRepayAt(), DateHelper.DATE_FORMAT_YMD));
            detailRes.setStatusStr(RepaymentContants.STATUS_NO_STR);
        } else {
            interest = borrowRepayment.getInterest();
            principal = borrowRepayment.getPrincipal();
            detailRes.setStatusStr(RepaymentContants.STATUS_YES_STR);
            detailRes.setRepayAt(DateHelper.dateToString(borrowRepayment.getRepayAtYes(), DateHelper.DATE_FORMAT_YMD));
        }

        detailRes.setStatus(borrowRepayment.getStatus());
        Specification<ThirdBatchLog> thirdBatchLogSpecification = Specifications.<ThirdBatchLog>and()
                .eq("type", ThirdBatchLogContants.BATCH_REPAY)
                .eq("sourceId", borrowRepayment.getId())
                .in("state",Lists.newArrayList(0,1).toArray())
                .build();
        Date nowDate = new Date();   //当前时间
        Date tempRepayAt = DateHelper.nextDate(borrowRepayment.getRepayAt());  //还款日（第二天0点之前都算 还款日）
        Integer lateDays = 0;
        //当前已还款直接取逾期天数||应还时间大于当前时间(还未到还款日)
        if (borrowRepayment.getStatus() == RepaymentContants.STATUS_YES || tempRepayAt.getTime() > nowDate.getTime()) {
            lateDays = borrowRepayment.getLateDays();
            //当前是否是垫付
        } else if (!StringUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            lateDays = DateHelper.diffInDays(DateHelper.nextDate(borrowRepayment.getAdvanceAtYes()), tempRepayAt, false);
            //当前时间大于还款日（逾期)&&未还款
        } else if(nowDate.getTime()>tempRepayAt.getTime()&&borrowRepayment.getStatus()==RepaymentContants.STATUS_NO){
            lateDays = DateHelper.diffInDays(nowDate, tempRepayAt, false);
        }
        detailRes.setLateDays(lateDays);
        List<ThirdBatchLog> thirdBatchLogs = thirdBatchLogService.findList(thirdBatchLogSpecification);
        if (!CollectionUtils.isEmpty(thirdBatchLogs)) {
            detailRes.setStatus(2);  //还款复审中
            detailRes.setStatusStr("还款复审中");
        }
        detailRes.setInterest(StringHelper.formatMon(interest / 100d));
        detailRes.setPrincipal(StringHelper.formatMon(principal / 100d));
        detailRes.setBorrowName(borrow.getName());
        detailRes.setCollectionMoney(StringHelper.formatMon(borrowRepayment.getRepayMoney() / 100d));
        detailRes.setOrder(borrowRepayment.getOrder() + 1);

        return detailRes;
    }

    /**
     * 当月有还款日期
     *
     * @param userId
     * @param time
     * @return
     */
    @Override
    public List<Integer> days(Long userId, String time) {   // TODO 尝试不需要 SQL 来进行处理
        String sql = "SELECT DAY(repay_at) FROM gfb_borrow_repayment " +
                "where " +
                "user_id=" + userId + " " +
                "and   date_format(repay_at,'%Y%m') =" + time +
                " GROUP BY  day(repay_at)";
        Query query = entityManager.createNativeQuery(sql);
        List result = query.getResultList();
        return result;
    }

    /**
     * 标的还款记录
     *
     * @param transferId
     * @return
     */
    @Override
    public List<RepayCollectionLog> logs(Long borrowId) {
        List<BorrowRepayment> repaymentList = borrowRepaymentRepository.findByBorrowId(borrowId);
        if (CollectionUtils.isEmpty(repaymentList)) {
            return Collections.EMPTY_LIST;
        }
        List<RepayCollectionLog> logList = Lists.newArrayList();
        repaymentList.stream().forEach(p -> {
            RepayCollectionLog log = new RepayCollectionLog();
            log.setInterest(StringHelper.formatMon(p.getInterest() / 100d));
            log.setLateInterest(StringHelper.formatMon(p.getLateDays() / 100d));
            log.setOrder(p.getOrder() + 1);
            log.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100d));
            log.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            log.setRepayMoney(StringHelper.formatMon(p.getRepayMoney() / 100d));
            log.setRepayMoneyYes(StringHelper.formatMon(p.getRepayMoneyYes() / 100d));
            if (p.getStatus() == RepaymentContants.STATUS_NO) { //未还款
                log.setRepayAtYes("---");
                log.setRemark("---");
            } else {
                String date = DateHelper.dateToString(p.getRepayAtYes());
                log.setRepayAtYes(date);
                log.setRemark(date + RepaymentContants.STATUS_YES_STR);
            }
            logList.add(log);
        });
        return logList;
    }

    public BorrowRepayment save(BorrowRepayment borrowRepayment) {
        return borrowRepaymentRepository.save(borrowRepayment);
    }

    public List<BorrowRepayment> save(List<BorrowRepayment> borrowRepaymentList) {
        return borrowRepaymentRepository.save(borrowRepaymentList);
    }

    public BorrowRepayment insert(BorrowRepayment borrowRepayment) {
        if (ObjectUtils.isEmpty(borrowRepayment)) {
            return null;
        }
        borrowRepayment.setId(null);
        return borrowRepaymentRepository.save(borrowRepayment);
    }

    public BorrowRepayment updateById(BorrowRepayment borrowRepayment) {
        if (ObjectUtils.isEmpty(borrowRepayment) || ObjectUtils.isEmpty(borrowRepayment.getId())) {
            return null;
        }
        return borrowRepaymentRepository.save(borrowRepayment);
    }


    public BorrowRepayment findByIdLock(Long id) {
        return borrowRepaymentRepository.findById(id);
    }

    public BorrowRepayment findById(Long id) {
        return borrowRepaymentRepository.findOne(id);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification) {
        return borrowRepaymentRepository.findAll(specification);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Sort sort) {
        return borrowRepaymentRepository.findAll(specification, sort);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Pageable pageable) {
        return borrowRepaymentRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<BorrowRepayment> specification) {
        return borrowRepaymentRepository.count(specification);
    }

}
