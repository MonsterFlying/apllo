package com.gofobao.framework.comment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.entity.*;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.service.TopicsNoticesService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Component
@Slf4j
public class TopicsNoticesBizImpl implements TopicsNoticesBiz {

    @Autowired
    TopicsUsersService topicsUsersService;

    @Autowired
    TopicsNoticesService topicsNoticesService;

    @Autowired
    TopicService topicService;

    @Override
    public Long count(@NonNull Long userId) {
        Specification<TopicsNotices> topicsReplyNoticesSpecification = Specifications
                .<TopicsNotices>and()
                .eq("userId", userId)
                .eq("del", 0)
                .build();

        return topicsNoticesService.count(topicsReplyNoticesSpecification);
    }

    @Override
    public Long count(@NonNull Long userId, @NonNull Integer sourceType, boolean veiwState) {
        Specification<TopicsNotices> topicsReplyNoticesSpecification = Specifications
                .<TopicsNotices>and()
                .eq("userId", userId)
                .eq("sourceType", sourceType)
                .eq("viewState", veiwState ? 1 : 0)
                .build();

        return topicsNoticesService.count(topicsReplyNoticesSpecification);
    }

    @Override
    public boolean noticesByComment(@NonNull TopicComment topicComment) {
        try {
            Preconditions.checkNotNull(topicComment.getId(), "topicComment id is empty");
            Long forUserId = topicComment.getUserId();
            TopicsUsers forTopicsUsers = topicsUsersService.findByUserId(forUserId);
            Preconditions.checkNotNull(forTopicsUsers, "find forTopicsUsers record is empty");
            Long topicId = topicComment.getTopicId();
            Topic topic = topicService.findById(topicId);
            Preconditions.checkNotNull(topic, "find topic record is empty");
            Date nowDate = new Date();
            TopicsNotices notices = new TopicsNotices();
            notices.setContent(topicComment.getContent());
            notices.setCreateDate(nowDate);
            notices.setForUserIconUrl(forTopicsUsers.getAvatar());
            notices.setForUserName(forTopicsUsers.getUsername());
            notices.setForUserId(forTopicsUsers.getUserId());
            notices.setSourceId(topicComment.getId());
            notices.setSourceType(0);
            notices.setUpdateDate(nowDate);
            notices.setUserId(topic.getUserId());
            notices.setViewState(0);
            topicsNoticesService.save(notices);
            return true;
        } catch (Exception ex) {
            log.error("保存用户通知失败", ex);
            return false;
        }
    }

    @Override
    public boolean noticesByReplay(@NotNull TopicReply topicReply) {
        try {
            Preconditions.checkNotNull(topicReply.getId(), "ropicReply id is empty");
            Date nowDate = new Date();
            TopicsNotices notices = new TopicsNotices();
            notices.setContent(topicReply.getContent());
            notices.setCreateDate(nowDate);
            notices.setForUserIconUrl(topicReply.getUserIconUrl());
            notices.setForUserName(topicReply.getUserName());
            notices.setForUserId(topicReply.getUserId());
            notices.setSourceId(topicReply.getId());
            notices.setSourceType(1);
            notices.setUpdateDate(nowDate);
            notices.setUserId(topicReply.getForUserId());
            notices.setViewState(0);
            topicsNoticesService.save(notices);
            return true;
        } catch (Exception ex) {
            log.error("保存用户通知失败", ex);
            return false;
        }
    }
}
