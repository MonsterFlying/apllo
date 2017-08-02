package com.gofobao.framework.windmill.borrow.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.windmill.borrow.service.WindmillBorrowService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2017/8/1.
 */
@Component
public class WindmillBorrowServiceImpl implements WindmillBorrowService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BorrowRepository borrowRepository;

    //不传id,查当前可投标的列表。如带id查询时，只返回这个id信息，满标也要返回
    @Override
    public List<Borrow> list(Long id) {

        //过滤掉 发标待审 初审不通过；复审不通过 已取消
        List statusArray = Lists.newArrayList(
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS),
                new Integer(BorrowContants.PENDING));
        //過濾掉秒标,净值标
        List typeArray = Lists.newArrayList(new Integer(BorrowContants.INDEX_TYPE_JING_ZHI),
                new Integer(BorrowContants.MIAO_BIAO));

        String sql = "SELECT b.* FROM Borrow b LEFT JOIN Tender t  ON t.borrowId!=b.id WHERE 1=1 AND b.status NOT IN(:statusArray) AND b.type NOT IN (:typeArray) AND closeAt IS NULL AND tenderId IS NULL ";

        if (ObjectUtils.isEmpty(id)) {
            sql += "id=" + id;
        }
        Query query = entityManager.createQuery(sql, Borrow.class);
        List<Borrow> borrowList = query.getResultList();
        Optional<List<Borrow>> result = Optional.empty();
        return result.ofNullable(borrowList).orElse(Collections.emptyList());
    }
}
