package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.response.VoUserAssetInfoResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetBiz {

    /**
     * 获取用户资产详情
     * @param userId
     * @return
     */
    ResponseEntity<VoUserAssetInfoResp> userAssetInfo(Long userId);
}
