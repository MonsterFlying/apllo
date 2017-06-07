package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewOrderDetailWarpRes extends VoBaseResp {
    private VoViewOrderDetailRes detailWarpRes =new VoViewOrderDetailRes();
}
