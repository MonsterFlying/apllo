package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.vo.request.VoCountReq;
import com.gofobao.framework.member.vo.response.VoSiteSumBalanceResp;
import com.gofobao.framework.scheduler.service.CountAssetInfo;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * Created by xin on 2017/12/14.
 */
@RestController
public class SiteBalanceController {
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private CountAssetInfo countAssetInfo;

    @PostMapping("/site/balance")
    public ResponseEntity<VoSiteSumBalanceResp> siteBalance(VoCountReq voCountReq) {
        if (ObjectUtils.isEmpty(voCountReq)
                || StringUtils.isEmpty(voCountReq.getParamStr())
                || StringUtils.isEmpty(voCountReq.getSign())
                || !SecurityHelper.checkSign(voCountReq.getSign(), voCountReq.getParamStr())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"非法访问",VoSiteSumBalanceResp.class));
        }
        Map<String, String> map = new Gson().fromJson(voCountReq.getParamStr(), new TypeToken<Map<String,String>>(){}.getType());
        return userCacheService.findByDate(DateHelper.stringToDate(map.get("date")));
    }

    @GetMapping("/site/count/execute")
    public ResponseEntity<VoBaseResp> executeCount(VoCountReq voCountReq) {
        if (ObjectUtils.isEmpty(voCountReq)
                || StringUtils.isEmpty(voCountReq.getParamStr())
                || StringUtils.isEmpty(voCountReq.getSign())
                || !SecurityHelper.checkSign(voCountReq.getSign(), voCountReq.getParamStr())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"非法访问",VoBaseResp.class));
        }
        countAssetInfo.dayStatistic(new Date());
        return ResponseEntity.ok(VoBaseResp.ok("手动执行成功"));
    }

}
