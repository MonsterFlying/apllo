package com.gofobao.framework.repayment.vo.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by admin on 2017/6/5.
 */
@Data
@ApiModel("借款详情")
public class VoDetailReq {

    private Long userId;

    private Long borrowId;
}
