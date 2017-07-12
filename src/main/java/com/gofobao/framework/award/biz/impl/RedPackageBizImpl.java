package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.contants.RedPacketContants;
import com.gofobao.framework.award.entity.ActivityRedPacket;
import com.gofobao.framework.award.entity.ActivityRedPacketLog;
import com.gofobao.framework.award.repository.RedPackageLogRepository;
import com.gofobao.framework.award.repository.RedPackageRepository;
import com.gofobao.framework.award.service.RedPackageService;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.OpenRedPackage;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.JixinContants;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class RedPackageBizImpl implements RedPackageBiz {

    @Autowired
    private RedPackageService redPackageService;

    @Autowired
    private RedPackageLogRepository redPackageLogRepository;

    @Autowired
    private CapitalChangeHelper changeHelper;

    @Autowired
    private RedPackageRepository redPackageRepository;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private DictItemServcie dictItemServcie;

    @Autowired
    private DictValueService dictValueService;

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueService.findTopByItemIdAndValue02(dictItem.getId(), bankName);
                }
            });

    @Autowired
    private JixinManager jixinManager;

    @Override
    public ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq) {
        try {
            List<RedPackageRes> redPackageRes = redPackageService.list(voRedPackageReq);
            VoViewRedPackageWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewRedPackageWarpRes.class);
            warpRes.setResList(redPackageRes);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            log.error("RedPackageBizImpl-->list fail", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewRedPackageWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewOpenRedPackageWarpRes> openRedPackage(VoOpenRedPackageReq packageReq) {
        try {
            List<ActivityRedPacket> redPackages = redPackageService.openRedPackage(packageReq);
            OpenRedPackage openRedPackage = new OpenRedPackage();
            openRedPackage.setFlag(true);
            do {
                if (CollectionUtils.isEmpty(redPackages)) {
                    log.info("打开红包失败,该红包id不存在 或者已过期: {redPackageId:" + packageReq.getRedPackageId() + "," +
                            "userId:" + packageReq.getUserId() + "," +
                            "nowTime:" + DateHelper.dateToString(new Date()) + "}");
                    openRedPackage.setFlag(false);
                    break;
                }
                ActivityRedPacket redPacket = redPackages.get(0);
                try {
                    //增加资金
                    CapitalChangeEntity entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.RedPackage);
                    entity.setUserId(packageReq.getUserId());
                    entity.setMoney(redPacket.getMoney());
                    entity.setRemark("红包收入");

                    if (!changeHelper.capitalChange(entity)) {
                        log.info("红包资金变动失败: " +
                                "{redPackageId:" + redPacket.getId() + "," +
                                "userId:" + redPacket.getUserId() + "," +
                                "money:" + redPacket.getMoney() + "," +
                                "nowTime:" + DateHelper.dateToString(new Date()) + "}");
                        openRedPackage.setFlag(false);
                        break;
                    }
                    //红包更新
                    ActivityRedPacketLog redPacketLog = new ActivityRedPacketLog();
                    redPacketLog.setUserId(packageReq.getUserId());
                    redPacketLog.setCreateTime(new Date());
                    redPacketLog.setRedPacketId(packageReq.getRedPackageId());
                    redPacketLog.setIparam1(0);
                    redPacketLog.setIparam2(0);
                    redPackageLogRepository.save(redPacketLog);
                    redPacket.setStatus(RedPacketContants.used);
                    redPacket.setUpdateDate(new Date());
                    redPackageRepository.save(redPacket);
                    double money = redPacket.getMoney() / 100d;

                    //查询红包账户
                    DictValue dictValue =  jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                    UserThirdAccount redPacketAccount =  userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                    //请求即信红包
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(redPacket.getUserId());
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(redPacket.getMoney(), 100, false));
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("拆开红包");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.info("拆开红包异常:" + msg);
                        openRedPackage.setFlag(false);
                        break;
                    }
                    //站内信数据装配
                    Notices notices = new Notices();
                    notices.setFromUserId(1L);
                    notices.setUserId(redPacket.getUserId());
                    notices.setRead(true);
                    notices.setRead(false);
                    notices.setRead(false);
                    notices.setName("打开红包");
                    notices.setContent("你在" + DateHelper.dateToString(new Date()) + "开启红包(" + redPacket.getActivityName() + ")获得奖励" + money + "元");
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
                    openRedPackage.setFlag(true);
                    openRedPackage.setMoney(money);
                    //日志记录
                    log.info("打开红包成功: {redPackageId:" + redPacket.getId() + "," +
                            "userId:" + redPacket.getUserId() + "," +
                            "money:" + money + ", " +
                            "nowTime:" + DateHelper.dateToString(new Date()) + "}");

                } catch (Throwable e) {
                    //日志记录
                    log.info("打开红包失败: {redPackageId:" + redPacket.getId() + "," +
                            "userId:" + redPacket.getUserId() + "," +
                            "money:" + redPacket.getMoney() / 100d + " ," +
                            "nowTime:" + DateHelper.dateToString(new Date()) + "}");
                    openRedPackage.setFlag(false);
                    break;
                }
            } while (false);
            /**
             * 返回结果
             */
            if (openRedPackage.isFlag()) {  //拆开成功
                VoViewOpenRedPackageWarpRes warpRes = VoBaseResp.ok("打开成功", VoViewOpenRedPackageWarpRes.class);
                warpRes.setOpenRedPackage(openRedPackage);
                return ResponseEntity.ok(warpRes);
            } else {
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(
                                VoBaseResp.ERROR,
                                "打开红包失败",
                                VoViewOpenRedPackageWarpRes.class));
            }
        } catch (Throwable e) {
            log.info("RedPackageBizImpl openRedPackage fail", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "打开红包失败",
                            VoViewOpenRedPackageWarpRes.class));
        }
    }
}
