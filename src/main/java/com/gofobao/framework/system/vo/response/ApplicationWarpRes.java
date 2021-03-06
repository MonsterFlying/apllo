package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/10/25.
 */
@Data
public class ApplicationWarpRes extends VoBaseResp {

    private List<VoApplication> applications = Lists.newArrayList();

    @Data
    public class VoApplication{
        private Integer applicationId;

        @ApiModelProperty("应用别名")
        private String aliasName;

        @ApiModelProperty("二维码地址")
        private String qrodeUrl;

        @ApiModelProperty("应用logo")
        private String logo;

        @ApiModelProperty("应用简介")
        private String sketch;

        @ApiModelProperty("1:android; 2:ios")
        private Integer terminal;
    }


}
