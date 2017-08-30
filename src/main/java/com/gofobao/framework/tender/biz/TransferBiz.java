package com.gofobao.framework.tender.biz;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.VoPcBorrowList;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.vo.request.*;
import com.gofobao.framework.tender.vo.response.*;
import com.gofobao.framework.tender.vo.response.web.VoViewTransferBuyWarpRes;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
public interface TransferBiz {

    /**
     * 查询债权转让购买记录
     * @return
     */
    ResponseEntity<VoViewTransferBuyLogList> findTransferBuyLog(VoFindTransferBuyLog voFindTransferBuyLog);

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
    ResponseEntity<VoBaseResp> againVerifyTransfer(long transferId,String batchNo) throws Exception;

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
     * pc理财列表
     * @param voBorrowListReq
     * @return
     */
    ResponseEntity<VoPcBorrowList> pcFindTransferList(VoBorrowListReq voBorrowListReq);

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
    ResponseEntity<VoBorrowTenderUserWarpListRes> transferUserList(VoTransferUserListReq transferUserListReq);

    /**
     * 新增子级标的
     *
     * @param nowDate
     * @param transfer
     * @param parentTender
     * @param transferBuyLogList
     * @return
     */
    List<Tender> addChildTender(Date nowDate, Transfer transfer, Tender parentTender, List<TransferBuyLog> transferBuyLogList);

    /**
     * 生成子级债权回款记录，标注老债权回款已经转出
     *
     * @param nowDate
     * @param transfer
     * @param parentBorrow
     * @param childTenderList
     */
    List<BorrowCollection> addChildTenderCollection(Date nowDate, Transfer transfer, Borrow parentBorrow, List<Tender> childTenderList) throws Exception;


}
