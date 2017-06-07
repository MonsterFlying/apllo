package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Data
@ApiModel("邀请的好友投资信息")
public class VoViewFriendsTenderInfoWarpRes extends VoBaseResp {
    private List<FriendsTenderInfo> frindsTenderInfo= Lists.newArrayList();


}
