package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
@ApiModel
public class VoViewBorrowListWarpRes extends VoBaseResp {
    private List<VoViewBorrowList>  voViewBorrowLists= Lists.newArrayList();
}
