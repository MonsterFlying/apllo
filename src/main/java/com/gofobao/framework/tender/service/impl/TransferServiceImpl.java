package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.helper.BooleanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.repository.TenderRepository;
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
        voTransferReq.setStatus(TenderConstans.TRANSFER_ING);

        Map<String, Object> resultMaps = commonQuery(voTransferReq);
        List<Tender> tenderList = (List<Tender>) resultMaps.get("tenderList");

        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("transferOfList", new ArrayList<>());
            return resultMaps;
        }
        //标id集合
        Set<Long> tenderIdArray = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());

        Specification specification = Specifications.<Borrow>and()
                .in("tenderId", tenderIdArray.toArray())
                .eq("userId", voTransferReq.getUserId())
                .eq("status", BorrowContants.BIDDING)
                .build();
        List<Borrow> borrowList = borrowRepository.findAll(specification);
        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(
                        Collectors.toMap(
                                Borrow::getTenderId,
                                Function.identity()));
        List<Long> tempTenderIdArray = borrowList.stream()
                .map(p -> p.getTenderId())
                .collect(Collectors.toList());
        List<TransferOf> transferOfs = Lists.newArrayList();
        List<Tender> tenders = tenderList.stream()
                .filter(p -> tempTenderIdArray.contains(p.getId()))
                .collect(Collectors.toList());
        tenders.stream().forEach(p -> {
            TransferOf transferOf = new TransferOf();
            Borrow borrow = borrowMap.get(p.getId());
            if (ObjectUtils.isEmpty(borrow)) {
                return;
            }
            transferOf.setName(borrow.getName());
            transferOf.setApr(StringHelper.formatMon(borrow.getApr() / 100d));
            transferOf.setCreateTime(DateHelper.dateToString(p.getUpdatedAt()));
            transferOf.setPrincipal(StringHelper.formatMon(borrow.getMoney() / 100d));
            double spend = (double) borrow.getMoneyYes() / (double) borrow.getMoney();
            transferOf.setSpend(StringHelper.formatMon(spend));
            transferOf.setCancel(spend != 1d && BooleanHelper.isFalse(borrow.getThirdTransferFlag()));
            transferOf.setBorrowId(borrow.getId());
            transferOfs.add(transferOf);
        });
        resultMaps.put("transferOfList", transferOfs);
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
        voTransferReq.setStatus(TenderConstans.TRANSFER_YES);
        Map<String, Object> resultMaps = commonQuery(voTransferReq);
        List<Tender> tenderList = (List<Tender>) resultMaps.get("tenderList");
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("transferedList", new ArrayList<>());
            return resultMaps;
        }
        //标id集合
        Set<Long> tenderArray = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());

        Specification specification = Specifications.<Borrow>and()
                .in("tenderId", tenderArray.toArray())
                .eq("userId", voTransferReq.getUserId())
                .eq("status", BorrowContants.PASS)
                .build();
        List<Borrow> borrowList = borrowRepository.findAll(specification);
        //转让标集合
        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(
                        Collectors.toMap(
                                Borrow::getTenderId,
                                Function.identity()));

        List<Long> tempTenderIdArray = borrowList.stream()
                .map(p -> p.getTenderId())
                .collect(Collectors.toList());
        //过滤掉投tender表中id没有的borrow中的tenderId
        List<Tender> tenders = tenderList.stream()
                .filter(p -> tempTenderIdArray.contains(p.getId()))
                .collect(Collectors.toList());

        List<Transfered> transfereds = Lists.newArrayList();
        tenders.stream().forEach(p -> {
            Transfered transfered = new Transfered();
            Borrow borrow = borrowMap.get(p.getId());
            if (ObjectUtils.isEmpty(borrow)) {
                return;
            }
            transfered.setName(borrow.getName());
            double transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128);
            transfered.setCost(StringHelper.formatMon(Math.round(borrow.getMoney() * transferFeeRate) / 100D));
            transfered.setTime(DateHelper.dateToString(borrow.getCreatedAt()));
            transfered.setPrincipal(StringHelper.formatMon(borrow.getMoneyYes() / 100D));
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
                "b.tender_id is null " +
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
        String sql = "select t.* from gfb_borrow_tender t  inner join gfb_borrow b " +
                "ON " +
                "t.borrow_id =b.id " +
                "where " +
                "t.status=1 " +
                "AND " +
                "b.tender_id is not null " +
                "AND " +
                "b.user_id!=" + voTransferReq.getUserId() + " " +
                "AND " +
                "(b.type=0 OR b.type=4) ORDER BY t.created_at  DESC ";

        //总记录数
        Query query = entityManager.createNativeQuery(sql, Tender.class);
        Integer totalCount = query.getResultList().size();
        resultMaps.put("totalCount", totalCount);

        //分页
        query.setFirstResult(voTransferReq.getPageIndex());
        query.setMaxResults(voTransferReq.getPageSize());
        List<Tender> tenders = query.getResultList();

        if (CollectionUtils.isEmpty(tenders)) {
            resultMaps.put("transferBuys", tenders);
            return resultMaps;
        }
        //标集合
        Set<Long> borrowIds = tenders.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

        List<TransferBuy> transferBuys = Lists.newArrayList();
        tenders.stream().forEach(p -> {
            TransferBuy transferBuy = new TransferBuy();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            transferBuy.setBorrowName(borrow.getName());
            transferBuy.setBorrowId(borrow.getId());
            transferBuy.setPrincipal(StringHelper.formatMon(p.getValidMoney() / 100D));
            transferBuy.setCreateAt(DateHelper.dateToString(p.getCreatedAt()));
            transferBuys.add(transferBuy);
        });
        resultMaps.put("transferBuys", transferBuys);
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
}
