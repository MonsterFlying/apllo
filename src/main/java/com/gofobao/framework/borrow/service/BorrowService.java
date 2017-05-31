package com.gofobao.framework.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListRes;
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

    /**
     * 检查是否招标中
     * @param borrow
     * @return
     */
    boolean checkBidding(Borrow borrow);

    /**
     * 检查是否在发布时间内
     * @param borrow
     * @return
     */
    boolean checkReleaseAt(Borrow borrow);

    /**
     * 检查招标时间是否有效
     * @param borrow
     * @return
     */
    boolean checkValidDay(Borrow borrow);

    /**
     * 检查投标是否太频繁
     * @param borrow
     * @return
     */
    boolean checkTenderNimiety(Borrow borrow);
}
