package com.gofobao.framework.asset.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/21.
 */
@Data
public class UnionLineNoWarpRes extends VoBaseResp{

    private List<UnionLineNo> unionLineNos = Lists.newArrayList();

    private Long totalCount =0L;


}
