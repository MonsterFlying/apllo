package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewSettleWarpListRes  extends VoBaseResp {
     @ApiModelProperty()
     List<VoViewSettleRespc> settleRes = Lists.newArrayList();

     @ApiModelProperty()
     private Integer totalCount=0;
}
