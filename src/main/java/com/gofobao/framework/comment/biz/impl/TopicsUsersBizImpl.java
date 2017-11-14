package com.gofobao.framework.comment.biz.impl;

import com.gofobao.framework.comment.biz.TopicsUsersBiz;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.response.VoTopicMemberCenterResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TopicsUsersBizImpl implements TopicsUsersBiz {

    @Autowired
    TopicsUsersService topicsUsersService;

    @Value("${qiniu.domain}")
    private String qiniuDomain;

    @Override
    public ResponseEntity<VoTopicMemberCenterResp> memberCenter(Long userId) {
        TopicsUsers topicsUsers = topicsUsersService.findByUserId(userId);
        VoTopicMemberCenterResp response = VoBaseResp.ok("成功", VoTopicMemberCenterResp.class);
        response.setUsername(topicsUsers.getUsername());
        if (!StringUtils.isEmpty(topicsUsers.getAvatar())) {
            response.setAvatar(String.format("%s/%s", qiniuDomain, topicsUsers.getAvatar()));
        }

        // 获取用户帖子动态


        // 获取获取用户的最新回复
        return null;
    }
}
