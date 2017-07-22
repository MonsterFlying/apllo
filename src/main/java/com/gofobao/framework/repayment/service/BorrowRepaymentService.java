package com.gofobao.framework.repayment.service;

import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.repayment.vo.request.VoOrderListReq;
import com.gofobao.framework.repayment.vo.response.RepayCollectionLog;
import com.gofobao.framework.repayment.vo.response.RepaymentOrderDetail;
import com.gofobao.framework.repayment.vo.response.pc.VoOrdersList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/1.
 */
public interface BorrowRepaymentService {

    /**
     * 还款计划列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    List<BorrowRepayment> repaymentList(VoCollectionOrderReq voCollectionOrderReq);

    List<BorrowRepayment> save(List<BorrowRepayment> borrowRepaymentList);


    /**
     * PC：还款计划
     *
     * @param orderListReq
     * @return
     */
    Map<String, Object> pcOrderList(VoOrderListReq orderListReq);


    /**
     * PC：还款计划导出excel
     *
     * @param orderListReq
     * @return
     */
    List<VoOrdersList> toExcel(VoOrderListReq orderListReq);

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    RepaymentOrderDetail detail(VoInfoReq voInfoReq);


    /**
     * 还款详情
     *
     * @param orderReq
     * @return
     */
    Map<String, Object> collectionList(VoCollectionListReq orderReq);

    /**
     * 当月还款天数
     *
     * @param userId
     * @param time
     * @return
     */
    List<Integer> days(Long userId, String time);


    List<RepayCollectionLog> logs(Long borrowId);


    BorrowRepayment save(BorrowRepayment borrowRepayment);

    BorrowRepayment insert(BorrowRepayment borrowRepayment);

    BorrowRepayment updateById(BorrowRepayment borrowRepayment);

    BorrowRepayment findByIdLock(Long id);

    BorrowRepayment findById(Long id);

    List<BorrowRepayment> findList(Specification<BorrowRepayment> specification);

    List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Sort sort);

    List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Pageable pageable);

    long count(Specification<BorrowRepayment> specification);

}
