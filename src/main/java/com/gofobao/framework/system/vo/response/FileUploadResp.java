package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动首页
 * Created by Administrator on 2017/6/20 0020.
 */
@Data
@ApiModel
public class FileUploadResp extends VoBaseResp {

    @Data
    @ApiModel
    public static class ImageKey {
        @ApiModelProperty("图片唯一信息")
        private String key;

        @ApiModelProperty("图片全路径")
        private String imagesUrl;
    }

    @ApiModelProperty("图片上传结果集")
    List<ImageKey> images = new ArrayList<>();
}



