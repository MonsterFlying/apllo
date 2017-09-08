package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.*;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.response.pc.ExpenditureDetail;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import com.gofobao.framework.member.vo.response.pc.VoViewAssetStatisticWarpRes;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetBiz {

    /**
     * 撤回即信红包
     *
     * @param voUnsendRedPacket
     * @return
     */
    ResponseEntity<VoBaseResp> unsendRedPacket(VoUnsendRedPacket voUnsendRedPacket);

    /**
     * 发送即信红包
     *
     * @param voSendRedPacket
     * @return
     */
    ResponseEntity<VoBaseResp> sendRedPacket(VoSendRedPacket voSendRedPacket);

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
    ResponseEntity<VoAliPayRechargeInfo> alipayBankInfo(Long userId);


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
    ResponseEntity<VoViewAssetLogsWarpRes> pcAssetLogs(VoAssetLogReq voAssetLogReq);

    /**
     * pc:流水导出
     * @param voAssetLogReq
     */
    void pcToExcel(VoAssetLogReq voAssetLogReq,HttpServletResponse response);



    String rechargeShow(HttpServletRequest request, Model model, String seqNo);


    /**
     * 联机充值
     * @param request
     * @param voRechargeReq
     * @return
     */
    ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, VoRechargeReq voRechargeReq) throws Exception;


    /**
     * 资金同步
     * @param userId
     * @return
     */
    ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(Long userId) throws Exception;


    /**
     * 账户总额統計
     * @param userId
     * @return
     */
    ResponseEntity<VoViewAssetStatisticWarpRes>pcAccountStatstic(Long userId);

    /**
     * 已賺收益統計
     * @param userId
     * @return
     */
    ResponseEntity<IncomeEarnedDetail> pcIncomeEarned(Long userId);

    /**
     * 支出统计
     * @param userId
     * @return
     */
    ResponseEntity<ExpenditureDetail>pcExpenditureDetail(Long userId);



    /**
     *  后台资金同步
     * @param voSynAssetsRep
     * @return
     */
    ResponseEntity<VoUserAssetInfoResp> adminSynOffLineRecharge(VoSynAssetsRep voSynAssetsRep) throws Exception;

    /**
     * 银行转账
     * @param userId
     * @return
     */
    ResponseEntity<VoUnionRechargeInfo> unionBankInfo(Long userId);

    /**
     * 新版资金流水
     * @param voAssetLogReq
     * @return
     */
    ResponseEntity<VoViewAssetLogWarpRes> newAssetLogResList(VoAssetLogReq voAssetLogReq);

    ResponseEntity<VoAssetIndexResp> synHome(Long userId) throws Exception;

}
