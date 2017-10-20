package com.gofobao.framework.member.service;

import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.request.VoFriendsTenderReq;
import com.gofobao.framework.member.vo.response.FriendsTenderInfo;
import com.gofobao.framework.member.vo.response.InviteAwardStatistics;
import com.gofobao.framework.member.vo.response.InviteFriends;

import java.util.List;
import java.util.Map;

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


    Map<String, Object> pcBrokerBounsList(VoFriendsTenderReq friendsTenderReq);

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


    /**
     * 员工邀请用户
     * @param voFriendsReq
     * @return
     */
    List<FriendsTenderInfo> employeeInviteUserFirstTender(VoFriendsReq voFriendsReq);

    /**
     * pc:邀请用户
     * @param voFriendsReq
     * @return
     */
    Map<String, Object> pcInviteUserFirstTender(VoFriendsReq voFriendsReq);

    /**
     * PC 邀请用户导出excel
     * @param friendsTenderReq
     * @return
     */
    List<InviteFriends>toExcel(VoFriendsTenderReq friendsTenderReq);


}
