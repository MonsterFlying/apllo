package com.gofobao.framework.helper.project;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zeke on 2017/8/3.
 */
@Component
@Slf4j
public class BatchAssetChangeHelper {

    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    private DictItemService dictItemService;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private AssetChangeProvider assetChangeProvider;

    final Gson GSON = new GsonBuilder().create();

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueService.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });


    /**
     * 发放债权转让资金
     *
     * @param sourceId
     * @param batchNo
     */
    public void batchAssetChangeAndCollection(long sourceId, String batchNo, int type) throws Exception {
        Specification<BatchAssetChange> bacs = Specifications
                .<BatchAssetChange>and()
                .eq("sourceId", sourceId)
                .eq("type", type)
                .eq("batchNo", batchNo)
                .in("state", 0, 1)
                .build();
        List<BatchAssetChange> batchAssetChangeList = batchAssetChangeService.findList(bacs);
        Preconditions.checkNotNull(batchAssetChangeList, batchNo + "债权转让资金变动记录不存在!");
        BatchAssetChange batchAssetChange = batchAssetChangeList.get(0);/* 债权转让资金变动记录 */
        if (batchAssetChange.getState() == 1) {
            log.error(String.format("资金变动未已完成!请检查资金是否变动：%s", GSON.toJson(batchAssetChange)));
            return;
        }

        Specification<BatchAssetChangeItem> bacis = Specifications
                .<BatchAssetChangeItem>and()
                .eq("batchAssetChangeId", batchAssetChange.getId())
                .eq("state", 0)
                .build();
        List<BatchAssetChangeItem> batchAssetChangeItemList = batchAssetChangeItemService.findList(bacis);
        Preconditions.checkNotNull(batchAssetChangeItemList, batchNo + "债权转让资金变动子记录不存在!");

        // 所有的资金变动
        for (BatchAssetChangeItem item : batchAssetChangeItemList) {
            AssetChange assetChange = new AssetChange();
            Long interest = item.getInterest();
            assetChange.setInterest(ObjectUtils.isEmpty(interest) ? 0 : interest);
            Long principal = item.getPrincipal();
            assetChange.setPrincipal(ObjectUtils.isEmpty(principal) ? 0 : principal);
            Long moeny = item.getMoney();
            assetChange.setMoney(ObjectUtils.isEmpty(moeny) ? 0 : moeny);
            assetChange.setForUserId(item.getForUserId());
            assetChange.setUserId(item.getUserId());
            assetChange.setRemark(item.getRemark());
            assetChange.setSeqNo(item.getSeqNo());
            assetChange.setGroupSeqNo(item.getGroupSeqNo());
            assetChange.setSourceId(item.getSourceId());
            assetChange.setType(AssetChangeTypeEnum.findType(item.getType()));
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
                throw new Exception(e);
            }

            item.setState(1);
            item.setUpdatedAt(new Date());
        }
        //更新批次资金变动 与 子记录的状态
        batchAssetChange.setState(1);
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChangeService.save(batchAssetChange);
        batchAssetChangeItemService.save(batchAssetChangeItemList);

    }

}
