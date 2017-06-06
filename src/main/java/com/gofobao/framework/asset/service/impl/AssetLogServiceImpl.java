package com.gofobao.framework.asset.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
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
     * @param voAssetLog
     * @return
     */
    @Override
    public List<VoViewAssetLogRes> assetLogList(VoAssetLog voAssetLog) {

        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.DESC, "createdAt"));
        Pageable pageable = new PageRequest(voAssetLog.getPageIndex()
                , voAssetLog.getPageSize()
                , sort);

        Specification<AssetLog> specification = Specifications.<AssetLog>and()
                .eq(!StringUtils.isEmpty(voAssetLog.getType()), "type", voAssetLog.getType())
                .between("createdAt", new Range<>(DateHelper.stringToDate(voAssetLog.getStartTime()),DateHelper.stringToDate(voAssetLog.getEndTime())))
                .eq("userId", voAssetLog.getUserId())
                .build();
       Page<AssetLog> assetLogPage = assetLogRepository.findAll(specification, pageable);

        Gson gson = new GsonBuilder().setDateFormat(DateHelper.DATE_FORMAT_YMDHMS).create();

        String jsonStr = gson.toJson(assetLogPage.getContent());
        List<VoViewAssetLogRes> voViewAssetLogRes = gson.fromJson(jsonStr, new TypeToken<List<VoViewAssetLogRes>>() {
        }.getType());
        List<VoViewAssetLogRes> result = Optional.ofNullable(voViewAssetLogRes).orElse(Collections.EMPTY_LIST);
        return result;
    }

    @Override
    public void insert(AssetLog assetLog) {
        assetLogRepository.save(assetLog) ;
    }

    @Override
    public void updateById(AssetLog assetLog) {
        assetLogRepository.save(assetLog) ;
    }
}
