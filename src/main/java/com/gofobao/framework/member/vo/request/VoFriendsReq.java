package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoFriendsReq extends Page {
    @ApiModelProperty(hidden = true )
    private Long userId;

}
