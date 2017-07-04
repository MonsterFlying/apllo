package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/7/3.
 */
public interface RechargeLogsBiz {

    ResponseEntity<VoViewRechargeWarpRes>logs(VoPcRechargeReq rechargeReq);

}
