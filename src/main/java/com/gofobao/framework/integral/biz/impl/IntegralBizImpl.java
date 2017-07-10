package com.gofobao.framework.integral.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.service.IntegralLogService;
import com.gofobao.framework.integral.service.IntegralService;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.integral.vo.response.VoIntegral;
import com.gofobao.framework.integral.vo.response.VoListIntegralResp;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.contants.DictAliasCodeContants;
import com.gofobao.framework.system.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
@Slf4j
public class IntegralBizImpl implements IntegralBiz {

    @Autowired
    private IntegralService integralService;
    @Autowired
    private IntegralLogService integralLogService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private DictService dictService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;

    @Value(value = "${jixin.redPacketAccountId}")
    private String redPacketAccountId; //存管红包账户

    @Autowired
    private MqHelper mqHelper;


    private static Map<String, String> integralTypeMap = new HashMap<>();

    static {
        integralTypeMap.put("tender", "投资积分");
        integralTypeMap.put("convert", "积分折现");
        integralTypeMap.put("post", "发帖积分");
        integralTypeMap.put("reply", "回帖积分");
        integralTypeMap.put("digest", "加精华积分");
        integralTypeMap.put("_digest", "取消精华积分");
        integralTypeMap.put("sign", "签到积分");
        integralTypeMap.put("sign_award", "签到奖励积分");
    }

    /**
     * 获取积分列表
     *
     * @param voListIntegralReq
     * @return
     */
    public ResponseEntity<VoListIntegralResp> list(VoListIntegralReq voListIntegralReq) {
        int pageSize = voListIntegralReq.getPageSize();
        int pageIndex = voListIntegralReq.getPageIndex();
        Long userId = voListIntegralReq.getUserId();

        Integral integral = integralService.findByUserId(userId);

        if (ObjectUtils.isEmpty(integral)) {
            return ResponseEntity.
                    badRequest().
                    body(VoListIntegralResp.error(VoBaseResp.ERROR, "系统开小差了, 请稍后重试!", VoListIntegralResp.class));
        }

        Asset asset = assetService.findByUserId(userId);
        if (ObjectUtils.isEmpty(integral)) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了, 请稍后重试!", VoListIntegralResp.class));
        }

