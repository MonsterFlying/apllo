package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
public class TenderBizImpl implements TenderBiz {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserService userService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;

    public ResponseEntity<VoBaseResp> createTender(VoCreateTenderReq voCreateTenderReq) {
        Long userId = voCreateTenderReq.getUserId();
        Long borrowId = voCreateTenderReq.getBorrowId();
        boolean isAutoTender = voCreateTenderReq.getIsAutoTender(); //是否是自动投标
        Date nowDate = new Date();

        Borrow borrow = borrowService.findByIdLock(borrowId);//投标锁定借款
        Preconditions.checkNotNull(borrow, "你看到的借款消失啦!");

        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "用户不存在!");

        if (users.getIsLock()) { //判断当前会员是否锁定
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被冻结操作，请联系客服人员!"));
        }

        if (!userService.checkRealname(users)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未实名认证!"));
        }

        if (!borrowService.checkBidding(borrow)) { //检查借款状态是否实在招标中
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款不在招标中!"));
        }

        if (!borrowService.checkReleaseAt(borrow)) { //检查借款是否到发布时间
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款未到招标时间!"));
        }

        if (!borrowService.checkValidDay(borrow)) { //检查是否是有效的招标时间
            /**
             * @// TODO: 2017/5/31 调用取消借款函数
             */
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款不在有效招标时间中!"));
        }

        if (tenderService.checkTenderNimiety(borrowId, userId)) {//判断是否频繁投标
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标太过于频繁!"));
        }

        UserCache userCache = userCacheService.findById(userId);
        Preconditions.checkNotNull(users, "用户缓存不存在!");

        if (!isAutoTender && borrow.getIsLock()) { //如果并非是自动投标  并且借款已经锁定
            Date releaseAt = borrow.getReleaseAt();
            if (borrow.getIsNovice()) {
                releaseAt = DateHelper.max(DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20), releaseAt);
            }
            if (!ObjectUtils.isEmpty(releaseAt) && (nowDate.getTime() > releaseAt.getTime())) {
                //添加队列自动投标
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("borrowId", StringHelper.toString(borrowId)); // 借款id
                Gson gson = new Gson();
                String transactionId = System.currentTimeMillis() + RandomHelper.generateNumberCode(4);
                msgMap.put("transactionId", transactionId);

                jmsMessagingTemplate.convertAndSend(ActiveMQConfig.AUTO_TENDER_QUEUE, gson.toJson(msgMap));

                //记录消息状态
                MqMsgLog mqMsgLog = new MqMsgLog();

                mqMsgLog.setCreateDate(new Date());
                mqMsgLog.setMsg(gson.toJson(msgMap));
                mqMsgLog.setTransactionId(transactionId);
                mqMsgLog.setUpdateDate(new Date());
                mqMsgLog.setType(3);
                mqMsgLogService.addMqLog(mqMsgLog);
            }

            if (borrow.getIsNovice()) {
                if (userCache.getTenderTuijian() || userCache.getTenderQudao()) {
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, "新手标只对未投过车贷标的用户在20点之前开放投标!"));
                }
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "该借款已被锁定，暂时不能进行投标，请稍后再试!"));
            }
        }

        return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
    }

    private }
