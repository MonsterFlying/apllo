package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import org.springframework.http.ResponseEntity;

import java.util.Date;

/**
 * 资金同步
 */
public interface AssetSynBiz {

    /**
     * 资金同步并且返回资金
     *
     * @param userId
     * @return
     */
    boolean doAssetSyn(Long userId) throws Exception;


    /**
     * 统计线下转账
     * <p>
     * 说明:
     * 考虑现在同步是主动, 防止有遗漏
     *
     * @param date
     * @return
     */
    boolean doOffLineRechargeByAleve(String date) throws Exception;


    /**
     * 后台资金同步
     *
     * @param userId
     * @param synDate
     * @return
     */
    boolean doAdminSynAsset(Long userId, Date synDate) throws Exception;
}
