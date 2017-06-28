package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class VoViewCollectionDaysWarpRes extends VoBaseResp{
    @ApiModelProperty("有回款;还款日期 warpRes: [25, 26, 27, 28, 29]")
    List<Integer> warpRes= Lists.newArrayList();
}
