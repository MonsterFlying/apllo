package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.response.VoIndexResp;
import org.springframework.http.ResponseEntity;

public interface HomeBiz {

    /**
     * 首页
     * @return
     */
    ResponseEntity<VoIndexResp> home();

}
