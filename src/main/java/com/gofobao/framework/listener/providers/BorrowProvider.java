package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    public boolean doFirstVerify(Long borrowId) throws Exception {
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
            return false;
        }

        Integer borrowType = borrow.getType();
        boolean bool = false;
        if (borrowType == 2) { //秒标
            bool = miaoBorrow(borrow);
        } else if (!ObjectUtils.isEmpty(borrow.getLendId())) { //转让标
            bool = lendBorrow(borrow);
        } else { //车贷、渠道、净值、转让 标
            bool = baseBorrow(borrow);
        }

        return bool;
    }

    /**
     * 车贷标、净值标、渠道标、转让标初审
     *
     * @return
     */
    private boolean baseBorrow(Borrow borrow) {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer borrowType = borrow.getType();
            if ((ObjectUtils.isEmpty(borrow.getPassword())) && (borrowType == 0 || borrowType == 1 || borrowType == 4) && borrow.getApr() > 800) {
                return false;
            }

            //更新借款状态
            borrow.setIsLock(true);
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Date releaseDate = borrow.getReleaseAt();

            //====================================
            //延时投标
            //====================================
            if (borrow.getIsNovice()) {//判断是否是新手标
                Date tempDate = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);
                releaseDate = DateHelper.max(tempDate, releaseDate);
            }

            //触发自动投标队列

        } while (false);
        return bool;
    }

    /**
     * 摘草 生成借款 初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean lendBorrow(Borrow borrow) throws Exception {
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Long lendId = borrow.getLendId();
            if (!ObjectUtils.isEmpty(lendId)) {
                Lend lend = lendService.findById(lendId);
                VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
                voCreateTenderReq.setUserId(lend.getUserId());
                voCreateTenderReq.setBorrowId(borrow.getId());
                voCreateTenderReq.setTenderMoney(borrow.getMoney());
                Map<String, Object> rsMap = tenderBiz.createTender(voCreateTenderReq);

                Object msg = rsMap.get("msg");
                if (ObjectUtils.isEmpty(msg)) {
                    log.error(StringHelper.toString(msg));
                }
            }
        } while (false);
        return false;
    }

    private boolean miaoBorrow(Borrow borrow) throws Exception {
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Integer payMoney = 0;
            Integer borrowType = borrow.getType();
            if (borrowType != 2) {
                return false;
            }
            Double principal = NumberHelper.toDouble(StringHelper.toString(borrow.getMoney()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal,
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            payMoney = (int) MathHelper.myRound((Double) rsMap.get("repayTotal") - principal, 2);

            if (borrow.getAwardType() == 1) {
                payMoney += borrow.getAward();
            } else if (borrow.getAwardType() == 2) {  //
                payMoney += (int) MathHelper.myRound(borrow.getMoney() * borrow.getAward() / 100 / 100, 2);
            }

            //更新资产记录
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Frozen);
            entity.setUserId(borrow.getUserId());
            entity.setMoney(payMoney);
            entity.setRemark("冻结秒标应付资金");
            capitalChangeHelper.capitalChange(entity);

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);
        } while (false);
        return false;
    }

    public boolean doAgainVerify(Long borrowId) throws Exception {
        do {
            Date nowDate = new Date();

            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 1) || (borrow.getMoney() == borrow.getMoneyYes())){
                return false;
            }

            Long tenderId = borrow.getTenderId();
            Integer repayMoney = 0;
            Integer repayInterest = 0;
            Integer borrowType = borrow.getType();
            if (borrow.isTransfer()){
                //============================更新转让标识=============================
                BorrowCollection borrowCollection = new BorrowCollection();
                borrowCollection.setTransferFlag(1);
                Specification<BorrowCollection> bcs = Specifications.<BorrowCollection>and()
                        .eq("tenderId",tenderId)
                        .eq("status",0)
                        .build();
                borrowCollectionService.updateBySpecification(borrowCollection,bcs);

                Tender tender = new Tender();
                tender.setId(tenderId);
                tender.setTransferFlag(2);
                tenderService.updateById(tender);
                //======================================================================

                //扣除转让待收
                bcs = Specifications.<BorrowCollection>and()
                        .eq("status",0)
                        .eq("transferFlag",1)
                        .build();

                List<BorrowCollection> transferedBorrowCollections = borrowCollectionService.findList(bcs,new Sort(Sort.Direction.DESC,"`order`"));

                Integer collectionMoney = 0;
                Integer collectionInterest = 0;
                for (BorrowCollection temp : transferedBorrowCollections) {
                    collectionMoney += temp.getCollectionMoney();
                    collectionInterest += temp.getInterest();
                }

                //更新资产记录
                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.CollectionLower);
                entity.setUserId(borrow.getUserId());
                entity.setMoney(collectionMoney);
                entity.setInterest(collectionInterest);
                entity.setRemark("债权转让成功，扣除待收资金");
                capitalChangeHelper.capitalChange(entity);
            }else {
                BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(StringHelper.toString(borrow.getMoney())),
                        NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
                Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");

                BorrowRepayment borrowRepayment = new BorrowRepayment();
                for (int i = 0; i < repayDetailList.size(); i++) {
                    Map<String, Object> repayDetailMap = repayDetailList.get(i);
                    repayMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue();
                    repayInterest += new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue();
                    borrowRepayment.setBorrowId(borrow.getId());
                    borrowRepayment.setStatus(0);
                    borrowRepayment.setOrder(i);
                    borrowRepayment.setRepayAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                    borrowRepayment.setRepayMoney(new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).intValue());
                    borrowRepayment.setPrincipal(new Double(NumberHelper.toDouble(repayDetailMap.get("principal"))).intValue());
                    borrowRepayment.setInterest(new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).intValue());
                    borrowRepayment.setRepayMoneyYes(0);
                    borrowRepayment.setCreatedAt(nowDate);
                    borrowRepayment.setUpdatedAt(nowDate);
                    borrowRepayment.setAdvanceMoneyYes(0);
                    borrowRepayment.setLateDays(0);
                    borrowRepayment.setLateInterest(0);
                    //borrowRepaymentService.i(borrowRepayment);
                }
            }


        } while (false);
        return false;
    }
}
