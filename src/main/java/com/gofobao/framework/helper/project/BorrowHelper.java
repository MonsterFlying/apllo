package com.gofobao.framework.helper.project;

import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.tender.vo.VoSaveThirdTender;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Max on 2017/3/27.
 */
@Slf4j
@Service
public class BorrowHelper {

    @Autowired
    private RedisHelper redisHelper;

    final Gson gson = new GsonBuilder().create();

    public static final String BORROW_TENDER_KEY = "BORROW_TENDER_KEY";

    /**
     * 保存即信投标记录到redis里面
     *
     * @param voSaveThirdTender
     */
    public void saveBorrowTenderInRedis(VoSaveThirdTender voSaveThirdTender) {
        //获取redis里的即信投标记录
        List<VoSaveThirdTender> voSaveThirdTenders = getBorrowTenderInRedis();
        //保存即信投标记录到redis里面
        voSaveThirdTenders.add(voSaveThirdTender);
        try {
            redisHelper.put(BORROW_TENDER_KEY, gson.toJson(voSaveThirdTenders));
        } catch (Exception e) {
            log.error("BorrowHelper saveBorrowTenderInRedis 保存即信投标申请失败:", e);
        }
    }

    /**
     * 获取redis里的即信投标记录
     */
    public List<VoSaveThirdTender> getBorrowTenderInRedis() {
        String voSaveThirdTendersStr = "";
        try {
            if (redisHelper.hasKey(BORROW_TENDER_KEY)) {
                voSaveThirdTendersStr = redisHelper.get(BORROW_TENDER_KEY, "");
            }
        } catch (Exception e) {
            log.error("BorrowHelper saveBorrowTenderInRedis 从redis中取出投标申请列表失败:", e);
        }
        List<VoSaveThirdTender> voSaveThirdTenders = null;
        //判断字符串是否存在
        if (!StringUtils.isEmpty(voSaveThirdTendersStr)) {
            voSaveThirdTenders = gson.fromJson(voSaveThirdTendersStr, new TypeToken<List<VoSaveThirdTender>>() {
            }.getType());
        } else {
            voSaveThirdTenders = new ArrayList<>();
        }
        return voSaveThirdTenders;
    }

    /**
     * 获取redis里的即信投标记录
     */
    public List<VoSaveThirdTender> getBorrowTenderInRedis(String productId, boolean isAuto) {
        List<VoSaveThirdTender> voSaveThirdTenderList = getBorrowTenderInRedis();
        if (CollectionUtils.isEmpty(voSaveThirdTenderList)) {
            return voSaveThirdTenderList;
        } else {
            return voSaveThirdTenderList.stream().filter(voSaveThirdTender ->
                    voSaveThirdTender.getIsAuto() == isAuto
                            && productId.equals(voSaveThirdTender.getProductId()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取redis里的即信投标记录
     */
    public void deleteBorrowTenderInRedis(List<VoSaveThirdTender> delVoSaveThirdTenderList) {
        if (!CollectionUtils.isEmpty(delVoSaveThirdTenderList)) {
            //全部取消投标申请
            List<VoSaveThirdTender> voSaveThirdTenderList = getBorrowTenderInRedis();
            voSaveThirdTenderList.removeAll(delVoSaveThirdTenderList);
            try {
                redisHelper.put(BORROW_TENDER_KEY, gson.toJson(voSaveThirdTenderList));
            } catch (Exception e) {
                log.error("BorrowHelper saveBorrowTenderInRedis 保存即信投标申请失败:", e);
            }
        }
    }

    /**
     * 计算RepayFashions
     *
     * @param repayFashions borrow表的repayFashion字段
     * @return
     */
    public static String countRepayFashions(Integer... repayFashions) {
        StringBuffer condition = new StringBuffer();//拼接条件
        StringBuffer binary;
        String tempStr;
        char[] binaryChar;
        for (int i = 1, len = MathHelper.pow(2, 3); i < len; i++) {
            binary = new StringBuffer();
            binaryChar = new char[3];
            tempStr = Integer.toBinaryString(i) + "";
            for (int j = 0; j < 3 - tempStr.length(); j++) {
                binary.append("0");
            }
            binary.append(tempStr).reverse();

            Set<Integer> num = new HashSet<>();
            binary.getChars(0, binary.length(), binaryChar, 0);
            for (Integer repayFashion : repayFashions) {
                if (binaryChar[repayFashion] == '1') {
                    num.add(i);
                }
            }

            for (Integer tempNum : num) {
                condition.append(tempNum).append(",");
            }
        }
        return condition.substring(0, condition.length() - 1);
    }

    /**
     * 获取债权转让费率
     *
     * @param leftOrder
     * @return
     */
    public static double getTransferFeeRate(int leftOrder) {
        return Math.min(0.004 + 0.0008 * (leftOrder - 1), 0.0128); // 获取债权转让费用
    }
}