        List<Map<String, String>> integralRule = null;
        try {
            integralRule = dictService.queryDictList(DictAliasCodeContants.INTEGRAL_RULE);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Long collection = asset.getCollection();//代收金额
        Integer totalIntegral = integral.getUseIntegral() + integral.getNoUseIntegral();//总积分

        VoListIntegralResp voListIntegralResp = VoBaseResp.ok("查询成功", VoListIntegralResp.class);
        voListIntegralResp.setTotalIntegral(totalIntegral);
        voListIntegralResp.setAvailableIntegral(integral.getUseIntegral());
        voListIntegralResp.setInvalidIntegral(integral.getNoUseIntegral());

        String takeRates = getTakeRates(collection, totalIntegral, integralRule);
        if (StringUtils.isEmpty(takeRates)) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "获取积分列表：积分折现比率获取失败！", VoListIntegralResp.class));
        }
        voListIntegralResp.setTakeRates(takeRates);

        List<VoIntegral> voIntegralList = new ArrayList<>();

        //分页和排序
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = new PageRequest(pageIndex, pageSize, sort);

        List<IntegralLog> integralLogList = integralLogService.findListByUserId(userId, pageable);
        Optional<List<IntegralLog>> objIntegralLog = Optional.ofNullable(integralLogList);
        objIntegralLog.ifPresent(p -> p.forEach(integralLog -> {
            VoIntegral voIntegral = new VoIntegral();
            voIntegral.setId(integralLog.getId());
            voIntegral.setTotalIntegral(integralLog.getNoUseIntegral() + integralLog.getUseIntegral());
            voIntegral.setTime(DateHelper.dateToStringYearMouthDay(integralLog.getCreatedAt()));
            voIntegral.setIntegral(("convert".equalsIgnoreCase(integralLog.getType())
                    || "_digest".equalsIgnoreCase(integralLog.getType())) ? String.format("-%s", integralLog.getValue()) : String.format("+%s", integralLog.getValue()));
            voIntegral.setType(integralLog.getType());
            voIntegral.setTypeName(findIntegralMap(integralLog.getType()));
            voIntegralList.add(voIntegral);
        }));

        voListIntegralResp.setVoIntegralList(voIntegralList);
        return ResponseEntity.ok(voListIntegralResp);
    }

    /**
     * 积分兑换
     *
     * @param voIntegralTakeReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> doTakeRates(VoIntegralTakeReq voIntegralTakeReq) throws Exception {
        Long userId = voIntegralTakeReq.getUserId();

        Integral integral = integralService.findByUserIdLock(userId);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);

        Integer integer = voIntegralTakeReq.getInteger();
        if ((integer < 10000) || (integer % 1000 != 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "兑换积分必须10000起，并且是1000倍数!"));
        }

        if (ObjectUtils.isEmpty(integral)) {
            throw new Exception("查询用户积分失败!");
        }

        if (ObjectUtils.isEmpty(userThirdAccount)) {
            log.error("================================积分兑换===================================");
            log.error("当前用户未开户：用户id:" + userId);
            log.error("===========================================================================");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }


        Integer useIntegral = integral.getUseIntegral();
        if (integer >= useIntegral) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "折现积分大于可用积分!"));
        }

        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(integral)) {
            throw new Exception("查询用户资产失败!");
        }

        List<Map<String, String>> integralRule = null;
        try {
            integralRule = dictService.queryDictList(DictAliasCodeContants.INTEGRAL_RULE);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Long collection = asset.getCollection();
        Integer sumIntegral=useIntegral+integral.getNoUseIntegral();
        String takeRatesStr = getTakeRates(collection, sumIntegral, integralRule);
        double takeRates = Double.parseDouble(takeRatesStr);//折现系数
        long money = Math.round(takeRates * integer);  // 可兑换金额
        //资金变动
        CapitalChangeEntity capitalChangeEntity = new CapitalChangeEntity();
        capitalChangeEntity.setType(CapitalChangeEnum.IntegralCash);
        capitalChangeEntity.setMoney((int) money);
        capitalChangeEntity.setUserId(new Long(userId).intValue());

        boolean b = false;
        try {
            b = capitalChangeHelper.capitalChange(capitalChangeEntity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (!b) {
            throw new Exception("用户资产变更失败!");
        }
        Integer noUseIntegral=integral.getNoUseIntegral() + integer;
        Integer userInteger1=integral.getUseIntegral() - integer;
        Integral saveIntegral = new Integral();
        saveIntegral.setUserId(userId);
        saveIntegral.setNoUseIntegral(noUseIntegral);
        saveIntegral.setUseIntegral(userInteger1);
        saveIntegral.setUpdatedAt(new Date());
        integralService.updateById(saveIntegral);
        IntegralLog integralLog = new IntegralLog();
        integralLog.setUseIntegral(userInteger1);
        integralLog.setNoUseIntegral(noUseIntegral);
        integralLog.setUserId(userId);
        integralLog.setCreatedAt(new Date());
        integralLog.setValue(integer);
        integralLog.setType("convert");

        integralLog = integralLogService.insert(integralLog);
        if (ObjectUtils.isEmpty(integralLog)) {
            throw new Exception("积分日志表插入失败!");
        } else {
            //调用即信发送红包接口
            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
            voucherPayRequest.setAccountId(redPacketAccountId);
            voucherPayRequest.setTxAmount(StringHelper.formatDouble(money , 100,false));
            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
            voucherPayRequest.setDesLine("用户积分兑换");
            voucherPayRequest.setChannel(ChannelContant.HTML);
            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                throw new Exception("积分折现异常:"+ msg);
            }
        }
        return ResponseEntity.ok(VoBaseResp.ok("积分折现成功!"));
    }



    /**
     * 查找积分类型名称
     *
     * @param type
     * @return
     */
    private String findIntegralMap(String type) {
        String typeName = integralTypeMap.get(type);
        return StringUtils.isEmpty(typeName) ? "其他积分奖励" : typeName;
    }

    /**
     * 获取积分转换率
     *
     * @param money    总代收
     * @param integral 总积分
     * @return
     */
    private String getTakeRates(Long money, Integer integral, List<Map<String, String>> maps) {
        money = money / 100;

        Integer moneyMin = 0;
        Integer moneyMax = 0;

        Integer integralMin = 0;
        Integer integralMax = 0;
        for (Map<String, String> bean : maps) {
            integralMin = Integer.parseInt(bean.get("value01"));
            integralMax = Integer.parseInt(bean.get("value02"));

            moneyMin = Integer.parseInt(bean.get("value03"));
            moneyMax = Integer.parseInt(bean.get("value04"));

            if (money >= moneyMin && money <= moneyMax && integral >= integralMin && integral <= integralMax) {
                return bean.get("value05");
            }
        }
        return null;
    }
}
