package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.request.VoFriendsTenderReq;
import com.gofobao.framework.member.vo.response.VoViewFriendsTenderInfoWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteAwardStatisticsWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteFriendersWarpRes;
import com.gofobao.framework.member.vo.response.pc.VoViewBrokerBounsWarpRes;
import com.gofobao.framework.member.vo.response.pc.VoViewInviteFriendsWarpRes;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Created by admin on 2017/6/7.
 */
public interface BrokerBounsBiz {

    ResponseEntity<VoViewInviteFriendersWarpRes> list(VoFriendsReq voFriendsReq);

    /**
     * pc 邀请好友投资记录
     *
     * @param voFriendsReq
     * @return
     */
    ResponseEntity<VoViewInviteFriendsWarpRes> pcFriendsTender(VoFriendsReq voFriendsReq);


    /**
     * 邀请好友奖励记录
     *
     * @param voFriendsTenderReq
     * @return
     */
    ResponseEntity<VoViewBrokerBounsWarpRes> pcBrokerBounsList(VoFriendsTenderReq voFriendsTenderReq);


    ResponseEntity<VoViewInviteAwardStatisticsWarpRes> statistic(Long userId);


    ResponseEntity<VoViewFriendsTenderInfoWarpRes> firstTender(VoFriendsReq voFriendsReq);


    Map<String, Object> shareRegister(Long userId);


    /**
     * pc 导出excel
     *
     * @param friendsTenderReq
     * @param response
     */
    void toExcel(VoFriendsTenderReq friendsTenderReq, HttpServletResponse response);

    /**
     * 天提成
     */
    void dayPushMoney(Date date);


    /**
     * 提成
     * @param date
     */
    void pushMoney(Date date);


    /**
     * 月提成
     * @param date
     */
    void monthPushMoney(Date date);


}
