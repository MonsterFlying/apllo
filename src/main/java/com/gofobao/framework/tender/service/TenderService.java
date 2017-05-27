package com.gofobao.framework.tender.service;

import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.tender.entity.Tender;

import java.util.List;

/**
 * Created by admin on 2017/5/19.
 */
public interface TenderService {


    boolean insert(Tender tender);

    boolean update(Tender tender);

    List<VoBorrowTenderUserRes> findBorrowTenderUser(VoBorrowByIdReq req);

}
