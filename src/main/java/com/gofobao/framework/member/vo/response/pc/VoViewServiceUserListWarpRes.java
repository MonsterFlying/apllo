package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/19.
 */
@Data
public class VoViewServiceUserListWarpRes extends VoBaseResp{

    private List<ServiceUser> serviceUsers= Lists.newArrayList();

}
