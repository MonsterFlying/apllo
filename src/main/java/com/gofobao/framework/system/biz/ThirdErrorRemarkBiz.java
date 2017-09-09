package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.vo.request.VoDealThirdErrorReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/8/25.
 */
public interface ThirdErrorRemarkBiz {

    /**
     * 处理失败批次
     *
     * @return
     */
    ResponseEntity<VoBaseResp> dealThirdError(VoDealThirdErrorReq voDealThirdErrorReq);

}
