package com.gofobao.framework.marketing.biz;

import com.gofobao.framework.system.vo.response.VoEventWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * 营销系统
 */
public interface MarketingBiz {

    void autoCancelRedpack();

    ResponseEntity<VoEventWarpRes> list();

}
