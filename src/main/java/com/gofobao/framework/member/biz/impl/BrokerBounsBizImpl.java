package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.InviteFriendsService;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.request.VoFriendsTenderReq;
import com.gofobao.framework.member.vo.response.*;
import com.gofobao.framework.member.vo.response.pc.PcInviteFriends;
import com.gofobao.framework.member.vo.response.pc.VoViewBrokerBounsWarpRes;
import com.gofobao.framework.member.vo.response.pc.VoViewInviteFriendsWarpRes;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Override
    public ResponseEntity<VoViewInviteFriendersWarpRes> list(VoFriendsReq voFriendsReq) {
        try {
            List<InviteFriends> inviteFriendsList = inviteFriendsService.list(voFriendsReq);
            VoViewInviteFriendersWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewInviteFriendersWarpRes.class);
            warpRes.setFriendsList(inviteFriendsList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.ok(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewInviteFriendersWarpRes.class));
        }
    }

    /**
     * pc:邀请好友
     *
     * @param voFriendsReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewInviteFriendsWarpRes> pcFriendsTender(VoFriendsReq voFriendsReq) {
        try {
            Map<String, Object> resultMaps = inviteFriendsService.pcInviteUserFirstTender(voFriendsReq);
            Integer totalCount = Integer.parseInt(resultMaps.get("totalCount").toString());
            List<PcInviteFriends> friendsList = (List<PcInviteFriends>) resultMaps.get("userList");
            VoViewInviteFriendsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewInviteFriendsWarpRes.class);
            warpRes.setTotalCount(totalCount);
            warpRes.setFriendsList(friendsList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询异常", VoViewInviteFriendsWarpRes.class));
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
        } catch (Throwable e) {
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
        } catch (Throwable e) {
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
        String inviteCode = user.getInviteCode();
        paramMaps.put("inviteUrl", webDomain + "/#/auth/register?inviteCode=" + inviteCode);
        paramMaps.put("inviteCode", inviteCode);
        paramMaps.put("invitePhone", user.getPhone());
        paramMaps.put("QRCodeURL", webDomain + "/invite/qrcode/getInviteFriendQRCode?inviteCode=" + inviteCode);
        paramMaps.put("requestSource", 3);
        return paramMaps;
    }

    /**
     * PC
     *
     * @param voFriendsTenderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewBrokerBounsWarpRes> pcBrokerBounsList(VoFriendsTenderReq voFriendsTenderReq) {
        try {
            VoViewBrokerBounsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewBrokerBounsWarpRes.class);
            Map<String, Object> resultMaps = inviteFriendsService.pcBrokerBounsList(voFriendsTenderReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<InviteFriends> friendsList = (List<InviteFriends>) resultMaps.get("bounsList");
            warpRes.setTotalCount(totalCount);
            warpRes.setFriendsList(friendsList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询异常", VoViewBrokerBounsWarpRes.class));
        }
    }

    @Override
    public void toExcel(VoFriendsTenderReq friendsTenderReq, HttpServletResponse response) {
        List<InviteFriends> inviteFriends  = inviteFriendsService.toExcel(friendsTenderReq);
        if (!CollectionUtils.isEmpty(inviteFriends)) {
            LinkedHashMap<String, String> paramMaps = Maps.newLinkedHashMap();
            paramMaps.put("createdAt", "时间");
            paramMaps.put("leave", "等级");
            paramMaps.put("scale", "奖励年利率");
            paramMaps.put("money", "提成奖励");
            paramMaps.put("waitPrincipalTotal", "计算提成的总待收本金");
            try {
                ExcelUtil.listToExcel(inviteFriends, paramMaps, "邀请好友", response);
            } catch (ExcelException e) {
                e.printStackTrace();
            }
        }

    }
}
