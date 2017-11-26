package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.comment.vo.response.VoTopicReplyListResp;
import com.gofobao.framework.comment.vo.response.VoTopicReplyResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Created by xin on 2017/11/13.
 */
@Service
public class TopicReplyServiceImpl implements TopicReplyService {


    @Autowired
    TopicReplyRepository topicReplyRepository;

    @Autowired
    private TopicsUsersService topicsUsersService;

    @Autowired
    private TopicsUsersRepository topicsUsersRepository;

    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicsNoticesBiz topicsNoticesBiz;

    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        Date nowDate = new Date();
        //判断用户是否能发言
        TopicsUsers topicsUsers = null;

        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁止发言", VoBaseResp.class));
        }
        //判断评论是否已被删除
        TopicComment topicComment = topicCommentRepository.findOne(voTopicReplyReq.getTopicCommentId());
        if (voTopicReplyReq.getTopicReplyId().equals(0) && topicComment.getDel().equals(1)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "回复失败,评论已被删除!", VoBaseResp.class));
        }
        //判断回复id是否存在
        if (voTopicReplyReq.getTopicReplyId() != 0) {
            TopicReply topicReply = topicReplyRepository.findOne(voTopicReplyReq.getTopicReplyId());
            if (ObjectUtils.isEmpty(topicReply) || topicReply.getDel().equals(1)) {
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "回复失败,回复已被删除!", VoBaseResp.class));
            }
        }

        //判断用户上次回复时间,设置回复时间间隔为1分钟
        TopicReply lastReply = topicReplyRepository.findTopByUserIdOrderByIdDesc(userId);
        if (!ObjectUtils.isEmpty(lastReply) && (nowDate.getTime() -
                lastReply.getCreateDate().getTime()) < DateHelper.MILLIS_PER_MINUTE) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "回复过于频繁,请1分钟后再试"));
        }


        // 回复ID

        // 回复的回复
        TopicReply parentTopicReply = null;
        if (voTopicReplyReq.getTopicReplyId() != 0) {
            parentTopicReply = topicReplyRepository.findOne(voTopicReplyReq.getTopicReplyId());
            Preconditions.checkNotNull(parentTopicReply, "parentTopicReply record is empty");
            if (parentTopicReply.getUserId().equals(userId)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "不能对自己回复", VoBaseResp.class));
            }
        }else{
            // 评论的回复
            if (topicComment.getUserId().equals(userId)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "不能对自己回复", VoBaseResp.class));
            }
        }


        TopicReply reply = new TopicReply();
        reply.setTopicId(topicComment.getTopicId());
        reply.setTopicCommentId(topicComment.getId());
        reply.setTopicTypeId(topicComment.getTopicTypeId());
        reply.setUserName(topicsUsers.getUsername());
        reply.setUserIconUrl(topicsUsers.getAvatar());
        reply.setUserId(topicsUsers.getUserId());
        reply.setUpdateDate(nowDate);
        reply.setCreateDate(nowDate);
        if (!ObjectUtils.isEmpty(parentTopicReply)) {
            // 回复
            reply.setReplyType(1);
            reply.setTopicReplyId(parentTopicReply.getId());
            reply.setForUserId(parentTopicReply.getUserId());
            reply.setForUserIconUrl(parentTopicReply.getUserIconUrl());
            reply.setForUserName(parentTopicReply.getUserName());
        } else {
            // 评论
            reply.setReplyType(0);
            reply.setForUserId(topicComment.getUserId());
            reply.setForUserIconUrl(topicComment.getUserIconUrl());
            reply.setForUserName(topicComment.getUserName());
        }

        //回复内容敏感词过滤
        FilteredResult filterResult = WordFilterUtil.filterText(voTopicReplyReq.getContent().trim(), '*');
        reply.setContent(filterResult.getFilteredContent());

        reply = topicReplyRepository.save(reply);
        Preconditions.checkNotNull(reply, "reply is fail");

        // 回复成功修改回复总数
        int contentTotalNum = topicComment.getContentTotalNum() + 1;
        topicComment.setContentTotalNum(contentTotalNum);
        topicComment.setUpdateDate(nowDate);

        topicCommentRepository.save(topicComment);

        topicsNoticesBiz.noticesByReplay(reply);
        return ResponseEntity.ok(VoBaseResp.ok("回复成功", VoBaseResp.class));
    }

    @Override
    public TopicReply findById(Long id) {
        return topicReplyRepository.findOne(id);
    }

    @Override
    @Transactional
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(avatar)) {
            throw new Exception("参数错误!");
        }
        if (!StringUtils.isEmpty(username)) {
            topicReplyRepository.batchUpateUsernameByUserId(userId, username);
            topicReplyRepository.batchUpateUsernameByForUserId(userId, username);
        } else {
            topicReplyRepository.batchUpateAvatarByUserId(userId, avatar);
            topicReplyRepository.batchUpateAvatarByForUserId(userId, avatar);
        }
    }

    @Override
    public TopicReply save(TopicReply topicReply) {
        return topicReplyRepository.save(topicReply);
    }

    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> deleteReply(@NotNull Long topicReplyId, Long userId) {
        //判断是否是回复的用户
        TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        if (!topicsUsers.getUserId().equals(userId)) {
            ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class));
        }
        Integer updateReplyCount = topicReplyRepository.updateOneReply(topicReplyId);
        if (ObjectUtils.isEmpty(updateReplyCount)) {
            ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除回复失败", VoBaseResp.class));
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除成功", VoBaseResp.class));
    }


    @Override
    public ResponseEntity<VoTopicReplyListResp> listReply(@NotNull Long topicCommentId) {
        List<TopicReply> topicReplies = topicReplyRepository.findByTopicCommentId(topicCommentId);
        VoTopicReplyListResp voTopicReplyListResp = VoBaseResp.ok("查询成功", VoTopicReplyListResp.class);
        for (TopicReply topicReply : topicReplies) {
            VoTopicReplyResp voTopicReplyResp = new VoTopicReplyResp();
            voTopicReplyResp.setUserName(topicReply.getUserName());
            voTopicReplyResp.setContent(topicReply.getContent());
            voTopicReplyResp.setTopTotalNum(topicReply.getTopTotalNum());
            voTopicReplyListResp.getVoTopicReplyResps().add(voTopicReplyResp);
        }
        return ResponseEntity.ok(voTopicReplyListResp);
    }

}
