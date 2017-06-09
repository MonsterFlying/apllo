package com.gofobao.framework.award.service.impl;

import com.gofobao.framework.asset.contants.AssetTypeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.award.repository.VirtualBorrowRepository;
import com.gofobao.framework.award.repository.VirtualCollectionRepository;
import com.gofobao.framework.award.repository.VirtualTenderRepository;
import com.gofobao.framework.award.service.VirtualService;
import com.gofobao.framework.award.vo.response.VirtualBorrowRes;
import com.gofobao.framework.award.vo.response.VirtualStatistics;
import com.gofobao.framework.award.vo.response.VirtualTenderRes;
import com.gofobao.framework.borrow.contants.BorrowVirtualContants;
import com.gofobao.framework.borrow.entity.BorrowVirtual;
import com.gofobao.framework.collection.entity.VirtualCollection;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.contants.VirtualTenderContants;
import com.gofobao.framework.tender.entity.VirtualTender;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private VirtualTenderRepository virtualTenderRepository;

    @Autowired
    private VirtualCollectionRepository virtualCollectionRepository;

    @Autowired
    private VirtualBorrowRepository virtualBorrowRepository;


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

        List<VirtualTender> virtualTenders = virtualTenderRepository.findByUserIdAndStatusIs(userId, VirtualTenderContants.VIRTUALTENDERSUCCESS);
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

    /**
     * 用户投资体验金列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<VirtualTenderRes> userTenderList(Long userId) {
        List<AssetLog> assetLogs = assetLogRepository.findByUserIdAndTypeIs(userId, AssetTypeContants.VIRTUAL_TENDER);
        if (CollectionUtils.isEmpty(assetLogs)) {
            return Collections.EMPTY_LIST;
        }

        List<VirtualTenderRes> virtualTenderRes = Lists.newArrayList();
        assetLogs.stream().forEach(p -> {
            VirtualTenderRes item = new VirtualTenderRes();
            item.setMoney(StringHelper.toString(p.getVirtualMoney() / 100d));
            item.setRemark(p.getRemark());
            item.setTime(DateHelper.dateToString(p.getCreatedAt()));
            virtualTenderRes.add(item);
        });
        return Optional.ofNullable(virtualTenderRes).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public List<VirtualBorrowRes> list() {
        List<BorrowVirtual> virtualList = virtualBorrowRepository.findByStatus(BorrowVirtualContants.STATUS_YES);
        if (CollectionUtils.isEmpty(virtualList)) {
            return Collections.EMPTY_LIST;
        }
        List<VirtualBorrowRes> virtualBorrowRes = Lists.newArrayList();
        virtualList.stream().forEach(p -> {
            VirtualBorrowRes borrowRes = new VirtualBorrowRes();
            borrowRes.setId(p.getId());
            borrowRes.setMoney(StringHelper.toString(p.getMost() / 100));
            borrowRes.setTimeLimit(p.getTimeLimit());
            borrowRes.setApr(StringHelper.toString(p.getApr() / 100));
            borrowRes.setName(p.getName());
            borrowRes.setRepayFashion("一次性还本付息");
            virtualBorrowRes.add(borrowRes);
        });
        return Optional.ofNullable(virtualBorrowRes).orElse(Collections.EMPTY_LIST);
    }
}
