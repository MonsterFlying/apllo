package com.gofobao.framework.collection.vo.response.web;

import com.gofobao.framework.system.vo.response.VoFindRepayStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/7/11.
 */
@ApiModel("回款详情")
@Data
public class CollectionDetail {
    private Long borrowId;
    private String name;
    private String orderStr;
    private String principal;
    private String interest;
    private String earnings;
    private String collectionAt;
    @ApiModelProperty("还款状态")
    private List<VoFindRepayStatus> voFindRepayStatusList;
}
