package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.VoViewFriendsTenderInfoWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteAwardStatisticsWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteFriendersWarpRes;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Created by admin on 2017/6/7.
 */
public interface BrokerBounsBiz  {

    ResponseEntity<VoViewInviteFriendersWarpRes> list(VoFriendsReq voFriendsReq);


    ResponseEntity<VoViewInviteAwardStatisticsWarpRes> statistic(Long userId);


    ResponseEntity<VoViewFriendsTenderInfoWarpRes> firstTender(VoFriendsReq voFriendsReq);


    ResponseEntity<Map<String,String>>shareRegister(Long userId);

}
