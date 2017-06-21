package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
@Slf4j
@Service
public class TransferBizImpl implements TransferBiz {

    @Autowired
    private TransferService transferService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private AssetService assetService;

    @Override
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(VoTransferReq voTransferReq) {
        try {
            List<TransferOf> transferOfs= transferService.transferOfList(voTransferReq);
            VoViewTransferOfWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferOfWarpRes.class);
            voViewTransferOfWarpRes.setTransferOfs(transferOfs);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl tranferOfList query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferOfWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(VoTransferReq voTransferReq) {
        try {
            List<Transfered> transfereds= transferService.transferedList(voTransferReq);
            VoViewTransferedWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferedWarpRes.class);
            voViewTransferOfWarpRes.setTransferedList(transfereds);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl transferedlist query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferedWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(VoTransferReq voTransferReq) {
        try {
            List<TransferMay> transferOfs= transferService.transferMayList(voTransferReq);
            VoViewTransferMayWarpRes voViewTransferOfWarpRes= VoBaseResp.ok("查询成功",VoViewTransferMayWarpRes.class);
            voViewTransferOfWarpRes.setMayList(transferOfs);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        }catch (Exception e){
            log.info("TransferBizImpl transferMayList query fail%S",e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferMayWarpRes.class));
        }
    }

    /**
     * 债权转让
     *
     * @param voTransferTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> transferTender(VoTransferTenderReq voTransferTenderReq) {
        Date nowDate = new Date();
        Long userId = voTransferTenderReq.getUserId();
        Long tenderId = voTransferTenderReq.getTenderId();

        Tender tender = tenderService.findById(tenderId);
        if ((ObjectUtils.isEmpty(tender)) || (tender.getTransferFlag() != 0) || (tender.getStatus() != 1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "操作不存在"));
        }

        Borrow borrow = borrowService.findById(tender.getBorrowId());
        if ((borrow.getType() != 0) || (borrow.getStatus() != 3) || borrow.isTransfer()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "操作不存在"));
        }

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("transferFlag", 0)
                .eq("status", 0)
                .eq("tenderId", tenderId)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "`order`"));
        if (CollectionUtils.isEmpty(borrowCollectionList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "立即转让失败!"));
        }

        int waitRepayCount = borrowCollectionList.size();
        if (waitRepayCount < 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "操作不存在"));
        }

        int cantrCapital = 0;
        for (BorrowCollection borrowCollection : borrowCollectionList) {
            cantrCapital += borrowCollection.getPrincipal();
        }

        if (cantrCapital < (1000 * 100)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "可转本金必须大于1000元才能转让"));
        }

        //转让借款
        Borrow tempBorrow = new Borrow();
        tempBorrow.setType(3);//3 转让标
        tempBorrow.setUse(borrow.getUse());
        tempBorrow.setIsLock(false);
        tempBorrow.setRepayFashion(borrow.getRepayFashion());
        tempBorrow.setTimeLimit(borrow.getRepayFashion() == 1 ? borrow.getTimeLimit() : waitRepayCount);
        tempBorrow.setMoney(cantrCapital);
        tempBorrow.setApr(borrow.getApr());
        tempBorrow.setLowest(1000 * 100);
        tempBorrow.setValidDay(1);
        tempBorrow.setName(borrow.getName());
        tempBorrow.setDescription(borrow.getDescription());
        tempBorrow.setIsVouch(borrow.getIsVouch());
        tempBorrow.setIsMortgage(borrow.getIsMortgage());
        tempBorrow.setIsConversion(borrow.getIsConversion());
        tempBorrow.setUserId(userId);
        tempBorrow.setTenderId(tender.getId());
        tempBorrow.setCreatedAt(nowDate);
        tempBorrow.setUpdatedAt(nowDate);
        tempBorrow.setMost(0);
        tempBorrow.setMostAuto(0);
        tempBorrow.setAwardType(0);
        tempBorrow.setAward(0);
        tempBorrow.setPassword("");
        tempBorrow.setMoneyYes(0);
        tempBorrow.setTenderCount(0);

        //锁定资产表
        assetService.findByUserIdLock(userId);

        Specification<Borrow> borrowSpecification = Specifications
                .<Borrow>and()
                .in("status", 0, 1)
                .build();

        long rsnum = borrowService.count(borrowSpecification);
        if (rsnum > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "您已经有一个进行中的借款标"));
        }

        borrowService.insert(tempBorrow);//插入转让标

        tender.setTransferFlag(1);
        tender.setUpdatedAt(nowDate);
        tenderService.updateById(tender);

        return ResponseEntity.ok(VoBaseResp.ok("立即转让成功!"));
    }
}
