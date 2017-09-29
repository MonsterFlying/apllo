package com.gofobao.framework.finance.biz;

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

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/6.
 */
public interface FinanceCollectionBiz {

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    ResponseEntity<VoViewCollectionOrderListWarpResp> orderList(VoCollectionOrderReq voCollectionOrderReq);



    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return
     */
    ResponseEntity<VoViewOrderDetailResp> orderDetail(VoOrderDetailReq voOrderDetailReq);


    ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId);
    

}
