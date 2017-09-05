package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "测试")
@RequestMapping
@Slf4j
public class TestController {
    @Autowired
    MqHelper mqHelper;
    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    AssetChangeProvider assetChangeProvider;

    @RequestMapping("/test/pub/batch/deal/{sourceId}/{batchNo}")
    public void batchDeal(@PathVariable("sourceId") String sourceId, @PathVariable("batchNo") String batchNo) {
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("batchNo", batchNo)
                .eq("sourceId", sourceId)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);

        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, sourceId,
                        MqConfig.BATCH_NO, batchNo,
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                        MqConfig.ACQ_RES, thirdBatchLogList.get(0).getAcqRes()
                );

        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
        }
    }

    @RequestMapping("/test/pub/amendAsset")
    public void amendAsset(){
        String seqNo = assetChangeProvider.getSeqNo(); // 资产记录流水号
        String groupSeqNo = assetChangeProvider.getGroupSeqNo(); // 资产记录分组流水号
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(77994);
        assetChange.setUserId(100009l);
        assetChange.setRemark(String.format("验证服可用金额数据修正，金额：%s元，userId：%s", 779.94, 100009));
        assetChange.setSeqNo(seqNo);
        assetChange.setGroupSeqNo(groupSeqNo);
        assetChange.setSourceId(100009l);
        assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error(String.format("资金变动失败：%s", assetChange));
        }

        assetChange = new AssetChange();
        assetChange.setMoney(8656);
        assetChange.setUserId(100002l);
        assetChange.setRemark(String.format("验证服数据可用金额修正，金额：%s元，userId：%s", 86.56, 100002));
        assetChange.setSeqNo(seqNo);
        assetChange.setGroupSeqNo(groupSeqNo);
        assetChange.setSourceId(100002l);
        assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error(String.format("资金变动失败：%s", assetChange));
        }

        assetChange = new AssetChange();
        assetChange.setMoney(1834);
        assetChange.setUserId(100001l);
        assetChange.setRemark(String.format("验证服数据可用金额修正，金额：%s元，userId：%s", 18.34, 100001));
        assetChange.setSeqNo(seqNo);
        assetChange.setGroupSeqNo(groupSeqNo);
        assetChange.setSourceId(100001l);
        assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error(String.format("资金变动失败：%s", assetChange));
        }

        assetChange = new AssetChange();
        assetChange.setMoney(756921);
        assetChange.setUserId(100001l);
        assetChange.setRemark(String.format("验证服数据冻结金额修正，金额：%s元，userId：%s", 7,569.21, 100001));
        assetChange.setSeqNo(seqNo);
        assetChange.setGroupSeqNo(groupSeqNo);
        assetChange.setSourceId(100001l);
        assetChange.setType(AssetChangeTypeEnum.amendNotUseMoney);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error(String.format("资金变动失败：%s", assetChange));
        }

        assetChange = new AssetChange();
        assetChange.setMoney(691685);
        assetChange.setUserId(100001l);
        assetChange.setRemark(String.format("验证服数据待还金额修正，金额：%s元，userId：%s", 6,916.85, 100001));
        assetChange.setSeqNo(seqNo);
        assetChange.setGroupSeqNo(groupSeqNo);
        assetChange.setSourceId(100001l);
        assetChange.setType(AssetChangeTypeEnum.amendPayment);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error(String.format("资金变动失败：%s", assetChange));
        }
    }
}
