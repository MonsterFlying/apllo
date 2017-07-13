package com.gofobao.framework.borrow.biz;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.borrow.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/26.
 */
public interface BorrowBiz {

    /**
     * 取消借款
     * @param voCancelBorrow
     * @return
     */
    ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow) throws Exception;

    /**
     * pc取消借款
     * @param voPcCancelThirdBorrow
     * @return
     */
    ResponseEntity<VoBaseResp> pcCancelBorrow(VoPcCancelThirdBorrow voPcCancelThirdBorrow) throws Exception;

    /**
     * 新增净值借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow);


    /**
     * 首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    ResponseEntity<VoViewBorrowListWarpRes> findAll(VoBorrowListReq voBorrowListReq);


    /**
     * pc：首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    ResponseEntity<VoPcBorrowList> pcFindAll(VoBorrowListReq voBorrowListReq);

    /**
     * 非转让标复审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    boolean notTransferBorrowAgainVerify(Borrow borrow) throws Exception;

    /**
     * 转让标复审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    boolean transferBorrowAgainVerify(Borrow borrow) throws Exception;


    /**
     * 标信息
     *
     * @param borrowId
     * @return
     */
    ResponseEntity<BorrowInfoRes> info(Long borrowId);

    /**
     * 标简介
     */
    ResponseEntity<VoViewVoBorrowDescWarpRes> desc(Long borrowId);

    /**
     * 标合同
     *
     * @param borrowId
     * @param userId
     * @return
     */
    Map<String, Object> contract(Long borrowId, Long userId);

    /**
     * pc：标合同
     *
     * @param borrowId
     * @param userId
     * @return
     */
    Map<String, Object> pcContract(Long borrowId, Long userId);

    /**
     * 提前结清
     *
     * @param voRepayAll
     * @return
     */
    ResponseEntity<VoBaseResp> repayAll(VoRepayAll voRepayAll);


    /**
     * pc:招标中统计
     *
     * @param
     * @return
     */
    ResponseEntity<VoViewBorrowStatisticsWarpRes> statistics();

    /**
     * 请求复审
     */
    ResponseEntity<VoBaseResp> doAgainVerify(VoDoAgainVerifyReq voDoAgainVerifyReq);

    /**
     * 校验提前结清参数
     *
     * @param voRepayAll
     * @return
     */
    ResponseEntity<VoBaseResp> checkRepayAll(VoRepayAll voRepayAll);

    /**
     * 登记官方借款（车贷标、渠道标）
     *
     * @param voRegisterOfficialBorrow
     * @param request
     * @return
     */
    ResponseEntity<VoHtmlResp> registerOfficialBorrow(VoRegisterOfficialBorrow voRegisterOfficialBorrow, HttpServletRequest request);


    /**
     * 执行受托支付状态查询
     * @param borrowId
     * @return
     */
    boolean doTrusteePay(Long borrowId);
}
