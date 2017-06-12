package com.gofobao.framework.borrow.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/17.
 */

@Data
public class VoBorrowListReq extends Page {
    @ApiModelProperty("0：车贷标；1：净值标；4：渠道标；")
    private Integer type;



}
