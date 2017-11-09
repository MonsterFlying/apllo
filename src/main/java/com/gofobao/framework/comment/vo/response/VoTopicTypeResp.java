package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by xin on 2017/11/8.
 */
@Data
@ApiModel("主题类型")
public class VoTopicTypeResp extends VoBaseResp {
   @ApiModelProperty("主题类型")
   private String  topicTypeName;

   @ApiModelProperty("最热标识")
   private Integer hotState;

   @ApiModelProperty("最新标识")
   private Integer newState;

   @ApiModelProperty("帖子总数")
   private Integer topicTotalNum;

   @ApiModelProperty("主题类型icon")
   private String iconUrl;

   @ApiModelProperty("创建时间")
   private Date createDate;

   @ApiModelProperty("更新时间")
   private Date updateDate;

   @ApiModelProperty("主题管理员")
   private long adminId;

   @ApiModelProperty("记录主题有效状态")
   private Integer del;
}
