package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicTopRecord;

public interface TopisIntegralBiz {

    /**
     * 发帖送积分
     *
     * @param topic
     * @return
     */
    boolean publishTopic(Topic topic);


    /**
     * 回复送积分
     *
     * @param topicComment
     * @return
     */
    boolean publishComment(TopicComment topicComment);



    /**
     * 回复送积分
     *
     * @param topicReply
     * @return
     */
    boolean publishReply(TopicReply topicReply);


    /**
     * 点赞送积分
     *
     * @param topicTopRecord
     * @return
     */
    boolean TopicTop(TopicTopRecord topicTopRecord);


}
