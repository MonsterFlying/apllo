package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by Max on 2017/3/31.
 */
@ApiModel
public class VoViewLendBlacklists {
    @ApiModelProperty("黑名单列表")
    private List<VoLendBlacklist> blacklists;
    @ApiModelProperty("本页页码")
    private Integer pageIndex;
    @ApiModelProperty("本页内容数")
    private Integer pageSize;

    @ApiModelProperty("总记录数")
    private Integer totalCount = 0;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<VoLendBlacklist> getBlacklists() {
        return blacklists;
    }

    public void setBlacklists(List<VoLendBlacklist> blacklists) {
        this.blacklists = blacklists;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
