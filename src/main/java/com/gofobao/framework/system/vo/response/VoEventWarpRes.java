package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/21.
 */
@Data
public class VoEventWarpRes extends VoBaseResp{

    private  List<Event> events = Lists.newArrayList();
}
