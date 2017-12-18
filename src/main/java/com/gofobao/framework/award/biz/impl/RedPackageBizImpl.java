package com.gofobao.framework.award.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelRequest;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class RedPackageBizImpl implements RedPackageBiz {
    @Autowired
    MqHelper mqHelper;

    @Autowired
    MarketingProcessBiz marketingProcessBiz;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    UserService userService;

    @Autowired
    AssetService assetService;

    @Autowired
    BorrowService borrowService;

    @Autowired
    TenderService tenderService;

    @Autowired
    NewAssetLogService newAssetLogService;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commonPublishRedpack(Long userId,
                                        long money,
                                        AssetChangeTypeEnum assetChangeTypeEnum,
                                        String onlyNo,
                                        String remark,
                                        long sourceId) throws Exception {
        Preconditions.checkArgument(sourceId > 0, "sourceId 不能为空");
        Preconditions.checkArgument(!StringUtils.isEmpty(onlyNo), "onleyNo 不能为空");
        Users user = userService.findByIdLock(userId);
        if (user.getIsLock()) {
            log.error("通用打开红包, 当前用户处于冻结状态!");
            throw new Exception("通用打开红包, 当前用户处于冻结状态!");
        }

        Preconditions.checkNotNull(user, "publishRedpack user record is null");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userThirdAccount is null");
        Long redpackAccountId = assetChangeProvider.getRedpackAccountId();
        Asset redpackAsset = assetService.findByUserIdLock(redpackAccountId);
        Preconditions.checkNotNull(redpackAsset, "publishRedpack redpackAsset is null");

        if (redpackAsset.getUseMoney() - money < 0) {
            exceptionEmailHelper.sendErrorMessage("红包余额不足警告",
                    String.format("当前红包余额: %s 元", StringHelper.formatDouble(redpackAsset.getUseMoney() / 100, true)));
            throw new Exception("非常抱歉, 红包账户余额不足, 请致电平台客服! (红包还可以继续使用)");
        }

        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        // 判断用户是否派发过
        Specification<NewAssetLog> specification = Specifications
                .<NewAssetLog>and()
                .eq("userId", userId)
                .eq("localType", assetChangeTypeEnum.getLocalType())
                .eq("groupOpSeqNo", onlyNo)
                .build();
        long count = newAssetLogService.count(specification);
        if (count > 0) {
            throw new Exception("红包已经派发");
        }

        // 派发红包
        Gson gson = new Gson();
        double doubleMoney = MoneyHelper.divide(money, 100, 2);
        VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
        // 红包账户
        voucherPayRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayRequest.setTxAmount(doubleMoney + "");
        voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
        voucherPayRequest.setDesLine(onlyNo);
        voucherPayRequest.setChannel(ChannelContant.HTML);
        VoucherPayResponse voucherPayResponse = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
        log.info(String.format("开始派发红包:%s", gson.toJson(voucherPayRequest)));
        if ((ObjectUtils.isEmpty(voucherPayResponse))
                || (!JixinResultContants.SUCCESS.equals(voucherPayResponse.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(voucherPayResponse) ? "当前网络不稳定，请稍候重试" : voucherPayResponse.getRetMsg();
            log.error(String.format("派发红包异常, 主动撤回: %s", msg));
            cancelOpenRedpack(onlyNo, userThirdAccount, redpackAccount, doubleMoney, voucherPayRequest);
            return false;
        }
        log.info(String.format("结束派发红包:%s", gson.toJson(voucherPayResponse)));

        // 执行资金变动
        try {
            // 红包账户发送红包
            AssetChange redpackPublish = new AssetChange();
            redpackPublish.setMoney(money);
            redpackPublish.setType(AssetChangeTypeEnum.publishRedpack);
            redpackPublish.setUserId(redpackAccountId);
            redpackPublish.setRemark(String.format("平台派发奖励红包 %s元", StringHelper.formatDouble(money / 100D, true)));
            redpackPublish.setGroupSeqNo(onlyNo);
            redpackPublish.setSeqNo(String.format("%s%s%s", voucherPayRequest.getTxDate(), voucherPayRequest.getTxTime(), voucherPayRequest.getSeqNo()));
            redpackPublish.setForUserId(userId);
            redpackPublish.setSourceId(sourceId);
            assetChangeProvider.commonAssetChange(redpackPublish);

            if (StringUtils.isEmpty(remark)) {
                remark = String.format("领取奖励红包 %s元", StringHelper.formatDouble(money / 100D, true));
            }
            // 用户接收红包
            AssetChange redpackR = new AssetChange();
            redpackR.setMoney(money);
            redpackR.setType(assetChangeTypeEnum);
            redpackR.setUserId(userId);
            redpackR.setRemark(remark);
            redpackR.setGroupSeqNo(onlyNo);
            redpackR.setSeqNo(String.format("%s%s%s", voucherPayRequest.getTxDate(), voucherPayRequest.getTxTime(), voucherPayRequest.getSeqNo()));
            redpackR.setForUserId(redpackAccountId);
            redpackR.setSourceId(sourceId);
            assetChangeProvider.commonAssetChange(redpackR);
            return true;
        } catch (Exception e) {
            log.error("本地资金变动失败, 进行红包回撤", e);
            cancelOpenRedpack(onlyNo, userThirdAccount, redpackAccount, doubleMoney, voucherPayRequest);
            throw new Exception("打开红包, 本地资金变动失败!");
        }
    }

    /**
     * 取消红包
     *
     * @param onlyNo            唯一的派发红包
     * @param userThirdAccount  用户开户信息
     * @param redpackAccount    红包账户
     * @param doubleMoney       派发金额
     * @param voucherPayRequest 派发红包请求类
     */
    private void cancelOpenRedpack(String onlyNo,
                                   UserThirdAccount userThirdAccount,
                                   UserThirdAccount redpackAccount,
                                   double doubleMoney,
                                   VoucherPayRequest voucherPayRequest) {
        log.info("[红包取消] 红包取消开始");
        VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
        voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayCancelRequest.setTxAmount(doubleMoney + "");
        voucherPayCancelRequest.setOrgTxDate(voucherPayRequest.getTxDate());
        voucherPayCancelRequest.setOrgTxTime(voucherPayRequest.getTxTime());
        voucherPayCancelRequest.setOrgSeqNo(voucherPayRequest.getSeqNo());
        // 撤销的账号
        voucherPayCancelRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayCancelRequest.setAcqRes(onlyNo);
        voucherPayCancelRequest.setChannel(ChannelContant.HTML);
        voucherPayCancelRequest.setAcqRes(onlyNo);
        int looper = 5;
        VoucherPayCancelResponse voucherPayCancelResponse = null;
        do {
            looper--;
            voucherPayCancelRequest.setTxDate(null);
            voucherPayCancelRequest.setTxTime(null);
            voucherPayCancelRequest.setSeqNo(null);
            voucherPayCancelResponse = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET,
                    voucherPayCancelRequest,
                    VoucherPayCancelResponse.class);
            if (JixinResultContants.isNetWordError(voucherPayCancelResponse)
                    || JixinResultContants.isBusy(voucherPayCancelResponse)) {
                log.warn("红包取消异常进行重新尝试");
                try {
                    Thread.sleep(2 * 1000L);
                } catch (Exception e) {
                }
            }

            if (JixinResultContants.SUCCESS.equals(voucherPayCancelResponse.getRetCode())) {
                log.warn("红包撤销成功");
                break;
            }
        } while (looper > 0);

        log.info("[红包取消] 红包取消结束");
    }


    @Override
    public ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq) {
        Pageable pageable = new PageRequest(voRedPackageReq.getPageIndex(), voRedPackageReq.getPageSize(), new Sort(Sort.Direction.DESC, "id"));
        VoViewRedPackageWarpRes voViewRedPackageWarpRes = VoBaseResp.ok("查询成功", VoViewRedPackageWarpRes.class);
        List<MarketingRedpackRecord> marketingRedpackRecords = marketingRedpackRecordService.findByUserIdAndState(voRedPackageReq.getUserId(), voRedPackageReq.getStatus(), pageable);

        RedPackageRes redPackageRes = null;
        // 遍历
        for (MarketingRedpackRecord item : marketingRedpackRecords) {
            if (item.getMarketingId() == 1) {
                item.setMarketingId(2L);
            } else if (item.getMarketingId() == 2) {
                item.setMarketingId(3L);
            } else if (item.getMarketingId() == 3) {
                item.setMarketingId(1L);
            } else {
                item.setMarketingId(4L);
            }
            redPackageRes = new RedPackageRes();
            redPackageRes.setExpiryDate(DateHelper.dateToString(item.getPublishTime(), DateHelper.DATE_FORMAT_YMDHM)
                    + "~" + DateHelper.dateToString(item.getCancelTime(), DateHelper.DATE_FORMAT_YMDHM));  // 有效时间
            redPackageRes.setMoney(StringHelper.formatMon(item.getMoney() / 100D));
            redPackageRes.setRedPackageId(item.getId());
            redPackageRes.setTitle(item.getMarkeingTitel());
            redPackageRes.setType(item.getMarketingId().intValue());
            voViewRedPackageWarpRes.getResList().add(redPackageRes);
        }
        return ResponseEntity.ok(voViewRedPackageWarpRes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoViewOpenRedPackageWarpRes> openRedPackage(VoOpenRedPackageReq packageReq) throws Exception {
        MarketingRedpackRecord marketingRedpackRecord = marketingRedpackRecordService.findTopByIdAndUserIdAndDel(packageReq.getRedPackageId(), packageReq.getUserId(), 0);
        if (ObjectUtils.isEmpty(marketingRedpackRecord)) {
            log.error("打开红包失败,该红包id不存在 或者已过期: {redPackageId:" + packageReq.getRedPackageId() + "," +
                    "userId:" + packageReq.getUserId() + "," +
                    "nowTime:" + DateHelper.dateToString(new Date()) + "}");
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "系统开小差了, 又有人要扣奖金了", VoViewOpenRedPackageWarpRes.class));
        }

        if (marketingRedpackRecord.getState() == 2) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "对不起, 当前红包已过期!", VoViewOpenRedPackageWarpRes.class));
        } else if (marketingRedpackRecord.getState() == 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "不要调皮了, 当前红包已经被领取了!", VoViewOpenRedPackageWarpRes.class));
        }

        Date nowDate = new Date();


        // 判断时间
        if (DateHelper.diffInDays(nowDate, marketingRedpackRecord.getCancelTime(), false) > 0) {
            // 更新红包
            marketingRedpackRecord.setState(2);
            marketingRedpackRecordService.save(marketingRedpackRecord);
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "对不起, 当前红包已过期!", VoViewOpenRedPackageWarpRes.class));
        }

        Users users = userService.findById(packageReq.getUserId());
        if (ObjectUtils.isEmpty(users)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "当前用户不存在!", VoViewOpenRedPackageWarpRes.class));
        }

        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "当前用户锁定, 取消你领取额红包的资格!", VoViewOpenRedPackageWarpRes.class));
        }

        // 红包开启平率限制
        try {
            Date beginDate = DateHelper.subDays(nowDate, 1);
            Specification<MarketingRedpackRecord> marketingRedpackRecordSpecification = Specifications
                    .<MarketingRedpackRecord>and()
                    .eq("userId", users.getId())
                    .eq("marketingId", 3)
                    .between("openTime", new Range(DateHelper.endOfDate(beginDate), DateHelper.endOfDate(nowDate)))
                    .build();
            long redpackCount = marketingRedpackRecordService.count(marketingRedpackRecordSpecification);
            if (redpackCount >= 3) {
                log.warn("当天开启红包[邀请好友]次数超过3个!");
                return ResponseEntity
                        .badRequest()
                        .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "开启邀请红包过于频繁, 请明天再次尝试!", VoViewOpenRedPackageWarpRes.class));
            }
        } catch (Exception e) {
            log.error("红包平率限制异常", e);
        }


        String onlySeql = String.format("%s%s%s", users.getId(), AssetChangeTypeEnum.receiveRedpack.getLocalType(), marketingRedpackRecord.getId());
        boolean result = commonPublishRedpack(users.getId(),
                marketingRedpackRecord.getMoney(),
                AssetChangeTypeEnum.receiveRedpack,
                onlySeql,
                null,
                marketingRedpackRecord.getId());
        if (result) {
            // 更新红包
            marketingRedpackRecord.setState(1);
            marketingRedpackRecord.setOpenTime(nowDate);
            marketingRedpackRecordService.save(marketingRedpackRecord);
            //站内信数据装配
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(marketingRedpackRecord.getUserId());
            notices.setRead(false);
            notices.setName("打开红包");
            notices.setContent("你在" + DateHelper.dateToString(new Date()) + "开启红包(" + marketingRedpackRecord.getMarkeingTitel() + ")获得奖励" + StringHelper.formatDouble(marketingRedpackRecord.getMoney() / 100d, true) + "元");
            notices.setType("system");
            notices.setCreatedAt(new Date());
            notices.setUpdatedAt(new Date());
            //发送站内信
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("RedPackageServiceImpl openRedPackage send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("RedPackageServiceImpl openRedPackage send mq exception", e);
            }

            VoViewOpenRedPackageWarpRes voViewOpenRedPackageWarpRes = VoBaseResp.ok("打开红包成功", VoViewOpenRedPackageWarpRes.class);
            voViewOpenRedPackageWarpRes.setMoney(MoneyHelper.divide(marketingRedpackRecord.getMoney(), 100D, 2));   // 红包小数点问题
            return ResponseEntity.ok().body(voViewOpenRedPackageWarpRes);
        } else {
            VoViewOpenRedPackageWarpRes voViewOpenRedPackageWarpRes = VoBaseResp.error(VoBaseResp.ERROR, "打开红包失败", VoViewOpenRedPackageWarpRes.class);
            voViewOpenRedPackageWarpRes.setMoney(0D);
            return ResponseEntity.ok().body(voViewOpenRedPackageWarpRes);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> publishActivity(VoPublishRedReq voPublishRedReq) throws Exception {
        String paramStr = voPublishRedReq.getParamStr();

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Gson gson = new Gson();
        String beginTime = paramMap.get("beginTime");
        if (StringUtils.isEmpty(beginTime)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "派发红包, 签名验证不通过!"));
        }

        Date beginDate = DateHelper.stringToDate(beginTime);
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(beginDate))).build();

        Long count = tenderService.count(specification);
        if (count == 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "派发红包, 派发对象为空!"));
        }

        int pageSize = 100, pageindex = 0, totalPageIndex = 0;
        totalPageIndex = count.intValue() / pageSize;
        totalPageIndex = count.intValue() % pageSize == 0 ? totalPageIndex : totalPageIndex + 1;
        // ==================================
        // 投资派发红包
        // ==================================
        for (; pageindex < totalPageIndex; pageindex++) {
            Pageable pageable = new PageRequest(pageindex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<Tender> tenderList = tenderService.findList(specification, pageable);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            for (Tender tender : tenderList) {
                log.info(String.format("触发活动: %s", gson.toJson(tender)));
                MarketingData marketingData = new MarketingData();
                marketingData.setTransTime(DateHelper.dateToString(tender.getCreatedAt()));
                marketingData.setUserId(tender.getUserId().toString());
                marketingData.setSourceId(tender.getId().toString());
                marketingData.setMarketingType(MarketingTypeContants.TENDER);
                try {
                    String json = gson.toJson(marketingData);
                    Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setMsg(data);
                    mqConfig.setTag(MqTagEnum.MARKETING_TENDER);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("投资营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("投资营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }

        return ResponseEntity.ok(VoBaseResp.ok("派发成功"));
    }

    @Override
    public ResponseEntity<VoBaseResp> publishOpenAccountRedpack(VoPublishRedReq voPublishRedReq) {
        String paramStr = voPublishRedReq.getParamStr();

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Gson gson = new Gson();
        String beginTime = paramMap.get("beginTime");
        if (StringUtils.isEmpty(beginTime)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "派发红包, 签名验证不通过!"));
        }

        Date beginDate = DateHelper.stringToDate(beginTime);

        Specification<Users> usersSpecification = Specifications
                .<Users>and()
                .gt("parentId", 0)
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(beginDate)))
                .build();

        Long userCount = userService.count(usersSpecification);
        int pageindex = 0, pageSize = 20;
        int totalPageIndex = 0;
        totalPageIndex = userCount.intValue() / pageSize;
        totalPageIndex = userCount.intValue() % pageSize == 0 ? totalPageIndex : totalPageIndex + 1;

        for (; pageindex < totalPageIndex; pageindex++) {
            Pageable pageable = new PageRequest(pageindex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<Users> userList = userService.findList(usersSpecification, pageable);
            for (Users users : userList) {
                if (users.getParentId() <= 0) {
                    continue;
                }
                log.info(String.format("触发活动: %s", gson.toJson(users)));
                MarketingData marketingData = new MarketingData();
                marketingData.setTransTime(DateHelper.dateToString(users.getCreatedAt()));
                marketingData.setUserId(users.getId().toString());
                marketingData.setSourceId(users.getId().toString());
                marketingData.setMarketingType(MarketingTypeContants.OPEN_ACCOUNT);
                try {
                    String json = gson.toJson(marketingData);
                    Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setMsg(data);
                    mqConfig.setTag(MqTagEnum.MARKETING_OPEN_ACCOUNT);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("用户开户营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("用户开户营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }
        return ResponseEntity.ok(VoBaseResp.ok("派发成功"));
    }

    @Override
    public ResponseEntity<VoBaseResp> publishRedpack4TenderRecord(VoPublishRedReq voPublishRedReq) {
        // 查询投标记录
        String paramStr = voPublishRedReq.getParamStr();
        Preconditions.checkNotNull(paramStr, "根据投标记录派发红包");
        Map<String, String> data = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String idStr = data.get("id");
        Tender tender = tenderService.findById(Long.parseLong(idStr));
        Preconditions.checkNotNull(tender, "根据投标记录补发红包, 无效投标记录");
        try {
            marketingProcessBiz.process("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
