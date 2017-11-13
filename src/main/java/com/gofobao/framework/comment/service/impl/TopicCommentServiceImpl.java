package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicTypeRepository;
import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.comment.vo.response.VoTopicCommentItem;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Service
public class TopicCommentServiceImpl implements TopicCommentService {
    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicTypeRepository topicTypeRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    @SuppressWarnings("all")
    public ResponseEntity<VoTopicCommentListResp> listDetail(long topicId, Pageable pageable) {
        List<TopicComment> topicComments = topicCommentRepository.findByTopicIdOrderByIdAsc(topicId, pageable);
        VoTopicCommentListResp voTopicCommentListResp = VoBaseResp.ok("查询评论成功", VoTopicCommentListResp.class);
        for (TopicComment topicComment : topicComments) {
            VoTopicCommentItem voTopicCommentItem = new VoTopicCommentItem();
            voTopicCommentItem.setContent(topicComment.getContent());
            voTopicCommentItem.setUserName(topicComment.getUserName());
             // image.......img
            voTopicCommentItem.setUserIconUrl(topicComment.getUserIconUrl());
            //评论时间分析
            long nowTime = Calendar.getInstance().getTimeInMillis();
            long publishTime = topicComment.getCreateDate().getTime();
            long between = nowTime - publishTime;
            if (between > DateHelper.MILLIS_PER_DAY * 7) {
                voTopicCommentItem.setTime("1周前");
            } else if (between >= DateHelper.MILLIS_PER_DAY) {
                voTopicCommentItem.setTime(between / DateHelper.MILLIS_PER_DAY + "天前");
            } else if (between >= DateHelper.MILLIS_PER_HOUR) {
                voTopicCommentItem.setTime(between / DateHelper.MILLIS_PER_HOUR + "小时前");
            } else if (between >= DateHelper.MILLIS_PER_MINUTE) {
                voTopicCommentItem.setTime(between / DateHelper.MILLIS_PER_MINUTE + "分钟前");
            }
            voTopicCommentListResp.getVoTopicCommentItemList().add(voTopicCommentItem);
        }
        return ResponseEntity.ok(voTopicCommentListResp);
    }

    @Override
    public ResponseEntity<VoBaseResp> publishComment(VoTopicCommentReq voTopicCommentReq, Long userId) {
        // 判断板块id存在否？
        TopicType topicType = topicTypeRepository.findById(voTopicCommentReq.getTopicTypeId());
        Preconditions.checkNotNull(topicType, "topicType is not exist");

        //判断话题id是否存在?
        Topic topic = topicRepository.findById(voTopicCommentReq.getTopicId());
        Preconditions.checkNotNull(topic, "topic is not exist");

        Users user = usersRepository.findById(userId);
        Preconditions.checkNotNull(user, "user record is empty");
        Date nowDate = new Date();
        Users users = usersRepository.findById(userId);
        TopicComment topicComment = new TopicComment();
        topicComment.setTopicId(voTopicCommentReq.getTopicId());
        topicComment.setUserId(userId);
        topicComment.setUserName(users.getUsername());
        topicComment.setCreateDate(nowDate);
        topicComment.setUpdateDate(nowDate);
        topicComment.setTopicTypeId(voTopicCommentReq.getTopicTypeId());
        // 用户内容铭感词过滤

        FilteredResult filteredResult = WordFilterUtil.filterText(voTopicCommentReq.getContent(), '*');
        topicComment.setContent(filteredResult.getFilteredContent());
        TopicComment commentResult = topicCommentRepository.save(topicComment);
        Preconditions.checkNotNull(commentResult,"comment is fail");
        return ResponseEntity.ok(VoBaseResp.ok("发布成功", VoBaseResp.class));
    }
}
