package com.gofobao.framework.tender.vo.response.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class VoViewPcAutoTenderWarpRes extends VoBaseResp {

    List<PcAutoTender>tenderList= Lists.newArrayList();
}
