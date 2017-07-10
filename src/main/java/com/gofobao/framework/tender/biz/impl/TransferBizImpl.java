package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.*;
import com.gofobao.framework.tender.vo.response.web.TransferBuy;
import com.gofobao.framework.tender.vo.response.web.VoViewTransferBuyWarpRes;
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
import java.util.Map;

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

    /**
     * 转让中
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(VoTransferReq voTransferReq) {
        try {
            Map<String,Object>resultMaps=transferService.transferOfList(voTransferReq);
            List<TransferOf> transferOfs =(List<TransferOf>) resultMaps.get("transferOfList");
            Integer totalCount=Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferOfWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferOfWarpRes.class);
            voViewTransferOfWarpRes.setTransferOfs(transferOfs);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl tranferOfList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferOfWarpRes.class));
        }
    }

    /**
     * 已转让
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(VoTransferReq voTransferReq) {
        try {
            Map<String,Object>resultMaps=transferService.transferedList(voTransferReq);
            List<Transfered> transfereds =(List<Transfered>) resultMaps.get("transferedList");
            Integer totalCount=Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferedWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferedWarpRes.class);
            voViewTransferOfWarpRes.setTransferedList(transfereds);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl transferedlist query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferedWarpRes.class));
        }
    }

    /**
     * 可转让
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(VoTransferReq voTransferReq) {
        try {
            Map<String,Object>resultMaps=transferService.transferMayList(voTransferReq);
            List<TransferMay> transferOfs =(List<TransferMay>) resultMaps.get("transferMayList");
            Integer totalCount=Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferMayWarpRes voViewTransferOfWarpRes = VoBaseResp.ok("查询成功", VoViewTransferMayWarpRes.class);
            voViewTransferOfWarpRes.setMayList(transferOfs);
            voViewTransferOfWarpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewTransferOfWarpRes);
        } catch (Throwable e) {
            log.info("TransferBizImpl transferMayList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferMayWarpRes.class));
        }
    }

    /**
     * 已购买
     * @param voTransferReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewTransferBuyWarpRes> tranferBuyList(VoTransferReq voTransferReq) {
        try {
            Map<String,Object>resultMaps=transferService.transferBuyList(voTransferReq);
            List<TransferBuy> transferOfs =(List<TransferBuy>) resultMaps.get("transferBuys");
            Integer totalCount=Integer.valueOf(resultMaps.get("totalCount").toString());
            VoViewTransferBuyWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewTransferBuyWarpRes.class);
            warpRes.setTransferBuys(transferOfs);
            warpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("TransferBizImpl transferMayList query fail%S", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewTransferBuyWarpRes.class));
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

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "timeLimit"));
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
                .eq("userId",userId)
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

    /**
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    public ResponseEntity<VoGoTenderInfo> goTenderInfo(Long tenderId, Long userId) {

        if ((ObjectUtils.isEmpty(tenderId)) || (ObjectUtils.isEmpty(userId))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoGoTenderInfo.error(VoGoTenderInfo.ERROR, "参数缺少!", VoGoTenderInfo.class));
        }
        Tender tender = tenderService.findById(tenderId);
        if (!userId.equals(tender.getUserId())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoGoTenderInfo.error(VoGoTenderInfo.ERROR, "只有债权持有人才允许转让!", VoGoTenderInfo.class));
        }

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("tenderId", tenderId)
                .eq("status", 0)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "id"));
        if (CollectionUtils.isEmpty(borrowCollections)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoGoTenderInfo.error(VoGoTenderInfo.ERROR, "未找到回款记录!", VoGoTenderInfo.class));
        }

        BorrowCollection borrowCollection = borrowCollections.get(0);
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        if (ObjectUtils.isEmpty(borrow)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoGoTenderInfo.error(VoGoTenderInfo.ERROR, "未找到借款记录!", VoGoTenderInfo.class));
        }

        String repayFashionStr = "";
        switch (borrow.getRepayFashion()) {
            case BorrowContants.REPAY_FASHION_AYFQ_NUM:
                repayFashionStr = "按月分期";
                break;
            case BorrowContants.REPAY_FASHION_YCBX_NUM:
                repayFashionStr = "一次性还本付息";
                break;
            case BorrowContants.REPAY_FASHION_XXHB_NUM:
                repayFashionStr = "先息后本";
                break;
            default:

        }

        int money = 0;
        for (BorrowCollection bean : borrowCollections) {
            money += bean.getPrincipal();
        }
        // 0.4% + 0.08% * (剩余期限-1)  （费率最高上限为1.28%）
        double rate = 0.004 + 0.0008 * (borrowCollections.size() - 1);
        rate = Math.min(rate, 0.0128);
        Double fee = money * rate;
        int day = DateHelper.diffInDays(borrowCollection.getCollectionAt(), new Date(), false);
        day = day < 0 ? 0 : day;

        VoGoTenderInfo voGoTenderInfo = VoGoTenderInfo.ok("查询成功!", VoGoTenderInfo.class);
        voGoTenderInfo.setTenderId(tender.getId());
        voGoTenderInfo.setApr(StringHelper.formatDouble(borrow.getApr(), 100.0, false));
        voGoTenderInfo.setBorrowName(borrow.getName());
        voGoTenderInfo.setNextRepaymentDate(DateHelper.dateToString(borrowCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
        voGoTenderInfo.setSurplusDate(String.valueOf(day));
        voGoTenderInfo.setRepayFashionStr(repayFashionStr);
        voGoTenderInfo.setTimeLimit(String.valueOf(borrowCollections.size()) + "个月");
        voGoTenderInfo.setMoney(StringHelper.formatDouble(money, 100.0, true));
        voGoTenderInfo.setFee(StringHelper.formatDouble(fee, 100.0, true));

        return ResponseEntity.ok(voGoTenderInfo);
    }
}
