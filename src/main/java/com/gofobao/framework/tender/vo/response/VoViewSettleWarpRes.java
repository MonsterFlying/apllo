package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewSettleWarpRes extends VoBaseResp{
    private List<VoViewSettleRes> voViewSettleRes= Collections.EMPTY_LIST;
}
