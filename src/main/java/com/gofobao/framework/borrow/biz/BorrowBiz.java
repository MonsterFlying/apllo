package com.gofobao.framework.borrow.biz;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.VoAddNetWorthBorrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListWarpRes;
import com.gofobao.framework.borrow.vo.response.VoViewVoBorrowDescWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Created by Zeke on 2017/5/26.
 */
public interface BorrowBiz {

    ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow);

    /**
     * 新增净值借款
     * @param voAddNetWorthBorrow
     * @return
     */
    ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow);


    /**
     * 首页标列表
     * @param voBorrowListReq
     * @return
     */
    ResponseEntity<VoViewBorrowListWarpRes> findAll(VoBorrowListReq voBorrowListReq);

    /**
     * 非转让标复审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    boolean notTransferedBorrowAgainVerify(Borrow borrow) throws Exception;

    /**
     * 转让标复审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    boolean transferedBorrowAgainVerify(Borrow borrow) throws Exception ;


    /**
     * 标信息
     * @param borrowId
     * @return
     */
    ResponseEntity<Object> info(Long borrowId);

    /**
     * 标简介
     */
    ResponseEntity<VoViewVoBorrowDescWarpRes> desc(Long borrowId);

    /**
     * 标合同
     * @param borrowId
     * @param userId
     * @return
     */
    Map<String, Object> contract(Long borrowId, Long userId);

}
