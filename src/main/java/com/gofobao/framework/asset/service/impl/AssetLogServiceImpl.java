package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.vo.repsonse.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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
        org.springframework.data.domain.Page<AssetLog> assetLogPage;

        AssetLog assetLog=new AssetLog();
        Example<AssetLog>assetLogExample=Example.of(assetLog);

        assetLogRepository.findAll(assetLogExample);

        if (StringUtils.isEmpty(voAssetLog.getType())) {
            assetLogPage = assetLogRepository.findByUserIdAndCreateAtLessThanEqualAndCreateAtGreaterThanEqual(voAssetLog.getUserId(),voAssetLog.getStartTime(),voAssetLog.getEndTime(), pageable);
        } else {
            assetLogPage = assetLogRepository.findByUserIdAndTypeAndCreateAtLessThanEqualAndCreateAtGreaterThanEqual(voAssetLog.getUserId(), voAssetLog.getType(),voAssetLog.getStartTime(),voAssetLog.getEndTime(), pageable);
        }
        Gson gson = new GsonBuilder().setDateFormat(DateHelper.DATE_FORMAT_YMDHMS).create();
        String jsonStr = gson.toJson(assetLogPage);
        List<VoViewAssetLogRes> voViewAssetLogRes = gson.fromJson(jsonStr, new TypeToken<List<VoViewAssetLogRes>>() {
        }.getType());
        List<VoViewAssetLogRes> result = Optional.ofNullable(voViewAssetLogRes).orElse(Collections.EMPTY_LIST);

        return result;
    }

    public boolean insert(AssetLog assetLog){
        if (ObjectUtils.isEmpty(assetLog)){
            return false;
        }
        assetLog.setId(null);
        return !ObjectUtils.isEmpty(assetLogRepository.save(assetLog));
    }

    public boolean updateById(AssetLog assetLog){
        if (ObjectUtils.isEmpty(assetLog) || ObjectUtils.isEmpty(assetLog.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(assetLogRepository.save(assetLog));
    }
}
