package com.gofobao.framework.repayment.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/6.
 */
@Data
public class VoViewCollectionWarpRes extends VoBaseResp{
    @ApiModelProperty("总记录数")
    private Integer totalCount=0;

    private List<VoCollection> voCollections= Lists.newArrayList();

}
