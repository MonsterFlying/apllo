package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/7.
 */
public interface RedPackageBiz {

    ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq);
}
