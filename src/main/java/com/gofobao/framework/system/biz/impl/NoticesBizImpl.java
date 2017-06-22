package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.NoticesService;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.VoViewNoticesInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * Created by Max on 17/6/5.
 */
@Component
@Slf4j
public class NoticesBizImpl implements NoticesBiz {

    @Autowired
    NoticesService noticesService;

    @Autowired
    UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Notices notices) {
        Preconditions.checkNotNull(notices, "NoticesBizImpl.save: notices is empty");
        Preconditions.checkNotNull(notices.getUserId(), "NoticesBizImpl.save: userId is empty");

        Users users = userService.findByIdLock(notices.getUserId());
        if (ObjectUtils.isEmpty(users)) {
            log.error("NoticesBizImpl.save: userId find null");
            return false;
        }

        Date now = new Date();
        if (ObjectUtils.isEmpty(notices.getCreatedAt())) {
            notices.setCreatedAt(now);
        }

        if (ObjectUtils.isEmpty(notices.getUpdatedAt())) {
            notices.setUpdatedAt(now);
        }

        noticesService.save(notices);
        Integer noticeCount = users.getNoticeCount();
        noticeCount = noticeCount <= 0 ? 0 : noticeCount;
        users.setNoticeCount(noticeCount + 1);
        users.setUpdatedAt(now);
        userService.save(users);
        return true;
    }

    @Override
    public ResponseEntity<VoViewUserNoticesWarpRes> list(VoNoticesReq voNoticesReq) {
        try {
            VoViewUserNoticesWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewUserNoticesWarpRes.class);
            warpRes.setNotices(noticesService.list(voNoticesReq));
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                        VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewUserNoticesWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewNoticesInfoWarpRes> info(VoNoticesReq voNoticesReq) {

        try {
            VoViewNoticesInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewNoticesInfoWarpRes.class);
            warpRes.setNoticesInfo(noticesService.info(voNoticesReq));
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            VoBaseResp.error(
                                    VoBaseResp.ERROR,
                                    "查询失败",
                                    VoViewNoticesInfoWarpRes.class));
        }

    }
}
