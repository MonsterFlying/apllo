package com.gofobao.framework.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListRes;
import com.gofobao.framework.borrow.vo.response.VoViewCollectionOrderListRes;
import org.springframework.data.domain.Example;

import java.util.List;

/**
 * Created by admin on 2017/5/17.
 */
public interface BorrowService {

    List<VoViewBorrowListRes> findAll(VoBorrowListReq voBorrowListReq);

    VoBorrowByIdRes findByBorrowId(VoBorrowByIdReq req);

    long countByUserIdAndStatusIn(Long userId,List<Integer> statusList);

    boolean insert(Borrow borrow);

    boolean updateById(Borrow borrow);

    Borrow findByIdLock(Long borrowId);

}
