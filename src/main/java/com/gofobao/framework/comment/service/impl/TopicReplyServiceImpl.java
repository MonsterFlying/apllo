package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.vo.request.VoTopicReplyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by xin on 2017/11/13.
 */
@Service
public class TopicReplyServiceImpl implements TopicReplyService {


    @Autowired
    TopicReplyRepository topicReplyRepository;

    @Override
    public ResponseEntity<VoBaseResp> publishReply(VoTopicReplyReq voTopicReplyReq, Long userId) {
        return null;
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


}
