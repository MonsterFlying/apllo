package com.gofobao.framework.collection.biz;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface PaymentBiz {

    /**
     * 回款列表
     * @param voCollectionOrderReq
     * @return
     */
   ResponseEntity<VoViewCollectionOrderListWarpResp> orderList(VoCollectionOrderReq voCollectionOrderReq);

    /**
     * 回款详情
     * @param voOrderDetailReq
     * @return
     */
   ResponseEntity<VoViewOrderDetailResp> orderDetail(VoOrderDetailReq voOrderDetailReq);


   ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId);

}
