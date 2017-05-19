package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/18.
 */
@Data
public class VoBorrowByIdReq {
        @ApiModelProperty("标id")
        private Integer borrowId;

}
