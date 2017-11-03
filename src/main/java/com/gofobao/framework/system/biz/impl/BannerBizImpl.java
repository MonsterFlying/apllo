package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.system.biz.BannerBiz;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.vo.response.BanerCache;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.gofobao.framework.system.vo.response.VoIndexResp;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/14.
 */
@Service
@Slf4j
public class BannerBizImpl implements BannerBiz {

    @Autowired
    private BannerService bannerService;

    @Override
    public ResponseEntity<VoIndexResp> index(String terminal) {
        VoIndexResp warpRes = VoBaseResp.ok("", VoIndexResp.class);
        try {
            List<IndexBanner> bannerList = bannerService.index(terminal);
            warpRes.setBannerList(bannerList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoIndexResp.class));
        }
    }

    @Override
    public ResponseEntity<BanerCache> clear(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        BanerCache banerCache = new BanerCache();

        //
        if (ObjectUtils.isEmpty(voDoAgainVerifyReq)
                || StringUtils.isEmpty(voDoAgainVerifyReq.getParamStr())
                || StringUtils.isEmpty(voDoAgainVerifyReq.getSign())
                || !SecurityHelper.checkSign(voDoAgainVerifyReq.getSign(), voDoAgainVerifyReq.getParamStr())) {
            log.info("非法访问");
            banerCache.setMsg("非法访问");
            return ResponseEntity.badRequest().body(banerCache);

        }
        Map<String, String> paramMap = new Gson().fromJson(voDoAgainVerifyReq.getParamStr(), new TypeToken<Map<String, String>>() {
        }.getType());

        String terminal = paramMap.get("terminal");

        Map<Integer, String> map = new HashMap<>();
        map.put(0, "mobile");
        map.put(1, "pc");
        banerCache.setStatusCode(VoBaseResp.OK);
        banerCache.setMsg("banner缓存已清除");
        try {
            if (!map.containsKey(Integer.valueOf(terminal))) {
                throw new Exception("terminal is error");
            }
            bannerService.clear(map.get(Integer.valueOf(terminal)));
            log.info("banner缓存已清除");
            return ResponseEntity.ok(banerCache);
        } catch (Exception e) {
            banerCache.setStatusCode(VoBaseResp.ERROR);
            banerCache.setMsg("清除缓存失败");
            log.info("banner缓存清除失败");
            return ResponseEntity.badRequest()
                    .body(banerCache);
        }
    }
}
