package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.response.VoFindIndexResp;
import org.springframework.http.ResponseEntity;

/**
 * 发现模块
 */
public interface FindBiz {


    /**
     * 发现首页
     * @return
     */
    ResponseEntity<VoFindIndexResp> index();

    /**
     * 理财计划发现首页
     * @return
     */
    ResponseEntity<VoFindIndexResp> financeIndex();
}
