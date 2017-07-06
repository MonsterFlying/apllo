package com.gofobao.framework.repayment.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class VoViewOrderListWarpRes extends VoBaseResp {

    private List<VoOrdersList>  ordersLists= Lists.newArrayList();

    @ApiModelProperty("总记录数")
    private Integer totalCount=0;
}
