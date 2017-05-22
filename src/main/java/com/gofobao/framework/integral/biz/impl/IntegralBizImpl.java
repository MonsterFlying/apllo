package com.gofobao.framework.integral.biz.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.repository.IntegralLogRepository;
import com.gofobao.framework.integral.service.IntegralLogService;
import com.gofobao.framework.integral.service.IntegralService;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.integral.vo.response.VoIntegral;
import com.gofobao.framework.integral.vo.response.VoListIntegralResp;
import com.gofobao.framework.system.contants.DictAliasCodeContants;
import com.gofobao.framework.system.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
    public ResponseEntity<VoBaseResp> list(VoListIntegralReq voListIntegralReq) {
        int pageSize = voListIntegralReq.getPageSize();
        int pageIndex = voListIntegralReq.getPageIndex();
        Long userId = voListIntegralReq.getUserId();

        Integral integral = integralService.findByUserId(userId);

        if (ObjectUtils.isEmpty(integral)) {
            return ResponseEntity.
                    badRequest().
                    body(VoListIntegralResp.error(1,""));
        }

        Asset asset = assetService.findById(userId);
        if (ObjectUtils.isEmpty(integral)) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "获取积分列表：会员id：" + userId + "，会员资产记录缺失！"));
        }

        List<Map<String, String>> integralRule = null;
        try {
            integralRule = dictService.queryDictList(DictAliasCodeContants.INTEGRAL_RULE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Integer collection = asset.getCollection();//代收金额
        Integer totalIntegral = integral.getUseIntegral() + integral.getNoUseIntegral();//总积分

        VoListIntegralResp voListIntegralResp = new VoListIntegralResp();
        voListIntegralResp.setTotalIntegral(totalIntegral);
        voListIntegralResp.setAvailableIntegral(integral.getUseIntegral());
        voListIntegralResp.setInvalidIntegral(integral.getNoUseIntegral());

        String takeRates = getTakeRates(collection, totalIntegral, integralRule);
        if (StringUtils.isEmpty(takeRates)) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "获取积分列表：积分折现比率获取失败！"));
        }
        voListIntegralResp.setTakeRates(takeRates);

        List<VoIntegral> voIntegralList = new ArrayList<>();

        List<IntegralLog> integralLogList = integralLogService.findByUserId(userId, pageIndex, pageSize);
        Optional<List<IntegralLog>> objIntegralLog = Optional.ofNullable(integralLogList);
        objIntegralLog.ifPresent(p -> p.forEach(integralLog -> {
            VoIntegral voIntegral = new VoIntegral();
            voIntegral.setId(integralLog.getId());
            voIntegral.setTotalIntegral(integralLog.getUseIntegral());
            voIntegral.setTime(DateHelper.dateToStringYearMouthDay(integralLog.getCreatedAt()));
            voIntegral.setIntegral(("convert".equalsIgnoreCase(integralLog.getType()) || "_digest".equalsIgnoreCase(integralLog.getType())) ? String.format("-%s", integralLog.getValue()) : String.format("+%s", integralLog.getValue()));
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
    public ResponseEntity<Integer> doTakeRates(VoIntegralTakeReq voIntegralTakeReq) {
        return null;
    }

    /**
     * 积分折现系数说明
     *
     * @return
     * @throws Exception
     */
    public ResponseEntity<String> takeRatesDesc() {
        return null;
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
    private String getTakeRates(Integer money, Integer integral, List<Map<String, String>> maps) {
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
