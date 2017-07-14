package com.gofobao.framework.lend.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/13.
 */
@Data
public class VoViewUserLendInfoWarpRes extends VoBaseResp{
    private List<UserLendInfo> lendInfos= Lists.newArrayList();

    private Integer totalCount=0;
}
