package com.gofobao.framework.lend.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class VoViewLendWarpRes extends VoBaseResp {

    private List<VoViewLend> lends= Lists.newArrayList();

    private Integer totalCount=0;


}
