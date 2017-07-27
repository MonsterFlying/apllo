package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoAdminCashReq;
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.VoBankApsWrapResp;
import com.gofobao.framework.asset.vo.response.VoCashLogDetailResp;
import com.gofobao.framework.asset.vo.response.VoCashLogWrapResp;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
public interface CashDetailLogBiz {

    /**
     * 提现前缀
     * @param userId
     * @param httpServletRequest
     * @return
     */
    ResponseEntity<VoPreCashResp> preCash(Long userId, HttpServletRequest httpServletRequest);

    /**
     * 提现
     *
     * @param httpServletRequest
     * @param userId
     * @param voCashReq
     * @return
     */
    ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest, Long userId, VoCashReq voCashReq) throws  Exception;

    /**
     * 查询联行号
     *
     * @param userId
     * @param voBankApsReq
     * @return
     */
    ResponseEntity<VoBankApsWrapResp> bankAps(Long userId, VoBankApsReq voBankApsReq);


    /**
     * 提现回调
     * @param request
     * @return
     */
    ResponseEntity<String> cashCallback(HttpServletRequest request) throws Exception;


    /**
     * 提现日志
     * @param userId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    ResponseEntity<VoCashLogWrapResp> log(Long userId, int pageIndex, int pageSize);




    /**
     * 提现详情
     * @param id
     * @return
     */
    ResponseEntity<VoCashLogDetailResp> logDetail(Long id,Long userId);


    /**
     * 展示提现结果
     * @param seqNo
     * @param model
     * @return
     */
    String showCash(String seqNo, Model model);

    /**
     * pc： 提现日志
     * @param cashLogs
     * @return
     */
    ResponseEntity<VoCashLogWarpRes> psLogs(VoPcCashLogs cashLogs);

    /**
     * 后台提现
     * @param httpServletRequest
     * @param voAdminCashReq
     * @return
     */
    ResponseEntity<VoHtmlResp> adminWebCash(HttpServletRequest httpServletRequest, VoAdminCashReq voAdminCashReq) throws Exception;

    /**
     * PC 提现记录导出
     * @param cashLogs
     * @param response
     */
    void toExcel(VoPcCashLogs cashLogs, HttpServletResponse response);


    /**
     * @param cashId
     * @param curNum
     * @param totalNum
     * @return
     */
    boolean doFormCashMoney(Long cashId, Integer curNum, Integer totalNum) throws Exception;
}
