package com.gofobao.framework.asset.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/28.
 */
@Data
@ApiModel
public class VoViewAssetLogsWarpRes extends VoBaseResp{
    private List<AssetLogs> assetLogs= Lists.newArrayList();
    private Integer totalCount;
}
