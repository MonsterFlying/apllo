package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.contants.TransferContants;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.repository.TransferBuyLogRepository;
import com.gofobao.framework.tender.repository.TransferRepository;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.TransferMay;
import com.gofobao.framework.tender.vo.response.TransferOf;
import com.gofobao.framework.tender.vo.response.Transfered;
import com.gofobao.framework.tender.vo.response.web.TransferBuy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/6/12.
 */
@Component
public class TransferServiceImpl implements TransferService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private TransferRepository transferRepository;


    @Autowired
    private TransferBuyLogRepository transferBuyLogService;

    @PersistenceContext
    private EntityManager entityManager;

    public Transfer save(Transfer transfer) {
        return transferRepository.save(transfer);
    }

    public List<Transfer> save(List<Transfer> transferList) {
        return transferRepository.save(transferList);
    }

    public List<Transfer> findList(Specification<Transfer> specification) {
        return transferRepository.findAll(specification);
    }

    public List<Transfer> findList(Specification<Transfer> specification, Sort sort) {
        return transferRepository.findAll(specification, sort);
    }

    public List<Transfer> findList(Specification<Transfer> specification, Pageable pageable) {
        return transferRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<Transfer> specification) {
        return transferRepository.count(specification);
    }

    public Transfer findById(long id) {
        return transferRepository.getOne(id);
    }

    public Transfer findByIdLock(long id) {
        return transferRepository.findById(id);
    }

    /**
     * 转让中
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public Map<String, Object> transferOfList(VoTransferReq voTransferReq) {
        voTransferReq.setStatus(TransferContants.CHECKPENDING);
        Map<String, Object> resultMaps = commonQueryTemp(voTransferReq);
        List<Transfer> tenderList = (List<Transfer>) resultMaps.get("transfers");
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("transferOfList", new ArrayList<>());
            return resultMaps;
        }
        //标集合
        Set<Long> borrowIds = tenderList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

        List<TransferOf> transferOfs = Lists.newArrayList();
        tenderList.forEach(p -> {
            TransferOf transferOf = new TransferOf();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            transferOf.setTransferId(p.getId());
            transferOf.setName(borrow.getName());
            transferOf.setSpend(StringHelper.formatMon((p.getTransferMoneyYes() / 100D) / (p.getTransferMoney() / 100D)));
            transferOf.setApr(StringHelper.formatMon(p.getApr() / 100D));
            transferOf.setCancel(p.getIsLock());
            transferOf.setCreateTime(DateHelper.dateToString(p.getCreatedAt()));
            transferOf.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            transferOfs.add(transferOf);
        });
        resultMaps.put("transferOfList", tenderList);
        return resultMaps;
    }

    /**
     * 已转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public Map<String, Object> transferedList(VoTransferReq voTransferReq) {
        voTransferReq.setStatus(TransferContants.TRANSFERED);
        Map<String, Object> resultMaps = commonQueryTemp(voTransferReq);
        List<Transfer> tenderList = (List<Transfer>) resultMaps.get("transfers");
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("transferedList", new ArrayList<>());
            return resultMaps;
        }
        //标id集合
        Set<Long> tenderArray = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());
        List<Borrow> borrows = borrowRepository.findByIdIn(new ArrayList<>(tenderArray));
        Map<Long, Borrow> borrowMap = borrows.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

        List<Transfered> transfereds = Lists.newArrayList();
        tenderList.forEach(p -> {
            Transfered transfered = new Transfered();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            transfered.setTime(DateHelper.dateToString(p.getSuccessAt()));
            double transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128);
            transfered.setCost(StringHelper.formatMon(Math.round(borrow.getMoney() * transferFeeRate) / 100D));
            transfered.setName(borrow.getName());
            transfered.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            transfereds.add(transfered);
        });
        resultMaps.put("transferedList", transfereds);
        return resultMaps;
    }

    /**
     * 可转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public Map<String, Object> transferMayList(VoTransferReq voTransferReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();

        String sql = "select t.* from gfb_borrow_tender t  inner join gfb_borrow b " +
                "ON " +
                "t.borrow_id =b.id " +
                "where " +
                "t.status=1 " +
                "AND " +
                "t.user_id=" + voTransferReq.getUserId() + " " +
                "AND " +
                "t.transfer_flag=" + TenderConstans.TRANSFER_NO + " " +   //未转让
                "AND " +
                "t.state=" + TenderConstans.BACK_MONEY + " " +    //回款中
                "AND " +
                "(b.type=0 OR b.type=4) " +
                "AND " +
                "  ( " +
                " SELECT " +
                "SUM(c.principal) " +
                " FROM " +
                "gfb_borrow_collection c " +
                " WHERE " +
                "c.tender_id = t.id " +
                " AND " +
                "c.transfer_flag = 0 " +
                " AND " +
                "c. STATUS = 0 " +
                ") >= 100000";

        //分页
        Query sqlQuery = entityManager.createNativeQuery(sql.toString(), Tender.class);
        List<Tender> totalCountList = sqlQuery.getResultList();
        sqlQuery.setFirstResult(voTransferReq.getPageIndex());
        sqlQuery.setMaxResults(voTransferReq.getPageSize());
        List<Tender> tenderList = sqlQuery.getResultList();

        //总记录数
        resultMaps.put("totalCount", totalCountList.size());
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("transferMayList", new ArrayList<>());
            return resultMaps;
        }
        tenderList = tenderList.stream().filter(p -> !StringUtils.isEmpty(p.getBorrowId())).collect(Collectors.toList());
        //标id集合
        Set<Long> borrowIdArray = tenderList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        Specification specification = Specifications.<Borrow>and()
                .in("id", borrowIdArray.toArray())
                .eq("closeAt", null)
                .build();
        List<Borrow> borrowList = borrowRepository.findAll(specification);
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        //投标ID集合
        List<Long> tenderIdArray = tenderList.stream().map(p -> p.getId()).collect(Collectors.toList());
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findByTenderIdIn(tenderIdArray);
        Map<Long, List<BorrowCollection>> borrowCollectionMaps = borrowCollections.stream().collect(groupingBy(BorrowCollection::getTenderId));

        List<TransferMay> transferMays = Lists.newArrayList();
        tenderList.stream().forEach(p -> {
            TransferMay transferMay = new TransferMay();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            if (ObjectUtils.isEmpty(borrow)) {
                return;
            }
            transferMay.setName(borrow.getName());
            transferMay.setTenderId(p.getId());
            List<BorrowCollection> borrowCollectionList1 = borrowCollectionMaps.get(p.getId());
            long principalSum = borrowCollectionList1.stream().mapToLong(w -> w.getPrincipal()).sum();
            long interestSum = borrowCollectionList1.stream().mapToLong(w -> w.getInterest()).sum();
            transferMay.setInterest(StringHelper.formatMon(interestSum / 100d));
            transferMay.setPrincipal(StringHelper.formatMon(principalSum / 100d));
            transferMay.setOrder(borrowCollectionList1.size());
            transferMay.setBorrowId(borrow.getId());
            BorrowCollection borrowCollection = borrowCollectionList1.get(0);
            transferMay.setNextCollectionAt(DateHelper.dateToString(borrowCollection.getCollectionAt()));
            transferMays.add(transferMay);
        });
        resultMaps.put("transferMayList", transferMays);
        return resultMaps;
    }


    @Override
    public Map<String, Object> transferBuyList(VoTransferReq voTransferReq) {
        Map<String, Object> resultMaps = commonQuery(voTransferReq);

        Specification<TransferBuyLog> specification = Specifications.<TransferBuyLog>and()
                .eq("userId", voTransferReq.getUserId())
                .in("state", Lists.newArrayList(0, 1).toArray())
                .build();
        Page<TransferBuyLog> transferBuyLogPage = transferBuyLogService.findAll(specification,
                new PageRequest(voTransferReq.getPageIndex(),
                        voTransferReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        //购买债券集合
        List<TransferBuyLog> transferBuyLogs = transferBuyLogPage.getContent();

        //债券
        Set<Long> transferIds = transferBuyLogs.stream().map(m -> m.getTransferId()).collect(Collectors.toSet());
        List<Transfer> transfers = transferRepository.findByIdIn(new ArrayList<>(transferIds));
        Map<Long, Transfer> transferMap = transfers.stream().collect(Collectors.toMap(Transfer::getId, Function.identity()));

        //标集合
        Set<Long> borrowIds = transfers.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

        //数据装配
        List<TransferBuy> transferBuys = Lists.newArrayList();
        transferBuyLogs.stream().forEach(p -> {
            TransferBuy transferBuy = new TransferBuy();
            Transfer transfer = transferMap.get(p.getTransferId());
            Borrow borrow = borrowMap.get(transfer.getBorrowId());
            transferBuy.setBorrowName(borrow.getName());
            transferBuy.setBorrowId(borrow.getId());
            transferBuy.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            transferBuy.setCreateAt(DateHelper.dateToString(p.getCreatedAt()));
            transferBuys.add(transferBuy);
        });
        resultMaps.put("transferBuys", transferBuys);
        resultMaps.put("totalCount", transferBuyLogPage.getTotalElements());
        return resultMaps;
    }

    /**
     * 公共查询
     *
     * @param voTransferReq
     * @return
     */
    public Map<String, Object> commonQuery(VoTransferReq voTransferReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();
        Page<Tender> tenderPage = tenderRepository.findByUserIdAndStatusIsAndTransferFlagIs(
                voTransferReq.getUserId(),
                TenderConstans.SUCCESS,
                voTransferReq.getStatus(),
                new PageRequest(voTransferReq.getPageIndex(),
                        voTransferReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id"))
        );
        List<Tender> tenderList = tenderPage.getContent();
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("tenderList", new ArrayList<>());
        } else {
            resultMaps.put("tenderList", tenderList);
        }
        resultMaps.put("totalCount", tenderPage.getTotalElements());
        return resultMaps;
    }


    /**
     * 转让中||已转让 查询
     *
     * @param voTransferReq
     * @return
     */
    public Map<String, Object> commonQueryTemp(VoTransferReq voTransferReq) {
        Map<String, Object> resultMap = Maps.newHashMap();
        Specification<Transfer> specification = Specifications.<Transfer>and()
                .eq("userId", voTransferReq.getUserId())
                .eq("state", voTransferReq.getStatus())
                .build();
        Page<Transfer> transferPage = transferRepository.findAll(specification,
                new PageRequest(voTransferReq.getPageIndex(),
                        voTransferReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        resultMap.put("totalCount", transferPage.getTotalElements());
        resultMap.put("transfers", transferPage.getContent());
        return resultMap;
    }


}
