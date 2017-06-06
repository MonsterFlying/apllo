package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewSettleWarpListRes  extends VoBaseResp {
     List<VoViewSettleRes> voViewSettleRes= Collections.EMPTY_LIST;
}
