package com.gofobao.framework.collection.service;

import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.collection.vo.response.web.CollectionList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

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
    List<BorrowCollection> orderList(VoCollectionOrderReq voCollectionOrderReq);

    /**
     * pc:回款列表
     * @param listReq
     * @return
     */
    Map<String, Object> pcOrderList(OrderListReq listReq);

    /**
     * pc：回款明细导出
     * @param listReq
     * @return
     */
    List<CollectionList> toExecl(OrderListReq listReq);



    /**
     * 回款详情
     */

    VoViewOrderDetailResp orderDetail(VoOrderDetailReq voOrderDetailReq);





    List<Integer> collectionDay(String date,Long userId);

    /**
     *
     * @param collectionListReq
     * @return
     */
    Map<String, Object> pcCollectionsByDay(VoCollectionListReq collectionListReq);




    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification);

    long count(Specification<BorrowCollection> specification);

    boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification);

    BorrowCollection save(BorrowCollection borrowCollection) throws Exception;

    BorrowCollection insert(BorrowCollection borrowCollection);

    BorrowCollection updateById(BorrowCollection borrowCollection);

    List<BorrowCollection> save(List<BorrowCollection> borrowCollectionList);
}
