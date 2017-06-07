package com.gofobao.framework.member.service;

import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.FriendsTenderInfo;
import com.gofobao.framework.member.vo.response.InviteAwardStatistics;
import com.gofobao.framework.member.vo.response.InviteFriends;

import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
public interface InviteFriendsService {
    /**
     * 邀请好友列表
     * @param voFriendsReq
     * @return
     */
    List<InviteFriends> list(VoFriendsReq voFriendsReq);

    /**
     * 邀请奖励统计
     * @param userId
     * @return
     */
    InviteAwardStatistics query(Long userId);


    /**
     *
     * @param voFriendsReq
     * @return
     */
    List<FriendsTenderInfo> inviteUserFirstTender(VoFriendsReq voFriendsReq);


}
