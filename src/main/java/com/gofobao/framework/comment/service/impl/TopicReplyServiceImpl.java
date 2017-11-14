package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.entity.TopicReply;
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

import java.util.List;

/**
 * Created by xin on 2017/11/13.
 */
@Service
public class TopicReplyServiceImpl implements TopicReplyService {


    @Autowired
    TopicReplyRepository topicReplyRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        Users users = usersRepository.findById(userId);
        Preconditions.checkNotNull(users, "user is not exist");
        TopicReply topicReply = new TopicReply();
        topicReply.setTopicId(voTopicReplyReq.getTopicId());
        topicReply.setTopicCommentId(voTopicReplyReq.getTopicCommentId());
        topicReply.setTopicTypeId(voTopicReplyReq.getTopicTypeId());
        topicReply.setUserName(users.getUsername());
        //回复内容敏感词过滤
        FilteredResult filterResult = WordFilterUtil.filterText(voTopicReplyReq.getContent(), '*');
        topicReply.setContent(filterResult.getFilteredContent());

        topicReply.setReplyType(voTopicReplyReq.getReplyType());
        TopicReply reply = topicReplyRepository.save(topicReply);
        Preconditions.checkNotNull(reply, "reply is fail");
        //回复成功修改回复总数

        return ResponseEntity.ok(VoBaseResp.ok("回复成功", VoBaseResp.class));
    }

    @Override
    public TopicReply findById(Long id) {
        return topicReplyRepository.findOne(id);
    }


    @Override
    public ResponseEntity<VoTopicReplyListResp> listReply(Long topicCommentId) {
        List<TopicReply> topicReplies = topicReplyRepository.findByTopicCommentId(topicCommentId);
        VoTopicReplyListResp voTopicReplyListResp = VoBaseResp.ok("查询成功",VoTopicReplyListResp.class);
        for (TopicReply topicReply : topicReplies){
            VoTopicReplyResp voTopicReplyResp = new VoTopicReplyResp();
            voTopicReplyResp.setUserName(topicReply.getUserName());
            voTopicReplyResp.setContent(topicReply.getContent());
            voTopicReplyResp.setTopTotalNum(topicReply.getTopTotalNum());
            voTopicReplyListResp.getVoTopicReplyResps().add(voTopicReplyResp);
        }
        return ResponseEntity.ok(voTopicReplyListResp);
    }

}
