package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.comment.vo.response.VoTopicReplyListResp;
import com.gofobao.framework.comment.vo.response.VoTopicReplyResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.google.common.base.Preconditions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
    private TopicsUsersRepository topicsUsersRepository;

    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicsNoticesBiz topicsNoticesBiz ;

    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        Date nowDate = new Date();
        //判断用户是否能发言
        TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁止发言", VoBaseResp.class));
        }
        // 评论ID
        TopicComment topicComment = topicCommentRepository.findOne(voTopicReplyReq.getTopicCommentId());
        Preconditions.checkNotNull(topicComment, "topicComment record is empty");

        // 回复ID
        //判断是回复评论还是回复评论的回复
        TopicReply parentTopicReply = null;
        if (voTopicReplyReq.getTopicReplyId() != 0) {
            parentTopicReply = topicReplyRepository.findOne(voTopicReplyReq.getTopicReplyId());
        }

        //用户不能自己对自己进行回复


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
        FilteredResult filterResult = WordFilterUtil.filterText(voTopicReplyReq.getContent(), '*');
        reply.setContent(filterResult.getFilteredContent());

        reply = topicReplyRepository.save(reply);
        Preconditions.checkNotNull(reply, "reply is fail");

        // 回复成功修改回复总数
        topicComment.setContentTotalNum(topicComment.getTopTotalNum() + 1);
        topicComment.setUpdateDate(nowDate);

        topicCommentRepository.save(topicComment) ;

        topicsNoticesBiz.noticesByReplay(reply) ;
        return ResponseEntity.ok(VoBaseResp.ok("回复成功", VoBaseResp.class));
    }

    @Override
    public TopicReply findById(Long id) {
        return topicReplyRepository.findOne(id);
    }

    @Override
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(avatar)) {
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
    public ResponseEntity<VoTopicReplyListResp> listReply(Long topicCommentId) {
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
