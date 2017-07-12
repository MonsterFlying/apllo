package com.gofobao.framework.collection.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.collection.vo.response.web.CollectionList;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime(), DateHelper.DATE_FORMAT_YMD);

        Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("collectionAt", new Range<>(date, DateHelper.endOfDate(date)))
                .eq("transferFlag", BorrowCollectionContants.TRANSFER_FLAG_NO)
                .ne("borrowId", null)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
        return Optional.ofNullable(borrowCollections).orElse(Collections.EMPTY_LIST);
    }

    /**
     * PC:回款列表
     *
     * @param orderListReq
     * @return
     */
    @Override
    public Map<String, Object> pcOrderList(OrderListReq orderListReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();
        //总记录数
        String totalSql = "select count(b.id) from BorrowCollection AS b where b.userId=:userId and b.status=0  GROUP BY date_format(b.collectionAt,'%Y%m%d') ";
        Query totalEm = entityManager.createQuery(totalSql, Long.class);
        totalEm.setParameter("userId", orderListReq.getUserId());
        List<Long> totalResult = totalEm.getResultList();
        Integer totalCount = totalResult.size();
        resultMaps.put("totalCount", totalCount);
        //分页
        String sql = "select date_format(b.collectionAt,'%Y-%m-%d'),sum(b.collectionMoney),sum(b.principal),sum(b.interest),count(b.id) from BorrowCollection AS b where b.userId=:userId  and b.status=0 GROUP BY date_format(b.collectionAt,'%Y-%m-%d') ORDER BY  b.collectionAt ASC";
        Query query = entityManager.createQuery(sql);
        query.setParameter("userId", orderListReq.getUserId());
        query.setFirstResult(orderListReq.getPageIndex() * orderListReq.getPageSize());
        query.setMaxResults(orderListReq.getPageSize());
        List resultList = query.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            resultMaps.put("orderList", new ArrayList<>());
            return resultMaps;
        }
        List<CollectionList> collectionLists = Lists.newArrayList();
        //装配结果集
        resultList.stream().forEach(p -> {
            CollectionList item = new CollectionList();
            Object[] objects = (Object[]) p;
            item.setCreateTime((String) objects[0]);
            item.setCollectionMoney(StringHelper.formatMon((Long) objects[1] / 100D));
            item.setPrincipal(StringHelper.formatMon((Long) objects[2] / 100D));
            item.setInterest(StringHelper.formatMon((Long) objects[3] / 100D));
            item.setOrderCount((Long) objects[4]);
            collectionLists.add(item);
        });
        resultMaps.put("orderList", collectionLists);
        return resultMaps;
    }


    @Override
    public Map<String, Object> pcCollectionsByDay(VoCollectionListReq listReq) {

        Map<String, Object> resultMaps = Maps.newHashMap();

        Date beginAt = DateHelper.beginOfDate(DateHelper.stringToDate(listReq.getTime(), DateHelper.DATE_FORMAT_YMD));
        Date endAt = DateHelper.endOfDate(DateHelper.stringToDate(listReq.getTime(), DateHelper.DATE_FORMAT_YMD));

        Specification specification = Specifications.<BorrowCollection>and()
                .eq("userId", listReq.getUserId())
                .eq("status", BorrowCollectionContants.STATUS_NO)
                .between("collectionAt", new Range<>(beginAt, endAt))
                .build();
        Page<BorrowCollection> collectionPage = borrowCollectionRepository.findAll(specification,
                new PageRequest(listReq.getPageIndex(),
                        listReq.getPageSize(),
                        new Sort("collectionAt")
                ));

        Long totalCount = collectionPage.getTotalElements();
        List<BorrowCollection> borrowCollections = collectionPage.getContent();
        resultMaps.put("totalCount", totalCount);

        if (CollectionUtils.isEmpty(borrowCollections)) {
            resultMaps.put("collectionList", new ArrayList<>());
            return resultMaps;
        }
        List<Collection> collectionList = Lists.newArrayList();
        Set<Long> borrowIds = borrowCollections.stream().map(p -> p.getBorrowId()).collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));
        Map<Long, Borrow> borrowMaps = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));


        borrowCollections.stream().forEach(p -> {
            Collection collection = new Collection();
            Borrow borrow = borrowMaps.get(p.getBorrowId());
            collection.setBorrowName(borrow.getName());
            collection.setInterest(StringHelper.formatMon(p.getInterest()/100D));
            collection.setPrincipal(StringHelper.formatMon(p.getPrincipal()/100D));
            collection.setCollectionAt(DateHelper.dateToString(p.getCollectionAt()));
            collection.setOrder(p.getOrder()+1);
            collection.setTimeLimit(borrow.getTimeLimit());
            if(borrow.getStatus()==0||borrow.getStatus()==4){ //官标
                    collection.setEarnings(StringHelper.formatMon((p.getCollectionMoney()*0.9)/100D));
            }else{
                collection.setEarnings(StringHelper.formatMon(p.getCollectionMoney()/100D));
            }
            collectionList.add(collection);
        });
        resultMaps.put("collectionList",collectionList);
        return resultMaps;
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
        Preconditions.checkNotNull(borrowCollection, "BorrowCollectionSericeImpl.orderDetail: borrowCollecion is empty");
        Borrow borrow = borrowRepository.findOne(borrowCollection.getBorrowId().longValue());
        VoViewOrderDetailResp detailRes = VoBaseResp.ok("查询成功", VoViewOrderDetailResp.class);
        detailRes.setOrder(borrowCollection.getOrder() + 1);
        detailRes.setCollectionMoney(StringHelper.formatMon(borrowCollection.getCollectionMoney() / 100D));
        detailRes.setLateDays(borrowCollection.getLateDays());
        detailRes.setBorrowName(borrow.getName());
        Integer interest = 0;  //利息
        Integer principal = 0;//本金
        if (borrowCollection.getStatus() == BorrowCollectionContants.STATUS_YES) {
            interest = borrowCollection.getInterest();
            principal = borrowCollection.getPrincipal();
            detailRes.setStatusStr(BorrowCollectionContants.STATUS_YES_STR);
            detailRes.setCollectionAt(DateHelper.dateToString(borrowCollection.getCollectionAtYes()));
        } else {
            detailRes.setStatusStr(BorrowCollectionContants.STATUS_NO_STR);
            detailRes.setCollectionAt(DateHelper.dateToString(borrowCollection.getCollectionAt()));

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
