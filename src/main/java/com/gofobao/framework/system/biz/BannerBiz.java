package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.response.VoViewIndexBannerWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/14.
 */
public interface BannerBiz {

    ResponseEntity<VoViewIndexBannerWarpRes> index(String terminal);
}
