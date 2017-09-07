package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/7.
 */
@Data
public class UserAccountThirdTxReq extends Page{

    @ApiModelProperty(hidden =true)
    private Long userId;

    @ApiModelProperty("0-所有交易\n" +
            "1-转入交易\n" +
            "2-转出交易\n" +
            "9-指定交易类型")
    private String type;

}
