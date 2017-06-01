package com.gofobao.framework.collection.service;

import com.gofobao.framework.borrow.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.borrow.vo.response.VoViewCollectionOrderListRes;

/**
 * Created by admin on 2017/5/31.
 */
public interface BorrowCollectionService {


    VoViewCollectionOrderListRes orderList(VoCollectionOrderReq voCollectionOrderReq);

}
