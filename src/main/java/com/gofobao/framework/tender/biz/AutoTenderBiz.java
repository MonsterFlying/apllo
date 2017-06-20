package com.gofobao.framework.tender.biz;

import com.gofobao.framework.tender.vo.response.VoViewUserAutoTenderWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderBiz {
    ResponseEntity<VoViewUserAutoTenderWarpRes> list(Long userId);

}
