package com.gofobao.framework.integral.biz.impl;

import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.ThirdAccountHelper;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.entity.IntegralLog;
import com.gofobao.framework.integral.service.IntegralLogService;
import com.gofobao.framework.integral.service.IntegralService;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.integral.vo.response.VoIntegral;
import com.gofobao.framework.integral.vo.response.VoListIntegralResp;
import com.gofobao.framework.integral.vo.response.pc.VoViewIntegralWarpRes;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.contants.DictAliasCodeContants;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
@Slf4j
public class IntegralBizImpl implements IntegralBiz {

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    private IntegralService integralService;

    @Autowired
    private IntegralLogService integralLogService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DictService dictService;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;

    @Autowired
    private DictItemService dictItemService;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    RedPackageBiz redPackageBiz;


    private static Map<String, String> integralTypeMap = new HashMap<>();

    static {
        integralTypeMap.put("tender", "投资积分");
        integralTypeMap.put("cancel", "拨正");
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
    @Override
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
        Long totalIntegral = integral.getUseIntegral() + integral.getNoUseIntegral();//总积分

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
        //APP请求需要返回积分列表
        if (voListIntegralReq.getType() == 0) {
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
        }
        voListIntegralResp.setDescImage(javaDomain + "/images/integral/desc.png");
        voListIntegralResp.setCollectionMoney(StringHelper.formatMon(asset.getCollection() / 100D));

        return ResponseEntity.ok(voListIntegralResp);
    }


