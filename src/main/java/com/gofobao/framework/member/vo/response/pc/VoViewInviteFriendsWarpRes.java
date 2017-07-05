package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class VoViewInviteFriendsWarpRes extends VoBaseResp {

    private List<PcInviteFriends> friendsList = Lists.newArrayList();


    private Integer totalCount = 0;

}
