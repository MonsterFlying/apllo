package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.award.contants.RedPacketContants;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    Gson GSON = new GsonBuilder().create();

    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private TenderRepository tenderRepository;


    /**
     * 复审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doAgainVerify(Map<String, String> msg) throws Exception {
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get("borrowId")));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if (borrow.getStatus() != 1) {
            log.error("复审：借款状态已发生改变！");
            return false;
        }

        if (borrow.isTransfer()) { // 批次债券转让
            log.info(String.format("复审: 批量债权转让申请开始: %s", GSON.toJson(msg)));
            VoThirdBatchCreditInvest voThirdBatchCreditInvest = new VoThirdBatchCreditInvest();
            voThirdBatchCreditInvest.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = tenderThirdBiz.thirdBatchCreditInvest(voThirdBatchCreditInvest);
            if (ObjectUtils.isEmpty(resp)) {
                log.info(String.format("复审: 批量债权转让申请成功: %s", GSON.toJson(msg)));
                return true;
            } else {
                log.error(String.format("复审: 批量债权转让申请失败: %s", GSON.toJson(resp)));
                return false;
            }
        } else {  // 批次标准标的放款
            log.info(String.format("复审: 批量正常放款申请开始: %s", GSON.toJson(msg)));
            if (ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                borrow.setSuccessAt(new Date());
                borrow = borrowService.save(borrow);
            }

            //批次放款
            VoThirdBatchLendRepay voThirdBatchLendRepay = new VoThirdBatchLendRepay();
            voThirdBatchLendRepay.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = borrowRepaymentThirdBiz.thirdBatchLendRepay(voThirdBatchLendRepay);
            if (ObjectUtils.isEmpty(resp)) {
                log.info(String.format("复审: 批量正常放款申请申请成功: %s", GSON.toJson(msg)));
                return true;

            } else {
                log.info(String.format("复审: 批量正常放款申请申请失败: %s", GSON.toJson(resp)));
                return false;
            }
        }
    }
}
