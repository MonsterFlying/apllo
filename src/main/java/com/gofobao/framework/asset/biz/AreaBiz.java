package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.response.pc.VoAreaWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/8/19.
 */
public interface AreaBiz {

    ResponseEntity<VoAreaWarpRes> list(String id);
}
