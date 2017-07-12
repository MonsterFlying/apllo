package com.gofobao.framework.collection.biz;

import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.collection.vo.response.web.VoCollectionListByDays;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionListWarpRes;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface PaymentBiz {

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    ResponseEntity<VoViewCollectionOrderListWarpResp> orderList(VoCollectionOrderReq voCollectionOrderReq);

    /**
     * pc：回款明细
     *
     * @param orderListReq
     * @return
     */
    ResponseEntity<VoViewCollectionListWarpRes> pcOrderList(OrderListReq orderListReq);


    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return
     */
    ResponseEntity<VoViewOrderDetailResp> orderDetail(VoOrderDetailReq voOrderDetailReq);

    /**
     *PC:回款详情
     * @param listReq
     * @return
     */
    ResponseEntity<VoViewCollectionWarpRes> pcOrderDetail(VoCollectionListReq listReq);


    ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId);

    /**
     * 根据时间查询回款列表
     * @param date
     * @param userId
     * @return
     */
    ResponseEntity<VoCollectionListByDays> collectionListByDays(String date, Long userId);

}
