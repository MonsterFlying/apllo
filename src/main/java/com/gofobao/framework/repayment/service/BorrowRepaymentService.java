package com.gofobao.framework.repayment.service;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;

/**
 * Created by admin on 2017/6/1.
 */
public interface BorrowRepaymentService {

    /**
     * 还款计划列表
     * @param voCollectionOrderReq
     * @return
     */
    VoViewCollectionOrderList repaymentList(VoCollectionOrderReq voCollectionOrderReq);


    /**
     *还款详情
     * @param voInfoReq
     * @return
     */
    VoViewOrderDetailRes info(VoInfoReq voInfoReq);

    BorrowRepayment save(BorrowRepayment borrowRepayment);

    BorrowRepayment insert(BorrowRepayment borrowRepayment);

    BorrowRepayment updateById(BorrowRepayment borrowRepayment);

}
