package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.FrzFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.borrow.vo.request.VoCreateThirdBorrowReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUserWarpListRes;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private MqHelper mqHelper;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;

    /**
     * 新版投标
     *
     * @param voCreateTenderReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> createTender(VoCreateTenderReq voCreateTenderReq) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voCreateTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "当前用户未开户状态"));
        }
        Users user = userService.findByIdLock(voCreateTenderReq.getUserId());
        Borrow borrow = borrowService.findByIdLock(voCreateTenderReq.getBorrowId());
        Asset asset = assetService.findByUserIdLock(voCreateTenderReq.getUserId());
        UserCache userCache = userCacheService.findByUserIdLock(voCreateTenderReq.getUserId());
        Multiset<String> extendMessage = HashMultiset.create();
        // 标的判断
        boolean state = verifyBorrowInfo4Borrow(borrow, user, voCreateTenderReq, extendMessage);
        if (!state) {
            Set<String> errorSet = extendMessage.elementSet();
            Iterator<String> iterator = errorSet.iterator();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, iterator.next()));
        }

        // 借款用户资产判断
        state = verifyUserInfo4Borrow(user, borrow, asset, userCache, voCreateTenderReq, extendMessage);
        Set<String> errorSet = extendMessage.elementSet();
        Iterator<String> iterator = errorSet.iterator();

        if (!state) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, iterator.next()));
        }

        //===========================================================
        //创建标的：投标行为 非转让标第一次成功前创建标的，
        //===========================================================
        ResponseEntity<VoBaseResp> resp;

        String productId = borrow.getProductId();
        if (ObjectUtils.isEmpty(productId) && !borrow.isTransfer()) { //判断没有在即信注册、并且类型为非转让标
            //标的登记
            int type = borrow.getType();
            if (type != 0 && type != 4) { //判断是否是官标、官标不需要在这里登记标的
                VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
                voCreateThirdBorrowReq.setBorrowId(borrow.getId());
                resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
                if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) { //创建状态为失败时返回错误提示
                    return resp;
                }
            }
        }

        Date nowDate = new Date();
        int validateMoney = Integer.parseInt(iterator.next());

        // 生成投标记录
        Tender borrowTender = createBorrowTenderRecord(voCreateTenderReq, user, nowDate, validateMoney);

        //===========================================================
        //投资人投标:类型为非转让标
        //===========================================================
        if (!borrow.isTransfer()) { //类型为非转让标
            VoCreateThirdTenderReq voCreateThirdTenderReq = new VoCreateThirdTenderReq();
            voCreateThirdTenderReq.setAcqRes(String.valueOf(borrowTender.getId()));
            voCreateThirdTenderReq.setUserId(borrowTender.getUserId());
            voCreateThirdTenderReq.setTxAmount(StringHelper.formatDouble(borrowTender.getValidMoney(), 100, false));
            voCreateThirdTenderReq.setProductId(borrow.getProductId());
            voCreateThirdTenderReq.setFrzFlag(FrzFlagContant.FREEZE);
            resp = tenderThirdBiz.createThirdTender(voCreateThirdTenderReq);
            if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) {
                String msg = "tenderBizImpl createTender: tenderId->" + borrowTender.getId() + "msg->" + resp.getBody().getState().getMsg();
                throw new Exception(msg);
            }
        }

        borrowTender.setIsThirdRegister(true);
        tenderService.updateById(borrowTender);

        // 扣除待还
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(borrowTender.getUserId());
        entity.setToUserId(borrow.getUserId());
        entity.setMoney(borrowTender.getValidMoney());
        entity.setRemark("投标冻结资金");
        if (!capitalChangeHelper.capitalChange(entity)) {
            throw new Exception("资金操作失败！");
        }

        borrow.setMoneyYes(borrow.getMoneyYes() + validateMoney);
        borrow.setTenderCount((borrow.getTenderCount() + 1));
        borrow.setId(borrow.getId());
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);
        // 对于投标金额等于招标金额触发复审
        if (borrow.getMoneyYes() >= borrow.getMoney()) {
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
            mqConfig.setTag(MqTagEnum.AGAIN_VERIFY);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            log.info(String.format("tenderBizImpl tender send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        }

        return ResponseEntity.ok(VoBaseResp.ok("投资成功"));
    }

    /**
     * 生成投标记录
     *
     * @param voCreateTenderReq
     * @param user
     * @param nowDate
     * @param validateMoney
     * @return
     */
    private Tender createBorrowTenderRecord(VoCreateTenderReq voCreateTenderReq, Users user, Date nowDate, int validateMoney) {
        Tender borrowTender = new Tender();
        borrowTender.setUserId(user.getId());
        borrowTender.setBorrowId(voCreateTenderReq.getBorrowId());
        borrowTender.setStatus(1);
        borrowTender.setMoney(NumberHelper.toInt(voCreateTenderReq.getTenderMoney()));
        borrowTender.setValidMoney(validateMoney);
        borrowTender.setSource(voCreateTenderReq.getTenderSource());
        Integer autoOrder = voCreateTenderReq.getAutoOrder();
        borrowTender.setAutoOrder(ObjectUtils.isEmpty(autoOrder) ? 0 : autoOrder);
        borrowTender.setIsAuto(voCreateTenderReq.getIsAutoTender());
        borrowTender.setUpdatedAt(nowDate);
        borrowTender.setCreatedAt(nowDate);
        borrowTender.setTransferFlag(0);
        borrowTender.setState(1);
        borrowTender = tenderService.save(borrowTender);
        return borrowTender;
    }


    /**
     * 借款用户审核检查
     * <p>
     * 主要做一下教研:
     * 1. 用户是否锁定
     * 2.投标是否满足最小投标原则
     * 3.有效金额是否大于自动投标设定的最大投标金额
     * 4.存管金额匹配
     * 4.账户有效金额匹配
     *
     * @param user
     * @param borrow
     * @param asset
     * @param userCache
     * @param voCreateTenderReq
     * @param extendMessage     @return
     */
    private boolean verifyUserInfo4Borrow(Users user, Borrow borrow, Asset asset, UserCache userCache, VoCreateTenderReq voCreateTenderReq, Multiset<String> extendMessage) {
        // 判断用户是否已经锁定
        if (user.getIsLock()) {
            extendMessage.add("当前用户属于锁定状态, 如有问题请联系客户!");
            return false;
        }

        // 判断最小投标金额
        int realTenderMoney = borrow.getMoney() - borrow.getMoneyYes();  // 剩余金额
        int minLimitTenderMoney = ObjectUtils.isEmpty(borrow.getLowest()) ? 50 * 100 : borrow.getLowest();  // 最小投标金额
        int realMiniTenderMoney = Math.min(realTenderMoney, minLimitTenderMoney);  // 获取最小投标金额
        if (realMiniTenderMoney > voCreateTenderReq.getTenderMoney()) {
            extendMessage.add("小于标的最小投标金额!");
            return false;
        }

        // 真实有效投标金额
        int invaildataMoney = Math.min(realTenderMoney, voCreateTenderReq.getTenderMoney().intValue());
        if (voCreateTenderReq.getIsAutoTender()) {
            // 对于设置最大自动投标金额进行判断
            if (borrow.getMostAuto() > 0) {
                invaildataMoney = Math.min(borrow.getMostAuto() - borrow.getMoneyYes(), invaildataMoney);
            }

            if ((invaildataMoney <= 0) || (invaildataMoney < minLimitTenderMoney)) {
                extendMessage.add("该借款已达到自投限额!");
                return false;
            }
        }

        if (invaildataMoney > asset.getUseMoney()) {
            extendMessage.add("您的账户可用余额不足,请先充值!");
            return false;
        }

        // 查询存管系统资金
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
            extendMessage.add("当前网络不稳定,请稍后重试!");
            return false;
        }

        double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100.0;// 可用余额  账面余额-可用余额=冻结金额
        if (availBal < invaildataMoney) {
            extendMessage.add("资金账户未同步，请先在个人中心进行资金同步操作!");
            return false;
        }

        extendMessage.add(String.valueOf(invaildataMoney));
        return true;
    }


    /**
     * 验证标的信息是否符合投标要求
     * 主要从:
     * 1.标的状态
     * 2.招标开始时间
     * 3.标的结束时间
     * 4.是否频繁投标
     * 5.投标密码判断
     * 6.债转转让是否与投标同一个人
     * 注意当标的已经过了招标时间, 会进行取消借款操作
     *
     * @param borrow
     * @param user
     * @param voCreateTenderReq
     * @param errerMessage      @return
     */
    private boolean verifyBorrowInfo4Borrow(Borrow borrow, Users user, VoCreateTenderReq voCreateTenderReq, Multiset<String> errerMessage) throws Exception {
        if (!(borrow.getStatus() == 1 && borrow.getMoneyYes() < borrow.getMoney())) {
            errerMessage.add("标的未在招标状态, 如有疑问请联系客服!");
            return false;
        }

        Date nowDate = new Date();
        Date releaseAt;
        if (borrow.getIsNovice()) {  // 新手
            releaseAt = DateHelper.max(DateHelper.setHours(nowDate, 20), borrow.getReleaseAt());
        } else { // 普通标的
            releaseAt = borrow.getReleaseAt();
        }

        if (ObjectUtils.isEmpty(borrow.getLendId()) && (releaseAt.getTime() > nowDate.getTime())) {
            errerMessage.add("当前标的未到发布时间!");
            return false;
        }

        Date endDate = DateHelper.addDays(DateHelper.beginOfDate(releaseAt), borrow.getValidDay() + 1);
        if (endDate.getTime() < nowDate.getTime()) {
            // 流标
            log.info("==========================================");
            log.info(String.format("标的流标操作: %s", GSON.toJson(borrow)));
            log.info("==========================================");
            VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
            voCancelBorrow.setBorrowId(borrow.getId());
            voCancelBorrow.setUserId(borrow.getUserId());
            ResponseEntity<VoBaseResp> voBaseRespResponseEntity = borrowBiz.cancelBorrow(voCancelBorrow);
            if (voBaseRespResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                errerMessage.add("当前标的已经超过招标时间");
            } else {
                errerMessage.add(voBaseRespResponseEntity.getBody().getState().getMsg());
            }
            return false;
        }

        // 判断投标频繁
        if (tenderService.checkTenderNimiety(borrow.getId(), user.getId())) {
            errerMessage.add("投标太过于频繁!");
            return false;
        }

        // 投标密码判断
        if (!StringUtils.isEmpty(borrow.getPassword())) {
            if (StringUtils.isEmpty(voCreateTenderReq.getBorrowPassword())) {
                errerMessage.add("投标密码不能为空!");
                return false;
            }

            if (!PasswordHelper.verifyPassword(borrow.getPassword(), voCreateTenderReq.getBorrowPassword())) {
                errerMessage.add("借款密码验证失败, 请重新输入!");
                return false;
            }
        }

        // 借款自投判断
        if (borrow.getUserId().equals(user.getId())) {
            errerMessage.add("不能投自己发布借款!");
            return false;
        }

        // 债转自投判断
        if (borrow.isTransfer()) {
            Tender tender = tenderService.findById(borrow.getTenderId());
            Borrow tempBorrow = borrowService.findById(tender.getBorrowId());
            if (user.getId().equals(tempBorrow.getUserId())) {
                errerMessage.add("不能投自己发布或转让的借款!");
                return false;
            }
        }

        return true;
    }

    /**
     * 投标
     *
     * @param voCreateTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> tender(VoCreateTenderReq voCreateTenderReq) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voCreateTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getAutoTransferState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动债权转让协议！", VoAutoTenderInfo.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoAutoTenderInfo.class));
        }


        ResponseEntity<VoBaseResp> voBaseRespResponseEntity = createTender(voCreateTenderReq);
        if (voBaseRespResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
        } else {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "投标失败"));
        }
    }

    /**
     * 投标用户
     *
     * @param tenderUserReq
     * @return
     */
    @Override
    public ResponseEntity<VoBorrowTenderUserWarpListRes> findBorrowTenderUser(TenderUserReq tenderUserReq) {
        try {
            List<VoBorrowTenderUserRes> tenderUserRes = tenderService.findBorrowTenderUser(tenderUserReq);
            VoBorrowTenderUserWarpListRes warpListRes = VoBaseResp.ok("查询成功", VoBorrowTenderUserWarpListRes.class);
            warpListRes.setVoBorrowTenderUser(tenderUserRes);
            return ResponseEntity.ok(warpListRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoBorrowTenderUserWarpListRes.class));
        }
    }


}
