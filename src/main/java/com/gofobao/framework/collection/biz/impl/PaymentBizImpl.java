package com.gofobao.framework.collection.biz.impl;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2017/6/6.
 */
@Service
@Slf4j
public class PaymentBizImpl implements PaymentBiz {

    @Autowired
    private BorrowCollectionService borrowCollectionService;

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> orderList(VoCollectionOrderReq voCollectionOrderReq) {
        try {
            VoViewCollectionOrderList collectionOrderList = borrowCollectionService.orderList(voCollectionOrderReq);
            VoViewCollectionOrderListResWarpRes resWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionOrderListResWarpRes.class);
            resWarpRes.setListRes(collectionOrderList);
            return ResponseEntity.ok(resWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewCollectionOrderListResWarpRes.class));
        }
    }

    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderDetailWarpRes> orderDetail(VoOrderDetailReq voOrderDetailReq) {
        try {
            VoViewOrderDetailRes detailRes = borrowCollectionService.orderDetail(voOrderDetailReq);
            VoViewOrderDetailWarpRes resWarpRes = VoBaseResp.ok("查询成功", VoViewOrderDetailWarpRes.class);
            resWarpRes.setDetailWarpRes(detailRes);
            return ResponseEntity.ok(resWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewOrderDetailWarpRes.class));
        }
    }
}
