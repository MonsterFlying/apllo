package com.gofobao.framework.collection.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * Created by admin on 2017/5/31.
 */
@Component
public class BorrowCollectionServiceImpl implements BorrowCollectionService {

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return VoViewCollectionOrderListRes
     */
    @Override
    public List<BorrowCollection> orderList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime(),DateHelper.DATE_FORMAT_YMD);

        Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("collectionAt", new Range<>(date, DateHelper.endOfDate(date)))
                .eq("transferFlag", BorrowCollectionContants.TRANSFER_FLAG_NO)
                .ne("borrowId",null)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
        return Optional.ofNullable(borrowCollections).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return VoViewOrderDetailResp
     */
    @Override
    public VoViewOrderDetailResp orderDetail(VoOrderDetailReq voOrderDetailReq) {
        BorrowCollection borrowCollection = borrowCollectionRepository.findOne(voOrderDetailReq.getCollectionId());
        if (Objects.isNull(borrowCollection)) {
            return null;
        }
        Borrow borrow = borrowRepository.findOne(borrowCollection.getBorrowId().longValue());
        VoViewOrderDetailResp detailRes = new VoViewOrderDetailResp();
        detailRes.setOrder(borrowCollection.getOrder() + 1);
        detailRes.setCollectionMoney(StringHelper.formatMon(borrowCollection.getCollectionMoney() / 100d));
        detailRes.setLateDays(borrowCollection.getLateDays());
        detailRes.setStartAt(DateHelper.dateToString(borrowCollection.getStartAtYes()));
        detailRes.setBorrowName(borrow.getName());
        Integer interest = 0;  //利息
        Integer principal = 0;//本金
        if (borrowCollection.getStatus() == BorrowCollectionContants.STATUS_YES) {
            interest = borrowCollection.getInterest();
            principal = borrowCollection.getPrincipal();
            detailRes.setStatus(BorrowCollectionContants.STATUS_YES_STR);
        } else {
            detailRes.setStatus(BorrowCollectionContants.STATUS_NO_STR);
        }
        detailRes.setPrincipal(NumberHelper.to2DigitString(interest / 100D));
        detailRes.setInterest(NumberHelper.to2DigitString(principal / 100D));
        return detailRes;
    }

    @Override
    public List<Integer> collectionDay(String date, Long userId) {
        String sql = "SELECT DAY(collection_at) FROM gfb_borrow_collection " +
                "where " +
                "user_id=" + userId + " " +
                "and " +
                "`status`=0 " +
                "and   date_format(collection_at,'%Y%m') =" + date +
                " GROUP BY  day(collection_at)";
        Query query = entityManager.createNativeQuery(sql);
        List result = query.getResultList();
        return result;
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable) {
        Page<BorrowCollection> page = borrowCollectionRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort) {
        return borrowCollectionRepository.findAll(specification, sort);
    }

    public boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification) {
        List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findAll(specification);
        Optional<List<BorrowCollection>> optional = Optional.ofNullable(borrowCollectionList);
        optional.ifPresent(list -> list.forEach(obj -> {
            BeanHelper.copyParamter(borrowCollection, obj, true);
        }));
        return !CollectionUtils.isEmpty(borrowCollectionRepository.save(borrowCollectionList));
    }

    public BorrowCollection save(BorrowCollection borrowCollection) {
        return borrowCollectionRepository.save(borrowCollection);
    }

    public BorrowCollection insert(BorrowCollection borrowCollection) {
        if (ObjectUtils.isEmpty(borrowCollection)) {
            return null;
        }
        borrowCollection.setId(null);
        return borrowCollectionRepository.save(borrowCollection);
    }

    public BorrowCollection updateById(BorrowCollection borrowCollection) {
        if (ObjectUtils.isEmpty(borrowCollection) || ObjectUtils.isEmpty(borrowCollection.getId())) {
            return null;
        }
        return borrowCollectionRepository.save(borrowCollection);
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification) {
        return borrowCollectionRepository.findAll(specification);
    }

    public long count(Specification<BorrowCollection> specification) {
        return borrowCollectionRepository.count(specification);
    }

    public List<BorrowCollection> save(List<BorrowCollection> borrowCollectionList) {
        return borrowCollectionRepository.save(borrowCollectionList);
    }
}
