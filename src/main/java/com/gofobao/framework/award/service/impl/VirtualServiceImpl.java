package com.gofobao.framework.award.service.impl;

import com.gofobao.framework.asset.contants.AssetTypeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.award.repository.VirtualCollectionRepository;
import com.gofobao.framework.award.repository.VirtualTenderRepasitory;
import com.gofobao.framework.award.service.VirtualService;
import com.gofobao.framework.award.vo.response.VirtualStatistics;
import com.gofobao.framework.collection.entity.VirtualCollection;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.tender.contants.VirtualTenderContants;
import com.gofobao.framework.tender.entity.VirtualTender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/8.
 */


@Component
public class VirtualServiceImpl implements VirtualService {


    @Autowired
    private AssetLogRepository assetLogRepository;
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private VirtualTenderRepasitory virtualTenderRepasitory;

    @Autowired
    private VirtualCollectionRepository virtualCollectionRepository;

    /**
     * 体验金统计
     *
     * @param userId
     * @return
     */
    @Override
    public VirtualStatistics statistics(Long userId) {
        VirtualStatistics virtualStatistics = new VirtualStatistics();
        Asset asset = assetRepository.findOne(userId);
        if (ObjectUtils.isEmpty(asset)) {
            return new VirtualStatistics();
        }
        List<AssetLog> assetLogs = assetLogRepository.findByUserIdAndTypeIs(userId, AssetTypeContants.VIRTUAL_TENDER);
        //可用
        virtualStatistics.setAvailable(asset.getVirtualMoney() / 100d);
        //已用
        virtualStatistics.setUsed(assetLogs.stream().mapToInt(m -> m.getMoney()).sum() / 100d);

        List<VirtualTender> virtualTenders = virtualTenderRepasitory.findByUserIdAndStatusIs(userId, VirtualTenderContants.VIRTUALTENDERSUCCESS);
        if (CollectionUtils.isEmpty(virtualTenders)) {
            virtualStatistics.setEarnings(0d);
            return virtualStatistics;
        }
        List<Integer> tenderIdArray = virtualTenders.stream().map(p -> p.getId()).collect(Collectors.toList());
        List<VirtualCollection> virtualCollections = virtualCollectionRepository.findByTenderIdInAndStatusIs(tenderIdArray, VirtualTenderContants.VIRTUALTENDERSUCCESS);
        if (CollectionUtils.isEmpty(virtualCollections)) {
            virtualStatistics.setEarnings(0d);
            return virtualStatistics;
        }
        Integer collectionMoney = virtualCollections.stream().mapToInt(p -> p.getCollectionMoney()).sum();
        virtualStatistics.setEarnings(collectionMoney / 100d);
        return virtualStatistics;
    }
}
