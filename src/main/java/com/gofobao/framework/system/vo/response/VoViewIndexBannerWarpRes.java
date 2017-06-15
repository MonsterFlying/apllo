package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class VoViewIndexBannerWarpRes extends VoBaseResp{
    private List<IndexBanner> bannerList= Lists.newArrayList();
}
