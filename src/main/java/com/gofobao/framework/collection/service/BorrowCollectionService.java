package com.gofobao.framework.collection.service;

import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by admin on 2017/5/31.
 */
public interface BorrowCollectionService {

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    VoViewCollectionOrderList orderList(VoCollectionOrderReq voCollectionOrderReq);


    /**
     * 回款详情
     */

    VoViewOrderDetailRes orderDetail(VoOrderDetailReq voOrderDetailReq);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification);

    long count(Specification<BorrowCollection> specification);

    boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification);

    BorrowCollection save(BorrowCollection borrowCollection);

    BorrowCollection insert(BorrowCollection borrowCollection);

    BorrowCollection updateById(BorrowCollection borrowCollection);

    List<BorrowCollection> save(List<BorrowCollection> borrowCollectionList);
}
