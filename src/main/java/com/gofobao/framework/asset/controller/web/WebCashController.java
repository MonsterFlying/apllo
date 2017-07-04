package com.gofobao.framework.asset.controller.web;
import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "pc:提现")
@RestController
@Slf4j
public class WebCashController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;

    @ApiOperation("pc:提现日志")
    @RequestMapping("cash/pc/v2/list")
    public ResponseEntity<VoCashLogWarpRes> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                 @ModelAttribute VoPcCashLogs cashLogs) {
        return cashDetailLogBiz.psLogs(cashLogs);
    }

}
