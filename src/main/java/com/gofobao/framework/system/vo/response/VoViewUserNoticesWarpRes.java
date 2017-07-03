package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class VoViewUserNoticesWarpRes extends VoBaseResp {
    @ApiModelProperty("总页数")
    private int pageCount;
    @ApiModelProperty("列表")
    private List<UserNotices> notices = Lists.newArrayList();
}
