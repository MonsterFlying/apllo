package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/7/3.
 */
public interface RechargeLogsBiz {

    ResponseEntity<VoViewRechargeWarpRes>logs(VoPcRechargeReq rechargeReq);



    /**
     * pc:充值记录导出
     * @param rechargeReq
     */
    void pcToExcel(VoPcRechargeReq rechargeReq, HttpServletResponse response);

}
