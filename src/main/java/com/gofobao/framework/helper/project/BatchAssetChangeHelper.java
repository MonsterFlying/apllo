package com.gofobao.framework.helper.project;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.constans.JixinContants;
import com.gofobao.framework.helper.BooleanHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;
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
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private DictItemService dictItemService;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;

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
    public void batchAssetChange(long sourceId, long batchNo) {
        Specification<BatchAssetChange> bacs = Specifications
                .<BatchAssetChange>and()
                .eq("sourceId", sourceId)
                .eq("type", BatchAssetChangeContants.BATCH_CREDIT_INVEST)
                .eq("batchNo", batchNo)
                .build();
        List<BatchAssetChange> batchAssetChangeList = batchAssetChangeService.findList(bacs);
        Preconditions.checkNotNull(batchAssetChangeList, batchNo + "债权转让资金变动记录不存在!");
        BatchAssetChange batchAssetChange = batchAssetChangeList.get(0);/* 债权转让资金变动记录 */

        Specification<BatchAssetChangeItem> bacis = Specifications
                .<BatchAssetChangeItem>and()
                .eq("batchAssetChangeId", batchAssetChange.getId())
                .eq("state", 0)
                .build();
        List<BatchAssetChangeItem> batchAssetChangeItemList = batchAssetChangeItemService.findList(bacis);
        Preconditions.checkNotNull(batchAssetChangeItemList, batchNo + "债权转让资金变动子记录不存在!");
        batchAssetChangeItemList.stream().forEach(batchAssetChangeItem -> {
            //发送存管红包
            if (BooleanHelper.isTrue(batchAssetChangeItem.getSendRedPacket())) {
                UserThirdAccount transferUserThirdAccount = userThirdAccountService.findByUserId(batchAssetChangeItem.getUserId()); /* 债权转让人存管账号 */
                //通过红包账户发放
                //调用即信发放债权转让人应收利息
                //查询红包账户
                DictValue dictValue = null;
                try {
                    dictValue = jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                } catch (ExecutionException e) {
                    log.error("transferBizImpl batchAssetChange 获取存管红包账户失败：", e);
                }
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                voucherPayRequest.setTxAmount(StringHelper.toString(batchAssetChangeItem.getMoney()));//扣除手续费
                voucherPayRequest.setForAccountId(transferUserThirdAccount.getAccountId());
                voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                voucherPayRequest.setDesLine(batchAssetChangeItem.getRemark());
                voucherPayRequest.setChannel(ChannelContant.HTML);
                VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                    log.error("BorrowRepaymentThirdBizImpl 调用即信发送发放债权转让人应收利息异常:" + msg);
                }
            }
            //扣减本地资金
            CapitalChangeEntity capitalChangeEntity = GSON.fromJson(GSON.toJson(batchAssetChangeItem), new TypeToken<CapitalChangeEntity>() {
            }.getType());
            try {
                capitalChangeHelper.capitalChange(capitalChangeEntity);
            } catch (Exception e) {
                log.error("transferBizImpl batchAssetChange assetChange error:", e);
            }
        });
    }
}
