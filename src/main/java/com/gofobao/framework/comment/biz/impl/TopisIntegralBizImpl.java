package com.gofobao.framework.comment.biz.impl;

import com.gofobao.framework.comment.biz.TopicsIntegralRecordBiz;
import com.gofobao.framework.comment.biz.TopisIntegralBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TopisIntegralBizImpl implements TopisIntegralBiz {

    @Autowired
    TopicsIntegralRecordBiz topicsIntegralRecordBiz;

    @Override
    public boolean publishTopic(@NonNull Topic topic) {
        Preconditions.checkNotNull(topic.getId(), "topis id is null");
        // 获取规则送积分
        Long value = 5L;
        try {
            return topicsIntegralRecordBiz.operateIntegral(topic.getUserId(), value, topic.getId(),
                    0, TopicsIntegralRecordBizImpl.OP_TYPE_ID_TOPIC);
        } catch (Exception e) {
            log.error("发帖派发积分失败", e);
            return false;
        }
    }

    @Override
    public boolean publishComment(TopicComment topicComment) {
        Preconditions.checkNotNull(topicComment.getId(), "topicComment id is null");
        // 获取规则送积分
        Long value = 1L;
        try {
            return topicsIntegralRecordBiz.operateIntegral(topicComment.getUserId(), value, topicComment.getId(),
                    1, TopicsIntegralRecordBizImpl.OP_TYPE_ID_COMMENT);
        } catch (Exception e) {
            log.error("评论派发积分失败", e);
            return false;
        }
    }

    @Override
    public boolean publishReply(TopicReply topicReply) {
        Preconditions.checkNotNull(topicReply.getId(), "topicReply id is null");
        // 获取规则送积分
        Long value = 1L;
        try {
            return topicsIntegralRecordBiz.operateIntegral(topicReply.getUserId(), value, topicReply.getId(),
                    1, TopicsIntegralRecordBizImpl.OP_TYPE_ID_RPELY);
        } catch (Exception e) {
            log.error("回复派发积分失败", e);
            return false;
        }
    }

    @Override
    public boolean TopicTop(TopicTopRecord topicTopRecord) {
        Preconditions.checkNotNull(topicTopRecord.getId(), "topicTopRecord id is null");
        // 获取规则送积分
        Long value = 1L;
        try {
            return topicsIntegralRecordBiz.operateIntegral(topicTopRecord.getUserId(), value, topicTopRecord.getId(),
                    1, TopicsIntegralRecordBizImpl.OP_TYPE_ID_TOP);
        } catch (Exception e) {
            log.error("点赞派发积分失败", e);
            return false;
        }
    }
}
