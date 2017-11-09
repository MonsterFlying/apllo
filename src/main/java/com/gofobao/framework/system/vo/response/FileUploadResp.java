package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 活动首页
 * Created by Administrator on 2017/6/20 0020.
 */
@Data
@ApiModel
public class FileUploadResp extends VoBaseResp {

    @ApiModelProperty(value = "图片全链接")
    private String imageUrl ;

    @ApiModelProperty(value = "图片唯一id, 当用户放弃编辑时, 请该参数调用删除图片接口")
    private String key ;
}
