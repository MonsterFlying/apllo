package com.gofobao.framework.asset.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/19.
 */
@Data
public class VoAreaWarpRes extends VoBaseResp {
   private List<AreaRes> areaRes = Lists.newArrayList();

}