package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.response.VoViewVirtualStatisticsWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/8.
 */
public interface VirtualBiz {

    ResponseEntity<VoViewVirtualStatisticsWarpRes>query(Long userId);

}
