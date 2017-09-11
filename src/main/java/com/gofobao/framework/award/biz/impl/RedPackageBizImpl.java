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
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.vo.response.VoPreRechargeResp;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.repository.RedPackageLogRepository;
import com.gofobao.framework.award.repository.RedPackageRepository;
import com.gofobao.framework.award.service.RedPackageService;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.borrow.entity.Borrow;
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
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
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

import java.util.Collection;
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

    @Override
    public ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq) {
        Pageable pageable = new PageRequest(voRedPackageReq.getPageIndex(), voRedPackageReq.getPageSize(), new Sort(Sort.Direction.DESC, "id"));
        VoViewRedPackageWarpRes voViewRedPackageWarpRes = VoBaseResp.ok("查询成功", VoViewRedPackageWarpRes.class);
        List<MarketingRedpackRecord> marketingRedpackRecords = marketingRedpackRecordService.findByUserIdAndState(voRedPackageReq.getUserId(), voRedPackageReq.getStatus(), pageable);

        RedPackageRes redPackageRes = null;
        // 遍历
        for (MarketingRedpackRecord item : marketingRedpackRecords) {
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

        try {
            String groupSeqNo = assetChangeProvider.getGroupSeqNo();
            long redId = assetChangeProvider.getRedpackAccountId();
            Asset redPackage = assetService.findByUserIdLock(redId);
            Long sendRedpackMoney = marketingRedpackRecord.getMoney();
            if (redPackage.getUseMoney() - sendRedpackMoney < 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "平台派发红包余额不足, 麻烦联系我们客服充钱, 请稍后重试!", VoViewOpenRedPackageWarpRes.class));
            }

            UserThirdAccount redpackThirdAccount = userThirdAccountService.findByUserId(redId); //查询红包账户
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(marketingRedpackRecord.getUserId());
            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
            voucherPayRequest.setAccountId(redpackThirdAccount.getAccountId());
            voucherPayRequest.setTxAmount(StringHelper.formatDouble(marketingRedpackRecord.getMoney(), 100, false));
            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
            voucherPayRequest.setDesLine("领取红包");
            voucherPayRequest.setChannel(ChannelContant.HTML);
            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                log.error("用户拆红包异常:" + msg);
                return ResponseEntity
                        .badRequest()
                        .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "存管系统发放红包异常!", VoViewOpenRedPackageWarpRes.class));
            }

            // 更新红包
            marketingRedpackRecord.setState(1);
            marketingRedpackRecordService.save(marketingRedpackRecord);

            try {
                // 红包账户发送红包
                AssetChange redpackPublish = new AssetChange();
                redpackPublish.setMoney(marketingRedpackRecord.getMoney());
                redpackPublish.setType(AssetChangeTypeEnum.publishRedpack);  //  扣除红包
                redpackPublish.setUserId(redId);
                redpackPublish.setRemark(String.format("派发奖励红包 %s元", StringHelper.formatDouble(marketingRedpackRecord.getMoney() / 100D, true)));
                redpackPublish.setGroupSeqNo(groupSeqNo);
                redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                redpackPublish.setForUserId(marketingRedpackRecord.getUserId());
                redpackPublish.setSourceId(marketingRedpackRecord.getId());
                assetChangeProvider.commonAssetChange(redpackPublish);

                // 用户接收红包
                AssetChange redpackR = new AssetChange();
                redpackR.setMoney(marketingRedpackRecord.getMoney());
                redpackR.setType(AssetChangeTypeEnum.receiveRedpack);
                redpackR.setUserId(marketingRedpackRecord.getUserId());
                redpackR.setRemark(String.format("领取奖励红包 %s元", StringHelper.formatDouble(marketingRedpackRecord.getMoney() / 100D, true)));
                redpackR.setGroupSeqNo(groupSeqNo);
                redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                redpackR.setForUserId(redId);
                redpackR.setSourceId(marketingRedpackRecord.getId());
                assetChangeProvider.commonAssetChange(redpackR);
            } catch (Exception e) {
                log.error("红包开启本地资金变动异常", e);
            }

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
            voViewOpenRedPackageWarpRes.setMoney(marketingRedpackRecord.getMoney() / 100D);
            return ResponseEntity.ok().body(voViewOpenRedPackageWarpRes);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public ResponseEntity<VoBaseResp> publishActivity(VoPublishRedReq voPublishRedReq) {
        Date nowDate = new Date();
        String paramStr = voPublishRedReq.getParamStr();
        Gson gson = new Gson();
        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
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
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(nowDate))).build();

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
                    mqConfig.setTag(MqTagEnum.MARKETING_OPEN_ACCOUNT);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("投资营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("投资营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }

        // ===============================
        // 用户派发红包
        // ===============================
        Specification<Users> usersSpecification = Specifications
                .<Users>and()
                .gt("parentId", 0)
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(nowDate)))
                .build();


        Long userCount = userService.count(usersSpecification);
        pageindex = 0;
        totalPageIndex = 0;
        totalPageIndex = userCount.intValue() / pageSize;
        totalPageIndex = userCount.intValue() % pageSize == 0 ? totalPageIndex : totalPageIndex + 1;

        for (; pageindex < totalPageIndex; pageindex++) {
            Pageable pageable = new PageRequest(pageindex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<Users> userList = userService.findList(usersSpecification, pageable);
            for (Users users : userList) {
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
                    log.info(String.format("开户营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("开户营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }
        return null;
    }


}
