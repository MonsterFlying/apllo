package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
@Data
public class VoViewTransferMayWarpRes extends VoBaseResp {
    private List<TransferMay> mayList= Lists.newArrayList();
}
