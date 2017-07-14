package com.gofobao.framework.asset.controller.web;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "资金流水")
@RestController
@Slf4j
public class WebAssetLogController {

    @Autowired
    private AssetBiz assetBiz;

@Autowired
private AssetLogRepository assetRepository;

    @RequestMapping(value = "pub/assetLog/pc/v2/list", method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogsWarpRes> pcAssetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq,
                                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.pcAssetLogs(voAssetLogReq);
    }


    @ApiOperation("资金流水导出")
    @RequestMapping(value = "pub/assetLog/pc/v2/toExcel", method = RequestMethod.GET)
    public void pcAssetLogToExcel(HttpServletResponse response, @ModelAttribute VoAssetLogReq voAssetLogReq/*,
                                            @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        voAssetLogReq.setUserId(901L);
        assetBiz.pcToExcel(voAssetLogReq,response);

    }



}
