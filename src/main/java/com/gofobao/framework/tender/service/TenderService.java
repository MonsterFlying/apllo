package com.gofobao.framework.tender.service;

import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;

import java.util.List;

/**
 * Created by admin on 2017/5/19.
 */
public interface TenderService {


    List<VoBorrowTenderUserRes> findBorrowTenderUser(VoBorrowByIdReq req);

}