    @Override
    public ResponseEntity<VoViewIntegralWarpRes> pcIntegralList(VoListIntegralReq integralReq) {
        try {
            VoViewIntegralWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewIntegralWarpRes.class);

            Map<String, Object> resultMaps = integralLogService.pcIntegralList(integralReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            warpRes.setTotalCount(totalCount);
            List<IntegralLog> integralLogs = (List<IntegralLog>) resultMaps.get("integralLogs");
            List<VoIntegral> voIntegralList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(integralLogs)) {
                integralLogs.stream().forEach(p -> {
                    VoIntegral voIntegral = new VoIntegral();
                    voIntegral.setId(p.getId());
                    voIntegral.setTotalIntegral(p.getNoUseIntegral() + p.getUseIntegral());
                    voIntegral.setTime(DateHelper.dateToStringYearMouthDay(p.getCreatedAt()));
                    voIntegral.setIntegral(("convert".equalsIgnoreCase(p.getType())
                            || "_digest".equalsIgnoreCase(p.getType())) ? String.format("-%s", p.getValue()) : String.format("+%s", p.getValue()));
                    voIntegral.setType(p.getType());
                    voIntegral.setTypeName(findIntegralMap(p.getType()));
                    voIntegral.setUsedIntegral(p.getNoUseIntegral());
                    voIntegral.setUseIntegral(p.getUseIntegral());
                    voIntegralList.add(voIntegral);

                });
            }
            warpRes.setIntegrals(voIntegralList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR,
                            "查询异常,稍后再试",
                            VoViewIntegralWarpRes.class));
        }
    }

    /**
     * 积分兑换
     *
     * @param voIntegralTakeReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<VoBaseResp> doTakeRates(VoIntegralTakeReq voIntegralTakeReq) throws Exception {
        log.info("[积分兑换] 执行开始");
        Long userId = voIntegralTakeReq.getUserId();
        // 获取积分记录
        Integral integral = integralService.findByUserIdLock(userId);
        Preconditions.checkNotNull(integral, "search integral reocrd is null");

        // 获取开户信息
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "search userThirdAccount record is null");

        // 判断开户信息是否完整
        ResponseEntity<VoBaseResp> checkResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!checkResponse.getStatusCode().equals(HttpStatus.OK)) {
            return checkResponse;
        }

        // 可用积分
        Long useIntegral = integral.getUseIntegral();
        // 兑换积分
        Integer integer = voIntegralTakeReq.getInteger();

        if (integer.intValue() > useIntegral.intValue()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "折现积分大于可用积分!"));
        }

        if ((integer < 10000) || (integer % 1000 != 0)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "兑换积分必须10000起，并且是1000倍数!"));
        }

        Asset asset = assetService.findByUserIdLock(userId);
        List<Map<String, String>> integralRule = null;
        try {
            integralRule = dictService.queryDictList(DictAliasCodeContants.INTEGRAL_RULE);
        } catch (Throwable e) {
            log.error("[积分兑换] 查询积分兑换规则失败", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询积分兑换规则失败, 请告知平台客服, 谢谢!"));
        }

        Gson gson = new Gson();
        // 待收
        Long collection = asset.getCollection();
        // 总积分
        Long sumIntegral = useIntegral + integral.getNoUseIntegral();
        String takeRatesStr = getTakeRates(collection, sumIntegral, integralRule);
        // 折现系数
        double takeRates = new BigDecimal(takeRatesStr).doubleValue();
        // 可兑换金额
        long money = Math.round(takeRates * integer);
        Date nowDate = new Date();

        // 更新记录
        Integral changeIntegral = new Integral();
        IntegralLog integralLog = new IntegralLog();
        try {
            Long noUseIntegral = integral.getNoUseIntegral() + integer;
            Long userInteger1 = integral.getUseIntegral() - integer;
            changeIntegral.setUserId(userId);
            changeIntegral.setNoUseIntegral(noUseIntegral);
            changeIntegral.setUseIntegral(userInteger1);
            changeIntegral.setUpdatedAt(new Date());
            changeIntegral = integralService.updateById(changeIntegral);

            integralLog.setUseIntegral(userInteger1);
            integralLog.setNoUseIntegral(noUseIntegral);
            integralLog.setUserId(userId);
            integralLog.setCreatedAt(new Date());
            integralLog.setValue(new Long(integer));
            integralLog.setType("convert");
            integralLog = integralLogService.insert(integralLog);
        } catch (Exception e) {
            log.error("[积分兑换] 积分兑换记录存储失败", e);
            exceptionEmailHelper.sendException("积分兑换记录存储失败", e);
            throw new Exception(e);
        }

        // 唯一记录
        String onlySeq = String.format("%s_integral_%s", integralLog.getId(), userId);  // 派发红包唯一标识
        String remark = String.format("使用积分(%s)兑换%s元", voIntegralTakeReq.getInteger(), StringHelper.formatDouble(money / 100D, true));
        // 派发结果
        boolean publishState = false;
        try {
            publishState = redPackageBiz.commonPublishRedpack(userId, money, AssetChangeTypeEnum.integralExchangeRedpack,
                    onlySeq, remark, integralLog.getId());
        } catch (Exception e) {
            log.error("[积分兑换] 红包派发失败", e);
            exceptionEmailHelper.sendException("积分兑换记录红包派发失败", e);
            publishState = false;
        }

        if (!publishState) {
            try {
                String errMsg = gson.toJson(changeIntegral);
                log.warn(String.format("[积分兑换] 积分撤回 %s", errMsg));
                exceptionEmailHelper.sendErrorMessage("积分兑换失败, 执行积分拨正", errMsg);
                // 执行积分兑换撤回
                changeIntegral.setNoUseIntegral(changeIntegral.getNoUseIntegral() - integer);
                changeIntegral.setUseIntegral(changeIntegral.getUseIntegral() + integer);
                changeIntegral.setUpdatedAt(nowDate);
                integralService.updateById(changeIntegral);

                IntegralLog cancelIntegralLog = new IntegralLog();
                cancelIntegralLog.setUseIntegral(changeIntegral.getUseIntegral());
                cancelIntegralLog.setNoUseIntegral(changeIntegral.getNoUseIntegral());
                cancelIntegralLog.setUserId(userId);
                cancelIntegralLog.setCreatedAt(nowDate);
                cancelIntegralLog.setValue(new Long(integer));
                cancelIntegralLog.setType("cancel");
                integralLogService.insert(cancelIntegralLog);
            } catch (Exception e) {
                log.error("[积分兑换]  积分拨正失败", e);
                exceptionEmailHelper.sendException("积分兑换失败, 执行积分拨正", e);
                throw new Exception(e);
            }
        }

        log.info("[积分兑换] 积分兑换结束");
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
    private String getTakeRates(Long money, Long integral, List<Map<String, String>> maps) {
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
