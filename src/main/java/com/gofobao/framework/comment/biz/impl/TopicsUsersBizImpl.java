package com.gofobao.framework.comment.biz.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.biz.TopicsUsersBiz;
import com.gofobao.framework.comment.entity.TopicReport;
import com.gofobao.framework.comment.entity.TopicsNotices;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.service.*;
import com.gofobao.framework.comment.vo.request.VoUpdateUsernameReq;
import com.gofobao.framework.comment.vo.response.VoAvatarResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TopicsUsersBizImpl implements TopicsUsersBiz {

    @Autowired
    TopicsUsersService topicsUsersService;

    @Autowired
    TopicsNoticesBiz topicsNoticesBiz;

    @Value("${qiniu.domain}")
    String qiniuDomain;

    @Autowired
    FileManagerBiz fileManagerBiz;

    @Autowired
    UserService userService;

    @Autowired
    TopicService topicService;

    @Autowired
    TopicCommentService topicCommentService;

    @Autowired
    TopicReplyService topicReplyService;

    @Autowired
    TopicReportService topicReportService;

    @Autowired
    TopicsNoticesService topicsNoticesService ;

    @Override
    public ResponseEntity<VoTopicMemberCenterResp> memberCenter(@NonNull Long userId) {
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoTopicMemberCenterResp.class)) ;
        }
        VoTopicMemberCenterResp response = VoBaseResp.ok("成功", VoTopicMemberCenterResp.class);
        response.setUsername(topicsUsers.getUsername());
        if (!StringUtils.isEmpty(topicsUsers.getAvatar())) {
            response.setAvatar(String.format("%s/%s", qiniuDomain, topicsUsers.getAvatar()));
        }

        Integer sourceType = 0;
        boolean veiwState = false;
        Long commentCount = topicsNoticesBiz.count(userId, sourceType, veiwState);
        response.setTopicNumber(commentCount);

        sourceType = 1;
        Long replyCount = topicsNoticesBiz.count(userId, sourceType, veiwState);
        response.setTopicReplyNumber(replyCount);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoAvatarResp> avatar(HttpServletRequest httpServletRequest, @NonNull Long userId) {
        List<String> strings;
        try {
            strings = fileManagerBiz.multiUpload(userId, httpServletRequest, "file");
        } catch (Exception e) {
            log.error("avatar save exception", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常", VoAvatarResp.class));
        }

        if (CollectionUtils.isEmpty(strings)
                || strings.size() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常", VoAvatarResp.class));
        }

        Date nowDate = new Date();
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoAvatarResp.class)) ;
        }
        Preconditions.checkNotNull(topicsUsers, "topicsUsers record is empty");
        topicsUsers.setAvatar(strings.get(0));
        topicsUsers.setUpdateDate(nowDate);
        topicsUsersService.save(topicsUsers);

        // 冗余数据修改
        try {
            updateRedundancy(userId, null, strings.get(0));
        } catch (Exception e) {
            log.error("批量修改冗余头像失败", e);
        }

        VoAvatarResp voAvatarResp = VoBaseResp.ok("成功", VoAvatarResp.class);
        voAvatarResp.setImg(String.format("%s%s", qiniuDomain, strings.get(0)));
        return ResponseEntity.ok(voAvatarResp);
    }

    @Override
    public ResponseEntity<VoBaseResp> updateUsername(@NonNull VoUpdateUsernameReq voUpdateUsernameReq, Long userId) {
        // 用户铭感词过滤
        String username = voUpdateUsernameReq.getUsername();
        if (username.contains("gfb")
                || username.contains("gofobao")
                || username.contains("广富宝")
                || username.startsWith("a_z_")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "昵称含有铭感词汇"));
        }

        FilteredResult filteredResult = WordFilterUtil.filterText(voUpdateUsernameReq.getUsername(), '*');
        String filteredContent = filteredResult.getFilteredContent();
        if (filteredContent.contains("*")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "昵称含有铭感词汇"));
        }

        // 只允许修改一次
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoAvatarResp.class)) ;
        }
        Preconditions.checkNotNull(topicsUsers, "topicsUsers is empty");
        Users user = userService.findById(userId);
        Preconditions.checkNotNull(user, "user record is empty");
        if (!topicsUsers.getUsername().equals(user.getUsername())
                && !topicsUsers.getUsername().startsWith("a_z_")) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "用户只有一次修改用户名的权利!"));
        }
        Date nowDate = new Date();
        topicsUsers.setUsername(voUpdateUsernameReq.getUsername());
        topicsUsers.setUpdateDate(nowDate);

        // 修改冗余数据
        try {
            updateRedundancy(userId, voUpdateUsernameReq.getUsername(), null);
        } catch (Exception e) {
            log.error("批量修改冗余用户名失败", e);
        }

        return ResponseEntity.ok(VoBaseResp.ok("操作成功"));
    }

    /**
     * 综合修改冗余的用户名和用户头像
     */
    private void updateRedundancy(Long userId, String username, String avatar) throws Exception {
        // 更改帖子冗余数据
        topicService.batchUpdateRedundancy(userId, username, avatar);
        // 修改评论冗余数据
        topicCommentService.batchUpdateRedundancy(userId, username, avatar);
        // 修改回复冗余数据
        topicReplyService.batchUpdateRedundancy(userId, username, avatar);
        // 修改通知冗余数据
        topicsNoticesService.batchUpdateRedundancy(userId, username, avatar);
    }
}
