package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.common.page.Page;
import lombok.Data;

/**
 * Created by admin on 2017/8/21.
 */
@Data
public class VoUnionLineNoReq extends Page {

    private Integer cityId;

    private Integer provinceId;

    private String keyword;
}
