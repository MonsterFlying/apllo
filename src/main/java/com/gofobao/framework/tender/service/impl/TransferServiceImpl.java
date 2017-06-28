package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.TransferMay;
import com.gofobao.framework.tender.vo.response.TransferOf;
import com.gofobao.framework.tender.vo.response.Transfered;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 转让中
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public List<TransferOf> transferOfList(VoTransferReq voTransferReq) {
        voTransferReq.setStatus(TenderConstans.TRANSFER_ING);
        List<Tender> tenderList = commonQuery(voTransferReq);

        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        //标id集合
        Set<Long> borrowIdArray = tenderList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());

        Specification specification = Specifications.<Borrow>and()
                .in("id", borrowIdArray.toArray())
                .ne("tenderId", null)
                .build();
        List<Borrow> borrowList = borrowRepository.findAll(specification);
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

        List<TransferOf> transferOfs = Lists.newArrayList();
        List<Tender> tenders = tenderList.stream().filter(p -> !StringUtils.isEmpty(p.getBorrowId())).collect(Collectors.toList());
        tenders.stream().forEach(p -> {
            TransferOf transferOf = new TransferOf();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            if (ObjectUtils.isEmpty(borrow)) {
                return;
            }
            transferOf.setName(borrow.getName());
            transferOf.setApr(StringHelper.formatMon(borrow.getApr() / 100d));
            transferOf.setCreateTime(DateHelper.dateToString(p.getUpdatedAt()));
            transferOf.setPrincipal(StringHelper.formatMon(borrow.getMoney() / 100d));
            transferOf.setSpend(StringHelper.formatMon(borrow.getMoneyYes() / borrow.getMoney() / 100d));
            transferOf.setBorrowId(p.getBorrowId());
            transferOfs.add(transferOf);
        });
        return Optional.ofNullable(transferOfs).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 已转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public List<Transfered> transferedList(VoTransferReq voTransferReq) {
        voTransferReq.setStatus(TenderConstans.TRANSFER_YES);
        List<Tender> tenderList = commonQuery(voTransferReq);
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        //标id集合
        Set<Long> borrowIdArray = tenderList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());

        Specification specification = Specifications.<Borrow>and()
                .in("id", borrowIdArray.toArray())
                .ne("tenderId", null)
                .build();
        List<Borrow> borrowList = borrowRepository.findAll(specification);
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        List<Tender> tenders = tenderList.stream().filter(p -> !StringUtils.isEmpty(p.getBorrowId())).collect(Collectors.toList());
        List<Transfered> transfereds = Lists.newArrayList();
        tenders.stream().forEach(p -> {
            Transfered transfered = new Transfered();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            if (ObjectUtils.isEmpty(borrow)) {
                return;
            }
            transfered.setName(borrow.getName());
            double transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128);
            transfered.setCost(StringHelper.formatMon(Math.round(borrow.getMoney() * transferFeeRate)));
            transfered.setTime(DateHelper.dateToString(borrow.getCreatedAt()));
            transfered.setPrincipal(StringHelper.formatMon(borrow.getMoneyYes() / 100d));
            transfereds.add(transfered);
        });
        return transfereds;
    }

    /**
     * 可转让
     *
     * @param voTransferReq
     * @return
     */
    @Override
    public List<TransferMay> transferMayList(VoTransferReq voTransferReq) {
        String sql = "select t.* from gfb_borrow_tender t  inner join gfb_borrow b " +
                "ON " +
                "t.borrow_id =b.id " +
                "where " +
                "t.status=1 " +
                "AND " +
                "t.user_id=" + voTransferReq.getUserId() +
                " AND " +
                "t.transfer_flag=0 " +
                "AND  " +
                "b.tender_id is null " +
                "AND " +
                "(b.type=0 OR b.type=4) " +
                "AND " +
                "  ( " +
                " SELECT " +
                "   SUM(c.principal) " +
                " FROM " +
                "  gfb_borrow_collection c " +
                " WHERE " +
                "  c.tender_id = t.id " +
                " AND c.transfer_flag = 0 " +
                " AND c. STATUS = 0 " +
                ") >= 100000";
        Query sqlQuery = entityManager.createNativeQuery(sql.toString(), Tender.class);
        sqlQuery.setFirstResult(voTransferReq.getPageIndex());
        sqlQuery.setMaxResults(voTransferReq.getPageSize());
        List<Tender> tenderList = sqlQuery.getResultList();
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        tenderList = tenderList.stream().filter(p -> !StringUtils.isEmpty(p.getBorrowId())).collect(Collectors.toList());
        //标id集合
        Set<Long> borrowIdArray = tenderList.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        Specification specification = Specifications.<Borrow>and()
                .in("id", borrowIdArray.toArray())
                .eq("tenderId", null)
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
            Integer principalSum = borrowCollectionList1.stream().mapToInt(w -> w.getPrincipal()).sum();
            Integer interestSum = borrowCollectionList1.stream().mapToInt(w -> w.getInterest()).sum();
            transferMay.setInterest(StringHelper.formatMon(interestSum / 100d));
            transferMay.setPrincipal(StringHelper.formatMon(principalSum / 100d));
            transferMay.setOrder(borrowCollectionList1.size());
            BorrowCollection borrowCollection = borrowCollectionList1.get(0);
            transferMay.setNextCollectionAt(DateHelper.dateToString(borrowCollection.getCollectionAt()));
            transferMays.add(transferMay);
        });
        return transferMays;
    }

    /**
     * 公共查询
     *
     * @param voTransferReq
     * @return
     */
    public List<Tender> commonQuery(VoTransferReq voTransferReq) {
        Page<Tender> tenderPage = tenderRepository.findByUserIdAndStatusIsAndTransferFlagIs(
                voTransferReq.getUserId(),
                TenderConstans.SUCCESS,
                voTransferReq.getStatus(),
                new PageRequest(voTransferReq.getPageIndex(),
                        voTransferReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id"))
        );
        List<Tender> tenderList = tenderPage.getContent();

        return Optional.ofNullable(tenderList).orElse(Collections.emptyList());
    }
}
