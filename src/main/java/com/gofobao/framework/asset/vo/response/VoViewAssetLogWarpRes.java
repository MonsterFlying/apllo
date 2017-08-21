package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewAssetLogWarpRes extends VoBaseResp {
    private List<VoViewAssetLogRes>  resList= new ArrayList<>() ;


    private Long totalCount=0L;
}
