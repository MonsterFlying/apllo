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
import com.google.common.base.Preconditions;
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
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private TenderRepository tenderRepository;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;

    /**
     * 初审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doFirstVerify(Map<String, String> msg) throws Exception {
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
        log.info(String.format("触发标的初审: %s", borrowId));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
            return true;
        }

        if (!ObjectUtils.isEmpty(borrow.getLendId())) {
            return verifyLendBorrow(borrow);      //有草出借初审
        } else {
            return verifyStandardBorrow(borrow);  //标准标的初审
        }
    }

    /**
     * 车贷标、净值标、渠道标、转让标初审
     *
     * @return
     */
    private boolean verifyStandardBorrow(Borrow borrow) {
        Date nowDate = DateHelper.subSeconds(new Date(), 10);
        borrow.setStatus(1);
        borrow.setVerifyAt(nowDate);
        Date releaseAt = borrow.getReleaseAt();
        borrow.setReleaseAt(ObjectUtils.isEmpty(releaseAt) ? nowDate : releaseAt);   // 处理不填写发布时间的请款
        borrowService.updateById(borrow);    //更新借款状态

        // 自动投标前提:
        // 1.没有设置标密码
        // 2.车贷标, 渠道标, 流转表
        // 3.标的年化率为 800 以上
        Integer borrowType = borrow.getType();
        ImmutableList<Integer> autoTenderBorrowType = ImmutableList.of(0, 1, 4);
        if ((ObjectUtils.isEmpty(borrow.getPassword()))
                && (autoTenderBorrowType.contains(borrowType)) && borrow.getApr() > 800) {
            borrow.setIsLock(true);
            borrowService.updateById(borrow);  // 锁住标的,禁止手动投标
            if (borrow.getIsNovice()) {   // 对于新手标直接延迟8点后推送
                Date noviceBorrowStandeReaseAt = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);  // 新手标 能进行制动的时间
                releaseAt = DateHelper.max(noviceBorrowStandeReaseAt, releaseAt);
            }

            //触发自动投标队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_AUTO_TENDER);
            mqConfig.setTag(MqTagEnum.AUTO_TENDER);
            mqConfig.setSendTime(releaseAt);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
                return true;
            } catch (Throwable e) {
                log.error("borrowProvider autoTender send mq exception", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 摘草 生成借款 初审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean verifyLendBorrow(Borrow borrow) throws Exception {
        Date nowDate = DateHelper.subSeconds(new Date(), 10);
        borrow.setStatus(1);  //更新借款状态
        borrow.setVerifyAt(nowDate);
        Date releaseAt = borrow.getReleaseAt();
        borrow.setReleaseAt(ObjectUtils.isArray(releaseAt) ? nowDate : releaseAt);
        borrowService.updateById(borrow);   // 更改标的为可投标状态
        Long lendId = borrow.getLendId();

        Preconditions.checkState(ObjectUtils.isArray(lendId), "摘草信息为空");
        Lend lend = lendService.findById(lendId);
        VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
        voCreateTenderReq.setUserId(lend.getUserId());
        voCreateTenderReq.setBorrowId(borrow.getId());
        voCreateTenderReq.setTenderMoney(MathHelper.myRound(borrow.getMoney() / 100.0, 2));
        ResponseEntity<VoBaseResp> response = tenderBiz.createTender(voCreateTenderReq);
        return response.getStatusCode().equals(HttpStatus.OK);


    }

    /**
     * 复审
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doAgainVerify(Map<String, String> msg) throws Exception {
        boolean bool = false;
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get("borrowId")));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if (borrow.getStatus() != 1) {
            log.error("复审：借款状态已发生改变！");
            return false;
        }

        if (borrow.isTransfer()) { //批次债券转让
            VoThirdBatchCreditInvest voThirdBatchCreditInvest = new VoThirdBatchCreditInvest();
            voThirdBatchCreditInvest.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = tenderThirdBiz.thirdBatchCreditInvest(voThirdBatchCreditInvest);
            if (ObjectUtils.isEmpty(resp)) {
                log.info("====================================================================");
                log.info("转让标发起复审成功！");
                log.info("====================================================================");
                bool = true;
            } else {
                log.info("====================================================================");
                log.info("转让标发起复审失败！ msg:" + resp.getBody().getState().getMsg());
                log.info("====================================================================");
            }
        } else {  // 标准标的购买
            if (ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                borrow.setSuccessAt(new Date());
                borrowService.updateById(borrow);
            }

            //批次放款
            VoThirdBatchLendRepay voThirdBatchLendRepay = new VoThirdBatchLendRepay();
            voThirdBatchLendRepay.setBorrowId(borrowId);
            ResponseEntity<VoBaseResp> resp = borrowRepaymentThirdBiz.thirdBatchLendRepay(voThirdBatchLendRepay);

            if (ObjectUtils.isEmpty(resp)) {
                log.info("====================================================================");
                log.info("非转让标发起复审成功！");
                log.info("====================================================================");
                bool = true;
                Specification specification = Specifications.<Tender>and()
                        .eq("borrowId", borrowId)
                        .eq("status", TenderConstans.SUCCESS)
                        .build();
                List<Tender> tenders = tenderRepository.findAll(specification);

                //======================================
                // 老用户投标红包
                //======================================
                tenders.stream().forEach(p -> {
                    UserCache userCache = userCacheService.findById(p.getUserId());
                    Map<String, String> paramsMap = newHashMap();
                    MqConfig mqConfig = new MqConfig();
                    // 非新手标  是新手标但是老用投
                    boolean access = (!borrow.getIsNovice()) || (borrow.getIsNovice() && (userCache.getTenderTuijian() || userCache.getTenderQudao()));
                    if (access) {
                        paramsMap.put("type", RedPacketContants.OLD_USER_TENDER_BORROW_REDPACKAGE);
                        paramsMap.put("tenderId", p.getId().toString());
                        paramsMap.put("time", DateHelper.dateToString(new Date()));

                        mqConfig.setMsg(paramsMap);
                        mqConfig.setTag(MqTagEnum.OLD_USER_TENDER);
                        mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 120));
                        mqHelper.convertAndSend(mqConfig);
                    }

                    //======================================
                    // 推荐用户投资红包
                    //======================================
                    paramsMap.clear();
                    paramsMap.put("type", RedPacketContants.INVITE_USER_TENDER_BORROW_REDPACKAGE);
                    paramsMap.put("tenderId", p.getId().toString());
                    paramsMap.put("time", DateHelper.dateToString(new Date()));
                    mqConfig.setMsg(paramsMap);
                    mqConfig.setTag(MqTagEnum.INVITE_USER_TENDER);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                    mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 160));
                    mqHelper.convertAndSend(mqConfig);
                });
            } else {
                log.info("====================================================================");
                log.info("非转让标发起复审失败！ msg:" + resp.getBody().getState().getMsg());
                log.info("====================================================================");
            }
        }
        return bool;
    }
}
