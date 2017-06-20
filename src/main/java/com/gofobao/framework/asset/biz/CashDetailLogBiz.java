package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.response.VoBankApsWrapResp;
import com.gofobao.framework.asset.vo.response.VoCashLogDetailResp;
import com.gofobao.framework.asset.vo.response.VoCashLogWrapResp;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
public interface CashDetailLogBiz {

    /**
     * 提现前缀
     * @param userId
     * @return
     */
    ResponseEntity<VoPreCashResp> preCash(Long userId);

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
    ResponseEntity<VoCashLogDetailResp> logDetail(Long id);


    /**
     * 展示提现结果
     * @param seqNo
     * @param model
     * @return
     */
    String showCash(String seqNo, Model model);
}
