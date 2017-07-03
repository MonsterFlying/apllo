package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "pc:提现")
@RestController
@Slf4j
public class WebCashController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;


}
