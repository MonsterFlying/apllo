package com.gofobao.framework.integral.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.integral.vo.response.VoIntegral;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
@Data
public class VoViewIntegralWarpRes extends VoBaseResp {
        private List<VoIntegral>integrals= Lists.newArrayList();

        @ApiModelProperty("总条数")
        private Integer totalCount=0;


}
