package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.FrzFlagContant;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Service
@Slf4j
public class TenderBizImpl implements TenderBiz {

    static final Gson GSON = new Gson();

    @Autowired
    private UserService userService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;

    public Map<String, Object> createTender(VoCreateTenderReq voCreateTenderReq) throws Exception {
        Map<String, Object> rsMap = null;
        Date nowDate = new Date();
        Long userId = voCreateTenderReq.getUserId();

        rsMap = checkCreateTender(voCreateTenderReq);
        Object msg = rsMap.get("msg");
        if (!ObjectUtils.isEmpty(msg)) {
            return rsMap;
        }

        Integer validMoney = (int) Double.parseDouble(rsMap.get("validMoney").toString());
        Borrow borrow = (Borrow) rsMap.get("borrow");

        Tender borrowTender = new Tender();
        borrowTender.setUserId(userId);
        borrowTender.setBorrowId(voCreateTenderReq.getBorrowId());
        borrowTender.setStatus(1);
        borrowTender.setMoney(voCreateTenderReq.getTenderMoney());
        borrowTender.setValidMoney(validMoney);
        borrowTender.setSource(voCreateTenderReq.getTenderSource());
        Integer autoOrder = voCreateTenderReq.getAutoOrder();
        borrowTender.setAutoOrder(ObjectUtils.isEmpty(autoOrder) ? 0 : autoOrder);
        borrowTender.setIsAuto(voCreateTenderReq.getIsAutoTender());
        borrowTender.setUpdatedAt(nowDate);
        borrowTender.setCreatedAt(nowDate);
        borrowTender.setTransferFlag(0);
        borrowTender.setState(1);
        borrowTender = tenderService.insert(borrowTender);

        //扣除待还
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(borrowTender.getUserId());
        entity.setToUserId(borrow.getUserId());
        entity.setMoney(borrowTender.getValidMoney());
        entity.setRemark("投标冻结资金");
        if (!capitalChangeHelper.capitalChange(entity)) {
            throw new Exception("资金操作失败！");
        }

        borrow.setMoneyYes(borrow.getMoneyYes() + validMoney);
        borrow.setTenderCount((borrow.getTenderCount() + 1));
        borrow.setId(borrow.getId());
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);

        if (borrow.getMoneyYes() >= borrow.getMoney()) {
            //复审
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
            mqConfig.setTag(MqTagEnum.AGAIN_VERIFY);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("tenderBizImpl againVerify send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Exception e) {
                log.error("tenderBizImpl againVerify send mq exception", e);
                throw new Exception(e);
            }
        }

