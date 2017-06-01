package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.vo.repsonse.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */
@RestController
@RequestMapping("/assetLog")
@Slf4j
public class AssetLogController {


    @Autowired
    private AssetLogService assetLogService;

    @RequestMapping("list")
    public List<VoViewAssetLogRes> assetLogResList(@ModelAttribute VoAssetLog voAssetLog) {

        List<VoViewAssetLogRes> resList=new ArrayList<>();
        try {
           resList = assetLogService.assetLogList(voAssetLog);
        } catch (Exception e) {
            log.error("assetLog/list exception", e);
        }
        return resList;
    }


}
