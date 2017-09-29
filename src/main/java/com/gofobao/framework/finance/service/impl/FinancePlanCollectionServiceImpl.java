package com.gofobao.framework.finance.service.impl;

import com.gofobao.framework.finance.entity.FinancePlanCollection;
import com.gofobao.framework.finance.repository.FinancePlanCollertionRepository;
import com.gofobao.framework.finance.service.FinancePlanCollectionService;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
@Service
public class FinancePlanCollectionServiceImpl implements FinancePlanCollectionService {
    @Autowired
    private FinancePlanCollertionRepository financePlanCollertionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public FinancePlanCollection save(FinancePlanCollection financePlanCollection) {
        return financePlanCollertionRepository.save(financePlanCollection);
    }

    public List<FinancePlanCollection> save(List<FinancePlanCollection> financePlanCollectionList) {
        return financePlanCollertionRepository.save(financePlanCollectionList);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification) {
        return financePlanCollertionRepository.findAll(specification);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Sort sort) {
        return financePlanCollertionRepository.findAll(specification, sort);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Pageable pageable) {
        return financePlanCollertionRepository.findAll(specification, pageable).getContent();
    }

    public FinancePlanCollection findById(long id) {
        return financePlanCollertionRepository.findOne(id);
    }

    public long count(Specification<FinancePlanCollection> specification) {
        return financePlanCollertionRepository.count(specification);
    }

    @Override
    public List<Integer> collectionDay(String date, Long userId) {
        String sql = "SELECT DAY(collection_at) FROM gfb_finance_plan_collection " +
                "where " +
                "user_id=" + userId + " " +
                "and   date_format(collection_at,'%Y%m') =" + date +
                " GROUP BY  day(collection_at)";
        Query query = entityManager.createNativeQuery(sql);
        List result = query.getResultList();
        return result;
    }
}
