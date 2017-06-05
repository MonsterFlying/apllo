package com.gofobao.framework.common.page;

import com.gofobao.framework.common.constans.CommonPageContants;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.ObjectUtils;

@ApiModel("分页")
public class Page extends VoBaseReq{

    @ApiModelProperty(name = "pageIndex", value = "页码（不传默认为1）", dataType = "int", required = false)
    protected Integer pageIndex;
    @ApiModelProperty(name = "pageSize", value = "数据条数（不传默认10）", dataType = "int", required = false)
    protected Integer pageSize;

    public Integer getPageIndex() {
        if (ObjectUtils.isEmpty(pageIndex) || pageIndex <= 1) {
            this.pageIndex = CommonPageContants.DEFAULT_PAGE_INDEX;
        }
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        if (ObjectUtils.isEmpty(pageIndex) || pageIndex <= 1) {
            this.pageIndex = CommonPageContants.DEFAULT_PAGE_INDEX;
        } else {
            this.pageIndex = pageIndex;
        }
    }

    public Integer getPageSize() {
        if (ObjectUtils.isEmpty(pageSize) || pageSize <= 0) {
            this.pageSize = CommonPageContants.DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (ObjectUtils.isEmpty(pageSize) || pageSize <= 0) {
            this.pageSize = CommonPageContants.DEFAULT_PAGE_SIZE;
        } else {
            this.pageSize = pageSize;
        }
    }
}
