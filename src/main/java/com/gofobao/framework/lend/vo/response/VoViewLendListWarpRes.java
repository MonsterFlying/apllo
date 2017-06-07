package com.gofobao.framework.lend.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewLendListWarpRes extends VoBaseResp {
    private List<VoViewLend> voViewLends= Lists.newArrayList();
}
