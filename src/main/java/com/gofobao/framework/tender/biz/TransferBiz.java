package com.gofobao.framework.tender.biz;

import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.*;
import com.gofobao.framework.tender.vo.response.*;
import com.gofobao.framework.tender.vo.response.web.VoViewTransferBuyWarpRes;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
public interface TransferBiz {

    /**
     * 结束债权转让
     * @param voEndTransfer
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> endTransfer(VoEndTransfer voEndTransfer)throws Exception;

    /**
     * 债权转让复审
     *
     * @param transferId
     * @return
     */
    ResponseEntity<VoBaseResp> againVerifyTransfer(long transferId,long batchNo) throws Exception;

    /**
     * 债权转让初审
     *
     * @param voPcFirstVerityTransfer
     * @return
     */
    ResponseEntity<VoBaseResp> firstVerifyTransfer(VoPcFirstVerityTransfer voPcFirstVerityTransfer) throws Exception;

    /**
     * 购买债权转让
     */
    ResponseEntity<VoBaseResp> buyTransfer(VoBuyTransfer voBuyTransfer) throws Exception;

    /**
     * 新版债权转让
     *
     * @param voTransferTenderReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> newTransferTender(VoTransferTenderReq voTransferTenderReq) throws Exception;

    /**
     * 转让中
     * @param voTransferReq
     * @return
     */
    ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(VoTransferReq voTransferReq);

    /**
     * 已转让
     * @param voTransferReq
     * @return
     */
    ResponseEntity<VoViewTransferedWarpRes> transferedlist(VoTransferReq voTransferReq);

    /**
     * 可转让
     * @param voTransferReq
     * @return
     */
    ResponseEntity<VoViewTransferMayWarpRes> transferMayList(VoTransferReq voTransferReq);

    /**
     * 已购买
     * @param voTransferReq
     * @return
     */
    ResponseEntity<VoViewTransferBuyWarpRes> tranferBuyList(VoTransferReq voTransferReq);

    /**
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    ResponseEntity<VoGoTenderInfo> goTenderInfo(Long tenderId, Long userId);

    /**
     * 理财列表
     * @param voBorrowListReq
     * @return
     */

    List<VoViewBorrowList> findTransferList(VoBorrowListReq voBorrowListReq);

    /**
     * 获取转让标详情
     * @param transferId
     * @return
     */
    ResponseEntity<BorrowInfoRes> transferInfo(Long transferId);

    /**
     * 通过投标记录ID取消债权转让
     * @param id
     */
    void cancelTransferByTenderId(Long id) throws Exception;


    /**
     * 投标记录列表
     * @return
     */
    ResponseEntity<VoBorrowTenderUserWarpListRes> transferUserList(Long borrowId);

}
