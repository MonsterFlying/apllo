package com.gofobao.framework.collection.service;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;

/**
 * Created by admin on 2017/5/31.
 */
public interface BorrowCollectionService {

    /**
     * 回款列表
     * @param voCollectionOrderReq
     * @return
     */
    VoViewCollectionOrderListRes orderList(VoCollectionOrderReq voCollectionOrderReq);


    /**
     *回款详情
     */


    VoViewOrderDetailRes orderDetail(VoOrderDetailReq voOrderDetailReq);

}
