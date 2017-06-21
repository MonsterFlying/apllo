package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.InviteFriendsService;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class BrokerBounsBizImpl implements BrokerBounsBiz {
    @Autowired
    private InviteFriendsService inviteFriendsService;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.imageDomain}")
    private String imageDomain;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Override
    public ResponseEntity<VoViewInviteFriendersWarpRes> list(VoFriendsReq voFriendsReq) {
        try {
            List<InviteFriends> inviteFriendsList = inviteFriendsService.list(voFriendsReq);
            VoViewInviteFriendersWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewInviteFriendersWarpRes.class);
            warpRes.setFriendsList(inviteFriendsList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.ok(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewInviteFriendersWarpRes.class));
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
            return ResponseEntity.ok(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewInviteAwardStatisticsWarpRes.class));
        }
    }

    /**
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
            return ResponseEntity.ok(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewFriendsTenderInfoWarpRes.class));
        }
    }

    /**
     * 分享注册邀请码
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> shareRegister(Long userId) {
        Map<String, Object> paramMaps = new HashMap<>();
        Users user = usersRepository.findOne(userId);
        String inviteCode=user.getInviteCode();
        paramMaps.put("inviteUrl", imageDomain + "/#/auth/register?inviteCode=" + inviteCode);
        paramMaps.put("inviteCode", inviteCode);
        paramMaps.put("invitePhone", user.getPhone());
        paramMaps.put("QRCodeURL",   imageDomain+"/qrcode/getInviteFriendQRCode?inviteCode=" + inviteCode);
        paramMaps.put("requestSource", 3);

        //String content = thymeleafHelper.build("friends", paramMaps);
        return paramMaps;
    }
}
