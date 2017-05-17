package com.gofobao.framework.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;

import java.util.List;

/**
 * Created by admin on 2017/5/17.
 */
public interface BorrowService {

         List<VoViewBorrowListRes> findAll(VoBorrowListReq voBorrowListReq);

}
