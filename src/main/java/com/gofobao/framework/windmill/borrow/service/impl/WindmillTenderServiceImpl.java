package com.gofobao.framework.windmill.borrow.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.windmill.borrow.service.WindmillTenderService;
import com.gofobao.framework.windmill.borrow.vo.request.BackRecordsReq;
import com.gofobao.framework.windmill.borrow.vo.request.UserTenderLogReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/4.
 */
@Component
public class WindmillTenderServiceImpl implements WindmillTenderService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    /**
     * 5.6投资记录查询接口
     *
     * @param tenderLogReq
     * @return
     */
    @Override
    public List<Tender> userTenderLog(UserTenderLogReq tenderLogReq) {

        List<Tender> resultTenders;

        StringBuilder sql = new StringBuilder("SELECT t FROM Tender t WHERE 1=1 AND t.userId=:userId ");
        //  如果传了id，只查询该用户这个id的记录
        if (StringUtils.isEmpty(tenderLogReq.getInvest_record_id())) {
            //時間條件的判斷
            String startAt = "";
            String endAt = "";
            if (!StringUtils.isEmpty(tenderLogReq.getStart_time())) {
                startAt = tenderLogReq.getStart_time();
            }
            if (StringUtils.isEmpty(tenderLogReq.getEnd_time())) {
                endAt = tenderLogReq.getEnd_time();
            }

            //時間格式
            if (!StringUtils.isEmpty(startAt) || !StringUtils.isEmpty(endAt)) {
                if (!StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(endAt)) {
                    sql.append(" AND t.createAt>='" + startAt + "' AND t.createAt<='" + endAt + "'");
                } else {
                    if (!StringUtils.isEmpty(startAt)) {
                        sql.append(" AND t.createAt>='" + startAt + "' ");
                    }
                    if (!StringUtils.isEmpty(endAt)) {
                        sql.append(" AND t.createAt<='" + endAt + "' ");
                    }
                }
            }
            //投資狀態判斷
            if (!StringUtils.isEmpty(tenderLogReq.getInvest_status())) {
                Integer state;
                //待回款 ||逾期
                if (tenderLogReq.getInvest_status() == 0 || tenderLogReq.getInvest_status() == 2) {
                    state = TenderConstans.BACK_MONEY;
                    //投资中
                } else if (tenderLogReq.getInvest_status() == -1) {
                    state = TenderConstans.BIDDING;
                } else {//已回款
                    state = TenderConstans.SETTLE;
                }
                sql.append(" AND t.state= " + state);
            }
            sql.append(" order by id desc ");
            Query query = entityManager.createQuery(sql.toString(), Tender.class);
            query.setParameter("userId", tenderLogReq.getPf_user_id());
            if (StringUtils.isEmpty(tenderLogReq.getInvest_status()) || tenderLogReq.getInvest_status() != 2) {
                query.setFirstResult(tenderLogReq.getOffset());
                query.setMaxResults(tenderLogReq.getLimit());
            }
            resultTenders = query.getResultList();
        } else {
            sql.append(" AND t.id=:id");
            sql.append(" order by id desc ");
            Query query = entityManager.createQuery(sql.toString(), Tender.class);
            query.setParameter("userId", tenderLogReq.getPf_user_id());
            try {
                query.setParameter("id",  tenderLogReq.getInvest_record_id());
            } catch (Exception e) {
            }
            resultTenders = query.getResultList();
        }
        if (CollectionUtils.isEmpty(resultTenders)) {
            return Collections.EMPTY_LIST;
        }
        //预期中的标
        if (!StringUtils.isEmpty(tenderLogReq.getInvest_status()) && tenderLogReq.getInvest_status() == 2) {
            List<Long> ids = resultTenders.stream().map(m -> m.getId()).collect(Collectors.toList());
            Specification specification = Specifications.<BorrowCollection>and()
                    .in("tenderId", ids.toArray())
                    .gt("lateDays", 0)
                    .build();
            List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
            if (CollectionUtils.isEmpty(borrowCollections)) {
                return resultTenders;
            }
            ids = borrowCollections.stream().map(p -> p.getTenderId()).collect(Collectors.toList());
            return tenderRepository.findByIdIn(ids);
        }
        return resultTenders;
    }

    /**
     * 5.7投资记录回款计划
     *
     * @param backRecordsReq
     * @return
     */
    @Override
    public List<BorrowCollection> backCollectionList(BackRecordsReq backRecordsReq) {
        Specification specification = Specifications.<BorrowCollection>and()
                .eq("borrowId", backRecordsReq.getBid_id())
                .eq("userId", backRecordsReq.getPf_user_id())
                .eq("tenderId", backRecordsReq.getInvest_record_id())
                .build();
        return borrowCollectionRepository.findAll(specification);
    }
}
