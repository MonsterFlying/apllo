package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.DictBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusListReq;
import com.gofobao.framework.system.vo.response.VoServiceResp;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 关于我们
 * Created by Max on 17/5/22.
 */
@RestController
public class ThirdBatchDealController {

    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;


    /**
     * 查询放款状态集合
     *
     * @param voFindLendRepayStatusListReq
     * @return
     */
    @ApiOperation("查询放款状态集合")
    @PostMapping("/thirdbatchdeal/pub/lendrepay/find/list")
    public ResponseEntity<VoViewFindLendRepayStatusListRes> findLendRepayStatusList(@ModelAttribute @Valid VoFindLendRepayStatusListReq voFindLendRepayStatusListReq) {
        return thirdBatchDealLogBiz.findLendRepayStatusList(voFindLendRepayStatusListReq);
    }

}
