package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by admin on 2017/5/22.
 */
@Service
@Slf4j
public class AssetLogServiceImpl implements AssetLogService {

    @Autowired
    private AssetLogRepository assetLogRepository;


    /**
     * 资金流水
     *
     * @param voAssetLogReq
     * @return
     */
    @Override
    public List<VoViewAssetLogRes> assetLogList(VoAssetLogReq voAssetLogReq) {

        Map<String, Object> resultMaps = commonQuery(voAssetLogReq);
        List<AssetLog> assetLogs = (List<AssetLog>) resultMaps.get("assetLogs");
        if (CollectionUtils.isEmpty(assetLogs)) {
            return Collections.EMPTY_LIST;
        }
        List<VoViewAssetLogRes> voViewAssetLogRes = Lists.newArrayList();
        assetLogs.stream().forEach(p -> {
            VoViewAssetLogRes viewAssetLogRes = new VoViewAssetLogRes();
            viewAssetLogRes.setMoney(StringHelper.formatMon(p.getMoney() / 100d));
            viewAssetLogRes.setTypeName(getAssetTypeStr(p.getType()));
            viewAssetLogRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewAssetLogRes.add(viewAssetLogRes);
        });
        List<VoViewAssetLogRes> result = Optional.ofNullable(voViewAssetLogRes).orElse(Collections.EMPTY_LIST);
        return result;
    }

    @Override
    public List<AssetLogs> pcAssetLogs(VoAssetLogReq voAssetLogReq) {
        Map<String, Object> resultMaps = commonQuery(voAssetLogReq);
        List<AssetLog> assetLogs = (List<AssetLog>) resultMaps.get("assetLogs");
        Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
        if (CollectionUtils.isEmpty(assetLogs)) {
            return Collections.EMPTY_LIST;
        }
        final int[] num = {0};
        List<AssetLogs> logs = Lists.newArrayList();
        assetLogs.stream().forEach(p -> {
            AssetLogs assetLog = new AssetLogs();
            assetLog.setOperationMoney(StringHelper.formatMon(p.getCollection() / 100D));
            assetLog.setRemark(p.getRemark());
            assetLog.setTime(DateHelper.dateToString(p.getCreatedAt()));
            assetLog.setTypeName(p.getType());
            assetLog.setUsableMoney(StringHelper.formatMon(p.getUseMoney() / 100D));
            if (num[0] == 0) {
                assetLog.setTotalCount(totalCount);
                num[0] = 1;
            }
            logs.add(assetLog);
        });
        return logs;
    }


    private Map<String, Object> commonQuery(VoAssetLogReq voAssetLogReq) {
        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.DESC, "createdAt"));
        Pageable pageable = new PageRequest(voAssetLogReq.getPageIndex()
                , voAssetLogReq.getPageSize()
                , sort);
        Date startTime = DateHelper.beginOfDate(DateHelper.stringToDate(voAssetLogReq.getStartTime(), DateHelper.DATE_FORMAT_YMD));
        Date endTime = DateHelper.endOfDate(DateHelper.stringToDate(voAssetLogReq.getEndTime(), DateHelper.DATE_FORMAT_YMD));

        Specification<AssetLog> specification = Specifications.<AssetLog>and()
                .eq(!StringUtils.isEmpty(voAssetLogReq.getType()), "type", voAssetLogReq.getType())
                .between("createdAt",
                        new Range<>(
                                DateHelper.beginOfDate(startTime),
                                DateHelper.endOfDate(endTime)))
                .eq("userId", voAssetLogReq.getUserId())
                .build();
        Page<AssetLog> assetLogPage = assetLogRepository.findAll(specification, pageable);

        Map<String, Object> resultMaps = Maps.newHashMap();

        List<AssetLog> assetLogs = assetLogPage.getContent();

        resultMaps.put("totalCount", assetLogPage.getTotalElements());
        resultMaps.put("assetLogs", assetLogs);

        return resultMaps;
    }

    @Override
    public void insert(AssetLog assetLog) {
        assetLogRepository.save(assetLog);
    }

    @Override
    public void updateById(AssetLog assetLog) {
        assetLogRepository.save(assetLog);
    }


    /**
     * 获取资产类型字符串
     *
     * @return
     */
    private static String getAssetTypeStr(String assetType) {

        if (StringUtils.isEmpty(assetType)) {
            return "";
        }

        String[] strArr = assetType.split("_");
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer tempBuff = null;
        for (String str : strArr) {
            tempBuff = new StringBuffer(str);
            tempBuff.setCharAt(0, Character.toUpperCase(str.charAt(0)));
            stringBuffer.append(tempBuff);
        }

        assetType = stringBuffer.toString();
        String rs = "";
        switch (CapitalChangeEnum.valueOf(assetType)) {
            case Award:
                rs = "奖励";
                break;
            case Bonus:
                rs = "提成";
                break;
            case AwardVirtualMoney:
                rs = "赠送体验金";
                break;
            case Borrow:
                rs = "借款";
                break;
            case Cash:
                rs = "提现";
                break;
            case CollectionAdd:
                rs = "添加代收";
                break;
            case CollectionLower:
                rs = "扣除待收";
                break;
            case Correct:
                rs = "数据修正";
                break;
            case ExpenditureOther:
                rs = "其他支出";
                break;
            case Fee:
                rs = "费用";
                break;
            case Frozen:
                rs = "冻结资金";
                break;
            case IncomeOther:
                rs = "其他收入";
                break;
            case IncomeOverdue:
                rs = "收到逾期费";
                break;
            case IncomeRepayment:
                rs = "回款";
                break;
            case IntegralCash:
                rs = "积分折现";
                break;
            case InterestManager:
                rs = "利息管理费";
                break;
            case Manager:
                rs = "账户管理费";
                break;
            case Overdue:
                rs = "逾期费";
                break;
            case PaymentAdd:
                rs = "添加待还";
                break;
            case PaymentLower:
                rs = "扣除待还";
                break;
            case Recharge:
                rs = "充值";
                break;
            case Repayment:
                rs = "还款";
                break;
            case Tender:
                rs = "投标";
                break;
            case Unfrozen:
                rs = "解除冻结";
                break;
            case RedPackage:
                rs = "奖励红包";
                break;
            case VirtualTender:
                rs = "投资体验标";
                break;
            default:
        }

        return rs;
    }

}