        return rsMap;
    }


    /**
     * 投标
     *
     * @param voCreateTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> tender(VoCreateTenderReq voCreateTenderReq) {
        Map<String, Object> rsMap = null;
        try {
            rsMap = createTender(voCreateTenderReq);
        } catch (Exception e) {
            log.error("创建投标异常：", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("投标失败!")));
        }
        Object msg = rsMap.get("msg");
        if (!ObjectUtils.isEmpty(msg)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString(msg)));
        }
        return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
    }

    /**
     * 检查投标前置条件
     *
     * @param voCreateTenderReq
     * @return
     */
    public Map<String, Object> checkCreateTender(VoCreateTenderReq voCreateTenderReq) {
        Map<String, Object> rsMap = new HashMap<>();
        String msg = null;

        do {
            Long userId = voCreateTenderReq.getUserId();
            Long borrowId = voCreateTenderReq.getBorrowId();
            boolean isAutoTender = voCreateTenderReq.getIsAutoTender(); //是否是自动投标
            String inputBorrowPassword = voCreateTenderReq.getBorrowPassword();
            String inputPayPassword = voCreateTenderReq.getPayPassword();
            Date nowDate = new Date();

            Borrow borrow = borrowService.findByIdLock(borrowId);//投标锁定借款
            Preconditions.checkNotNull(borrow, "你看到的借款消失啦!");

            Users users = userService.findByIdLock(userId);
            Preconditions.checkNotNull(users, "用户不存在!");

            if (users.getIsLock()) { //判断当前会员是否锁定
                msg = "当前用户已被冻结操作，请联系客服人员!";
                break;
            }

            if (!userService.checkRealname(users)) {
                msg = "当前用户未实名认证!";
                break;
            }

            if (!borrowService.checkBidding(borrow)) { //检查借款状态是否实在招标中
                msg = "当前借款不在招标中!";
                break;
            }

            if (!borrowService.checkReleaseAt(borrow)) { //检查借款是否到发布时间
                msg = "当前借款未到招标时间!";
                break;
            }

            if (!borrowService.checkValidDay(borrow)) { //检查是否是有效的招标时间
                //取消借款
                VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
                voCancelBorrow.setBorrowId(borrowId);
                voCancelBorrow.setUserId(borrow.getUserId());
                borrowBiz.cancelBorrow(voCancelBorrow);
                msg = "当前借款不在有效招标时间中!";
                break;
            }

            if (tenderService.checkTenderNimiety(borrowId, userId)) {//判断是否频繁投标
                msg = "投标太过于频繁!";
                break;
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
                    String transactionId = System.currentTimeMillis() + RandomHelper.generateNumberCode(4);

                    //触发自动投标队列
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                    mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
                    ImmutableMap<String, String> body = ImmutableMap
                            .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                    mqConfig.setMsg(body);
                    mqConfig.setSendTime(releaseAt);
                    try {
                        log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Exception e) {
                        log.error("borrowProvider autoTender send mq exception", e);
                    }
                }

                if (borrow.getIsNovice()) {
                    if (userCache.getTenderTuijian() || userCache.getTenderQudao()) {
                        msg = "新手标只对未投过车贷标的用户在20点之前开放投标!";
                        break;
                    }
                } else {
                    msg = "该借款已被锁定，暂时不能进行投标，请稍后再试!";
                    break;
                }
            }

            boolean bool = StringUtils.isEmpty(inputBorrowPassword);
            String borrowPassword = borrow.getPassword();
            if ((StringUtils.isEmpty(borrowPassword)) && (!StringUtils.isEmpty(inputBorrowPassword))) {
                try {
                    bool = PasswordHelper.verifyPassword(borrowPassword, inputBorrowPassword);
                } catch (Exception e) {
                    log.error("借款密码验证异常:", e);
                    msg = "借款密码验证失败!";
                    break;
                }
            }

            if (!bool) {
                msg = "借款密码输入错误!";
                break;
            }

            if (!StringUtils.isEmpty(inputPayPassword)) {
                String payPassword = users.getPayPassword();
                if (StringUtils.isEmpty(payPassword)) {
                    msg = "用户交易密码不存在!";
                    break;
                }

                try {
                    bool = PasswordHelper.verifyPassword(payPassword, inputPayPassword);
                } catch (Exception e) {
                    log.error("支付密码验证异常:", e);
                    msg = "支付密码验证失败!";
                    break;
                }

                if (!bool) {
                    msg = "支付密码不正确!";
                    break;
                }
            }

            Long tenderId = borrow.getTenderId();
            if (!ObjectUtils.isEmpty(tenderId)) {  //验证转让标原借款用户是否是当前用户
                Tender tender = tenderService.findById(tenderId);
                Preconditions.checkNotNull(tender, "投标信息不存在!");

                Borrow tempBorrow = borrowService.findById(tender.getBorrowId());

                if (userId == tempBorrow.getUserId()) {
                    msg = "不能投自己发布或转让的借款!";
                    break;
                }
            }

            //判断是否是当前用户的borrow
            if (userId.equals(borrow.getUserId())) {//判断是否是当前用户的借款
                msg = "不能投自己发布或转让的借款!";
                break;
            }

            double tenderMoney = voCreateTenderReq.getTenderMoney();
            if (tenderMoney < MathHelper.myRound(Math.min(borrow.getLowest(), borrow.getMoney() - borrow.getMoneyYes()), 2)) {
                msg = "投标金额必须大于起投金额!";
                break;
            }


            double validMoney = MathHelper.myRound(Math.min(borrow.getMoney() - borrow.getMoneyYes(), tenderMoney), 2);
            if (isAutoTender) {
                if (borrow.getMostAuto() > 0) {
                    validMoney = MathHelper.myRound(Math.min(borrow.getMostAuto() - borrow.getMoneyYes(), validMoney), 2);
                }

                if (validMoney <= 0) {
                    msg = "该借款已达到自投限额!";
                    break;
                }

                Integer lowest = voCreateTenderReq.getLowest();
                if (lowest > 0 && validMoney < lowest) {  //自动投标单笔最低投标额
                    msg = "该借款已达到自投限额!";
                    break;
                }
            }

            Asset asset = assetService.findByUserIdLock(userId);
            Preconditions.checkNotNull(users, "用户资产不存在!");

            if (validMoney > asset.getUseMoney()) {
                msg = "您的账户可用余额不足,请先充值!";
                break;
            }

            rsMap.put("validMoney", validMoney);
            rsMap.put("borrow", borrow);
            rsMap.put("asset", asset);
        } while (false);
        rsMap.put("msg", msg);
        return rsMap;
    }

    /**
     * 投标用户
     *
     * @param borrowId
     * @return
     */
    @Override
    public ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser(Long borrowId) {
        try {
            List<VoBorrowTenderUserRes> tenderUserRes = tenderService.findBorrowTenderUser(borrowId);
            VoBorrowTenderUserWarpListRes warpListRes = VoBaseResp.ok("查询成功", VoBorrowTenderUserWarpListRes.class);
            warpListRes.setVoBorrowTenderUser(tenderUserRes);
            return ResponseEntity.ok(warpListRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoBorrowTenderUserWarpListRes.class));
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
