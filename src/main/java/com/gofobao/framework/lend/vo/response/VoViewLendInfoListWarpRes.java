package com.gofobao.framework.lend.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
@Data
public class VoViewLendInfoListWarpRes extends VoBaseResp {

    private List<LendInfoList> listList= Lists.newArrayList();
}
