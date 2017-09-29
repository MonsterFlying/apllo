package com.gofobao.framework.finance.biz;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.finance.vo.request.VoFinanceCollectionDetailReq;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionDetailResp;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionListResp;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<VoViewFinanceCollectionListResp> orderList(VoCollectionOrderReq voCollectionOrderReq);


    /**
     * 回款详情
     *
     * @param voFinanceCollectionDetailReq
     * @return
     */
    ResponseEntity<VoViewFinanceCollectionDetailResp> orderDetail(VoFinanceCollectionDetailReq voFinanceCollectionDetailReq);


    /**
     * 回款日期
     *
     * @param date
     * @param userId
     * @return
     */
    ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId);


}
