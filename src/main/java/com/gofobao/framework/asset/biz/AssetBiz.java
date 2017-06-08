package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.VoRechargeBankInfoResp;
import com.gofobao.framework.asset.vo.response.VoRechargeEntityWrapResp;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogWarpRes;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetBiz {

    /**
     * 获取用户资产详情
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoUserAssetInfoResp> userAssetInfo(Long userId);


    /**
     * 用户账户流水
     *
     * @return
     */
    ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLog voAssetLog);


    /**
     * 充值
     *
     * @param request
     * @param voRechargeReq
     * @return
     */
    ResponseEntity<VoHtmlResp> recharge(HttpServletRequest request, VoRechargeReq voRechargeReq);

    /**
     * 充值回调
     * @param request
     * @param response
     * @return
     */
    ResponseEntity<String> rechargeCallback(HttpServletRequest request, HttpServletResponse response) throws Exception;


    /**
     * 获取存管信息
     * @param userId
     * @return
     */
    ResponseEntity<VoRechargeBankInfoResp> bankAcount(Long userId);


    /**
     * 查询充值日志列表
     * @param userId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    ResponseEntity<VoRechargeEntityWrapResp> log(Long userId, int pageIndex, int pageSize);

}
