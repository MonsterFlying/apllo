package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
@ApiModel
public class NoticesInfo {
    private String title;
    private String createTime;
    private String content;
    private Boolean read;
}
