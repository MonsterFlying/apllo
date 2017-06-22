package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

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
    ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLogReq voAssetLogReq);


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


    /**
     * 充值页面前置请求
     * @param userId
     * @return
     */
    ResponseEntity<VoPreRechargeResp> preRecharge(Long userId);

    /**
     * 获取资产首页金额信息
     * @param userId
     * @return
     */
    ResponseEntity<VoAssetIndexResp> asset(Long userId);

    /**
     * 累计用户收益
     * @param userId
     * @return
     */
    ResponseEntity<VoAccruedMoneyResp> accruedMoney(Long userId);


    /**
     * 账户余额
     * @param userId
     * @return
     */
    ResponseEntity<VoAvailableAssetInfoResp> accountMoney(Long userId);

    /**
     *  待收
     * @param userId
     * @return
     */
    ResponseEntity<VoCollectionResp> collectionMoney(Long userId);


    /**
     * pc:资金流水
     * @param voAssetLogReq
     * @return
     */
    ResponseEntity<AssetLog> pcLogs(VoAssetLogReq voAssetLogReq);

    String rechargeShow(HttpServletRequest request, Model model, String seqNo);


    /**
     * 联机充值
     * @param request
     * @param voRechargeReq
     * @return
     */
    ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, VoRechargeReq voRechargeReq);
}
