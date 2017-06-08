package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Data
public class VoViewRedPackageWarpRes extends VoBaseResp {

    List<RedPackageRes> resList= Lists.newArrayList();
}
