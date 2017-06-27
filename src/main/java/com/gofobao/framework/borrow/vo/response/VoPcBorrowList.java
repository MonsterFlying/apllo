package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/27.
 */
@Data
public class VoPcBorrowList {

    @ApiModelProperty("当前页")
    private Integer pageIndex;

    @ApiModelProperty("页面大小")
    private Integer pageSize;

    @ApiModelProperty("总记录数")
    private Integer totalCount;

    @ApiModelProperty("集合")
    private List<VoViewBorrowList> borrowLists;
}
