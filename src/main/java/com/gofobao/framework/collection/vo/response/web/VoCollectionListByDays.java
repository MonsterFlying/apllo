package com.gofobao.framework.collection.vo.response.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/7/11.
 */
@ApiModel("回款详情视图")
@Data
public class VoCollectionListByDays extends VoBaseResp{
    @ApiModelProperty("回款详情列表")
    private List<CollectionDetail> collectionDetailList;
}
