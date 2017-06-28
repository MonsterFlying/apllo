package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        List<AssetLog> assetLogs = commonQuery(voAssetLogReq);
        if (CollectionUtils.isEmpty(assetLogs)) {
            return Collections.EMPTY_LIST;
        }
        List<VoViewAssetLogRes> voViewAssetLogRes = Lists.newArrayList();
        assetLogs.stream().forEach(p -> {
            VoViewAssetLogRes viewAssetLogRes = new VoViewAssetLogRes();
            viewAssetLogRes.setMoney(StringHelper.formatMon(p.getMoney() / 100d));
            viewAssetLogRes.setType(p.getType());
            viewAssetLogRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewAssetLogRes.add(viewAssetLogRes);
        });
        List<VoViewAssetLogRes> result = Optional.ofNullable(voViewAssetLogRes).orElse(Collections.EMPTY_LIST);
        return result;
    }

    @Override
    public List<AssetLogs> pcAssetLogs(VoAssetLogReq voAssetLogReq) {
        List<AssetLog> assetLogs = commonQuery(voAssetLogReq);
        if (CollectionUtils.isEmpty(assetLogs)) {
            return Collections.EMPTY_LIST;
        }
        List<AssetLogs> logs = Lists.newArrayList();
        assetLogs.stream().forEach(p -> {
            AssetLogs assetLog = new AssetLogs();
            assetLog.setOperationMoney(StringHelper.formatMon(p.getCollection() / 100D));
            assetLog.setRemark(p.getRemark());
            assetLog.setTime(DateHelper.dateToString(p.getCreatedAt()));
            assetLog.setTypeName(p.getType());
            assetLog.setUsableMoney(StringHelper.formatMon(p.getUseMoney() / 100D));
            logs.add(assetLog);
        });
        return logs;
    }


    private List<AssetLog> commonQuery(VoAssetLogReq voAssetLogReq) {
        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.DESC, "createdAt"));
        Pageable pageable = new PageRequest(voAssetLogReq.getPageIndex()
                , voAssetLogReq.getPageSize()
                , sort);
        Date startTime = DateHelper.stringToDate(voAssetLogReq.getStartTime(), DateHelper.DATE_FORMAT_YMD);
        Date endTime = DateHelper.stringToDate(voAssetLogReq.getEndTime(), DateHelper.DATE_FORMAT_YMD);

        Specification<AssetLog> specification = Specifications.<AssetLog>and()
                .eq(!StringUtils.isEmpty(voAssetLogReq.getType()), "type", voAssetLogReq.getType())
                .between("createdAt",
                        new Range<>(
                                DateHelper.beginOfDate(startTime),
                                DateHelper.endOfDate(endTime)))
                .eq("userId", voAssetLogReq.getUserId())
                .build();
        Page<AssetLog> assetLogPage = assetLogRepository.findAll(specification, pageable);
        return assetLogPage.getContent();
    }

    @Override
    public void insert(AssetLog assetLog) {
        assetLogRepository.save(assetLog);
    }

    @Override
    public void updateById(AssetLog assetLog) {
        assetLogRepository.save(assetLog);
    }
}
