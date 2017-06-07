package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.service.InviteFriendsService;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class BrokerBounsBizImpl implements BrokerBounsBiz {
    @Autowired
    private InviteFriendsService inviteFriendsService;

    @Override
    public ResponseEntity<VoViewInviteFriendersWarpRes> list(VoFriendsReq voFriendsReq) {
        try {
            List<InviteFriends> inviteFriendsList = inviteFriendsService.list(voFriendsReq);
            VoViewInviteFriendersWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewInviteFriendersWarpRes.class);
            warpRes.setFriendsList(inviteFriendsList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.ok(VoBaseResp.ok("查询失败", VoViewInviteFriendersWarpRes.class));
        }
    }

    /**
     * 邀请统计
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<VoViewInviteAwardStatisticsWarpRes> statistic(Long userId) {
        try {
            InviteAwardStatistics statistics = inviteFriendsService.query(userId);
            VoViewInviteAwardStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewInviteAwardStatisticsWarpRes.class);
            warpRes.setInviteAwardStatistics(statistics);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.ok(VoBaseResp.ok("查询失败", VoViewInviteAwardStatisticsWarpRes.class));
        }
    }

    /**
     *
     * @param voFriendsReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewFriendsTenderInfoWarpRes> firstTender(VoFriendsReq voFriendsReq) {
        try {
            List<FriendsTenderInfo> tenderInfoList = inviteFriendsService.inviteUserFirstTender(voFriendsReq);
            VoViewFriendsTenderInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewFriendsTenderInfoWarpRes.class);
            warpRes.setFrindsTenderInfo(tenderInfoList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.ok(VoBaseResp.ok("查询失败", VoViewFriendsTenderInfoWarpRes.class));
        }
    }
}
