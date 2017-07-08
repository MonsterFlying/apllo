package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Data
public class VoViewCouponWarpRes extends VoBaseResp {
    private List<CouponRes> couponList= Lists.newArrayList();

    @ApiModelProperty("总记录数")
    private Integer totalCount=0;
}
