package com.gofobao.framework.system.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.vo.request.VoFindThirdBatch;
import com.gofobao.framework.system.vo.request.VoSendThirdBatch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/9/8.
 */
@Api(description = "pc:即信批次日志")
@RestController
public class WebThirdBatchLogController {
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;

    /**
     * 查询批次状态
     *
     * @return
     */
    @PostMapping(value = "/pub/third/batch/deal/find")
    @ApiOperation("发送即信批次处理")
    public ResponseEntity<VoBaseResp> findThirdThirdBatch(VoFindThirdBatch voFindThirdBatch) {
        return thirdBatchLogBiz.findThirdThirdBatch(voFindThirdBatch);
    }

    /**
     * 发送即信批次处理
     *
     * @param voSendThirdBatch
     * @return
     */
    @PostMapping(value = "/pub/third/batch/deal/send")
    @ApiOperation("发送即信批次处理")
    public ResponseEntity<VoBaseResp> sendThirdBatchDeal(@Valid @ModelAttribute VoSendThirdBatch voSendThirdBatch) {
        return thirdBatchLogBiz.sendThirdBatchDeal(voSendThirdBatch);
    }
}
