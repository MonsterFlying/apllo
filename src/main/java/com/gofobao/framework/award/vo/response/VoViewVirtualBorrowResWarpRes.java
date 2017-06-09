package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/9.
 */
@Data
public class VoViewVirtualBorrowResWarpRes extends VoBaseResp {
    private List<VirtualBorrowRes> resList = Lists.newArrayList();
}
