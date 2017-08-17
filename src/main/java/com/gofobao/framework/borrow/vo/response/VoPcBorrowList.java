package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/27.
 */
@Data
public class VoPcBorrowList extends VoBaseResp{

    @ApiModelProperty("当前页")
    private Integer pageIndex=0;

    @ApiModelProperty("页面大小")
    private Integer pageSize=10;

    @ApiModelProperty("总记录数")
    private Integer totalCount=0;

    @ApiModelProperty("集合")
    private List<VoViewBorrowList> borrowLists= Lists.newArrayList();
}
