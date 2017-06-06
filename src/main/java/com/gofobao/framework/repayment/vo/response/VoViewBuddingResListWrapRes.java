package com.gofobao.framework.repayment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewBuddingResListWrapRes extends VoBaseResp {
    private List<VoViewBuddingRes> viewBuddingResList = Collections.EMPTY_LIST;
}
