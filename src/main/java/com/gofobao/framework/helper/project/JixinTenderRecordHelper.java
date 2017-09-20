package com.gofobao.framework.helper.project;

import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.tender.vo.VoSaveThirdTender;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JixinTenderRecordHelper {
    @Autowired
    RedisHelper redisHelper;

    @Autowired
    JixinManager jixinManager;

    final Gson gson = new GsonBuilder().create();

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    public static final String JIXIN_TENDER_KEY = "JIXIN_TENDER_KEY";

    /**
     * 获取投标记录在redis中的Key
     *
     * @param productId
     * @param isAuto
     * @return
     */
    private String getBorrowTenderKey(String productId, boolean isAuto) {
        return String.format("%s_%s_%s", JIXIN_TENDER_KEY, isAuto ? 1 : 0, productId);
    }

    /**
     * 保存即信投标记录到redis
     *
     * @param voSaveThirdTender
     */
    public void saveJixinTenderInRedis(VoSaveThirdTender voSaveThirdTender) {
        try {
            String key = getBorrowTenderKey(voSaveThirdTender.getProductId(), voSaveThirdTender.getIsAuto());
            String value = redisHelper.get(key, "");
            List<VoSaveThirdTender> all = null;
            if (!StringUtils.isEmpty(value)) {
                all = gson.fromJson(value, new TypeToken<List<VoSaveThirdTender>>() {
                }.getType());
            } else {
                all = new ArrayList<>();
            }
            all.add(voSaveThirdTender);
            redisHelper.put(key, gson.toJson(all));
        } catch (Exception e) {
            exceptionEmailHelper.sendException("保存即信投标记录异常", e);
            log.error("保存即信投标申请记录到redis失败", e);
        }
    }

    /**
     * 取消即信投标记录
     *
     * @param productId
     * @param isAuto
     */
    public void cancelJixinTenderByRedisRecord(String productId, boolean isAuto) {
        try {
            String key = getBorrowTenderKey(productId, isAuto);
            String value = redisHelper.get(key, "");
            List<VoSaveThirdTender> all = null;
            if (!StringUtils.isEmpty(value)) {
                all = gson.fromJson(value, new TypeToken<List<VoSaveThirdTender>>() {
                }.getType());
            } else {
                all = new ArrayList<>();
            }

            if (CollectionUtils.isEmpty(all)) {
                return;
            }
            for (VoSaveThirdTender item : all) {
                BidCancelReq bidCancelReq = new BidCancelReq();
                String orderId = JixinHelper.getOrderId(JixinHelper.CANCEL_TENDER_PREFIX);

                bidCancelReq.setAccountId(item.getAccountId());
                bidCancelReq.setOrderId(orderId);  // 取消投标申请
                bidCancelReq.setOrgOrderId(item.getOrderId()); // 原始投标Id
                bidCancelReq.setProductId(item.getProductId()); // 投标ID
                bidCancelReq.setTxAmount(item.getTxAmount()); // 投标金额

                BidCancelResp bidCancelResp = jixinManager.send(JixinTxCodeEnum.BID_CANCEL, bidCancelReq, BidCancelResp.class);
                if (ObjectUtils.isEmpty(bidCancelResp) || JixinResultContants.SUCCESS.equals(bidCancelResp.getRetCode())) {
                    String data = gson.toJson(bidCancelReq);
                    exceptionEmailHelper.sendErrorMessage("即信自动投标取消失败", data);
                    continue;
                } else {
                    log.info("请求即信取消自动投标成功");
                }
            }
        } catch (Exception e) {
            exceptionEmailHelper.sendException("取消即信投标记录异常", e);
            log.error("保存即信投标申请记录到redis失败", e);
        }
    }

    /**
     * 即信投标记录取消
     * @param productId
     * @param isAuto
     */
    public void removeJixinTenderRecordInRedis(String productId, boolean isAuto) {
        try {
            String key = getBorrowTenderKey(productId, isAuto);
            redisHelper.remove(key) ;
        } catch (Exception e) {
            exceptionEmailHelper.sendException("删除即信投标记录异常", e);
            log.error("删除即信投标记录异常", e);
        }
    }
}
