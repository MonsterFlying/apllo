package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentItem;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Service
@Slf4j
public class TopicCommentServiceImpl implements TopicCommentService {
    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicRepository topicRepository;


    @Autowired
    private TopicsNoticesBiz topicsNoticesBiz;

    @Autowired
    private TopicsUsersRepository topicsUsersRepository;

    @Autowired
    private TopicsUsersService topicsUsersService;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    @Override
    public ResponseEntity<VoTopicCommentListResp> listDetail(long topicId, Pageable pageable) {
        List<TopicComment> topicComments = topicCommentRepository.findByTopicIdAndDelOrderByIdAsc(topicId, 0, pageable);
        VoTopicCommentListResp voTopicCommentListResp = VoBaseResp.ok("查询评论成功", VoTopicCommentListResp.class);
        for (TopicComment topicComment : topicComments) {
            VoTopicCommentItem voTopicCommentItem = new VoTopicCommentItem();
            voTopicCommentItem.setContent(topicComment.getContent());
            voTopicCommentItem.setUserName(topicComment.getUserName());
            // image.......img
            voTopicCommentItem.setUserIconUrl(imgPrefix + "/" + topicComment.getUserIconUrl());
            //评论时间分析
            long publishTime = topicComment.getCreateDate().getTime();
            voTopicCommentItem.setTime(DateHelper.getPastTime(publishTime));
            voTopicCommentListResp.getVoTopicCommentItemList().add(voTopicCommentItem);
        }
        return ResponseEntity.ok(voTopicCommentListResp);
    }

    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> publishComment(@NonNull VoTopicCommentReq voTopicCommentReq,
                                                     @NonNull Long userId) {
        //判断用户
        TopicsUsers topicsUsers = topicsUsersService.findByUserId(userId);
        //TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁言", VoBaseResp.class));
        }

        //判断话题id是否存在?
        Topic topic = topicRepository.findByIdAndDel(voTopicCommentReq.getTopicId(), 0);
        Preconditions.checkNotNull(topic, "topic is not exist");

        Date nowDate = new Date();
        TopicComment topicComment = new TopicComment();
        topicComment.setTopicId(voTopicCommentReq.getTopicId());
        topicComment.setUserId(userId);
        topicComment.setUserName(topicsUsers.getUsername());
        topicComment.setUserIconUrl(topicsUsers.getAvatar());
        topicComment.setCreateDate(nowDate);
        topicComment.setUpdateDate(nowDate);
        topicComment.setTopicTypeId(topic.getTopicTypeId());
        // 用户内容铭感词过滤
        FilteredResult filteredResult = WordFilterUtil.filterText(voTopicCommentReq.getContent(), '*');
        topicComment.setContent(filteredResult.getFilteredContent());
        TopicComment commentResult = topicCommentRepository.save(topicComment);
        Preconditions.checkNotNull(commentResult, "comment is fail");
        //发布成功修改评论总数
        topicRepository.updateToTalComment(topicComment.getTopicId());
        topicsNoticesBiz.noticesByComment(topicComment);
        return ResponseEntity.ok(VoBaseResp.ok("发布成功", VoBaseResp.class));
    }

    @Override
    public TopicComment findById(Long id) {
        return topicCommentRepository.findOne(id);
    }

    @Override
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(avatar)) {
            throw new Exception("参数错误!");
        }
        if (!StringUtils.isEmpty(username)) {
            topicCommentRepository.batchUpateUsernameByUserId(userId, username);
        } else {
            topicCommentRepository.batchUpateAvatarByUserId(userId, avatar);
        }
    }

    @Override
    public TopicComment save(TopicComment topicComment) {
        return topicCommentRepository.save(topicComment);
    }
}
